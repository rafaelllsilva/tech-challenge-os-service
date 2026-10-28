package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.exception.BadRequestException;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.ExecucaoFalhouPayload;
import com.fiap.techchallenge.os.mensageria.contrato.payload.PagamentoEstornadoPayload;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoRepository;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.transaction.support.TransactionOperations;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DespachanteDeEventosTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(AGORA, ZoneOffset.UTC);

    private final EventoSerializer serializer = new EventoSerializer(JsonMapper.builder().build());
    private final EventoProcessadoRepository repository = mock(EventoProcessadoRepository.class);

    @SuppressWarnings("unchecked")
    private final ManipuladorDeEvento<PagamentoEstornadoPayload> manipulador = mock(ManipuladorDeEvento.class);

    private DespachanteDeEventos despachante;

    @BeforeEach
    void setUp() {
        when(manipulador.tipo()).thenReturn(TipoEvento.PAGAMENTO_ESTORNADO);
        despachante = new DespachanteDeEventos(serializer, repository, TransactionOperations.withoutTransaction(),
                CLOCK, List.of(manipulador));
    }

    private EventEnvelope<PagamentoEstornadoPayload> estorno() {
        return EventEnvelope.novo(TipoEvento.PAGAMENTO_ESTORNADO, "42", new PagamentoEstornadoPayload("42", "pag-1"), CLOCK);
    }

    @Test
    void eventoNovoEhTratadoERegistradoComoProcessado() {
        var evento = estorno();

        var resultado = despachante.despachar(serializer.serializar(evento));

        assertThat(resultado).isEqualTo(ResultadoDoProcessamento.SUCESSO);
        verify(manipulador).tratar(evento);
        ArgumentCaptor<EventoProcessadoEntity> captor = ArgumentCaptor.forClass(EventoProcessadoEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(evento.eventId());
        assertThat(captor.getValue().getTipo()).isEqualTo("PAGAMENTO_ESTORNADO");
        assertThat(captor.getValue().getCorrelationId()).isEqualTo("42");
        assertThat(captor.getValue().getProcessadoEm()).isEqualTo(AGORA);
        assertThat(captor.getValue().getPayload()).contains("pag-1");
    }

    @Test
    void eventoDuplicadoEhIgnorado() {
        var evento = estorno();
        when(repository.existsById(evento.eventId())).thenReturn(true);

        var resultado = despachante.despachar(serializer.serializar(evento));

        assertThat(resultado).isEqualTo(ResultadoDoProcessamento.DUPLICADO);
        verify(manipulador, never()).tratar(any());
        verify(repository, never()).save(any());
    }

    @Test
    void eventoSemManipuladorEhConfirmadoSemProcessar() {
        var evento = EventEnvelope.novo(TipoEvento.EXECUCAO_FALHOU, "42", new ExecucaoFalhouPayload("42", "x"), CLOCK);

        var resultado = despachante.despachar(serializer.serializar(evento));

        assertThat(resultado).isEqualTo(ResultadoDoProcessamento.SEM_HANDLER);
        verify(repository, never()).save(any());
    }

    @Test
    void tipoDesconhecidoEhConfirmado() {
        String json = """
                {"eventId":"7f1c1a52-6c6f-4a8e-9a5d-3c1f0d2b9b11","type":"INEXISTENTE","version":1,
                 "occurredAt":"2026-01-01T10:00:00Z","correlationId":"42","payload":{}}""";

        assertThat(despachante.despachar(json)).isEqualTo(ResultadoDoProcessamento.TIPO_DESCONHECIDO);
        verify(manipulador, never()).tratar(any());
    }

    @Test
    void erroDeNegocioEhConfirmadoENaoRegistraComoProcessado() {
        var evento = estorno();
        doThrow(new BadRequestException("transição inválida")).when(manipulador).tratar(evento);

        var resultado = despachante.despachar(serializer.serializar(evento));

        assertThat(resultado).isEqualTo(ResultadoDoProcessamento.ERRO_NEGOCIO);
        verify(repository, never()).save(any());
    }

    @Test
    void erroTecnicoEhRelancado() {
        var evento = estorno();
        doThrow(new IllegalArgumentException("banco fora do ar")).when(manipulador).tratar(evento);

        assertThatThrownBy(() -> despachante.despachar(serializer.serializar(evento)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void jsonMalformadoEhRelancado() {
        assertThatThrownBy(() -> despachante.despachar("isto não é json"))
                .isInstanceOf(JacksonException.class);
    }

    @Test
    void colocaCorrelationIdEEventIdNoMdcDuranteOProcessamentoELimpaDepois() {
        var evento = estorno();
        List<String> vistos = new ArrayList<>();
        doAnswer(inv -> {
            vistos.add(MDC.get("correlationId"));
            vistos.add(MDC.get("eventId"));
            return null;
        }).when(manipulador).tratar(evento);

        despachante.despachar(serializer.serializar(evento));

        assertThat(vistos).containsExactly("42", evento.eventId().toString());
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("eventId")).isNull();
    }

    @Test
    void doisManipuladoresParaOMesmoTipoFalhamNaInicializacao() {
        assertThatThrownBy(() -> new DespachanteDeEventos(serializer, repository,
                TransactionOperations.withoutTransaction(), CLOCK, List.of(manipulador, manipulador)))
                .isInstanceOf(IllegalStateException.class);
    }
}
