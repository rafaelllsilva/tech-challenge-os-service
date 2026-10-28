package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OsCanceladaPayload;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Sem @Transactional na classe: as transações são controladas pelo teste para provar o rollback.
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "os.outbox.relay.habilitado=false") // o relay não pode consumir os eventos pendentes durante o teste
class OutboxPublicadorDeEventosIT {

    @Autowired
    private OutboxPublicadorDeEventos publicador;

    @Autowired
    private OutboxEventoRepository repository;

    @Autowired
    private EventoSerializer serializer;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    private EventEnvelope<OsCanceladaPayload> evento() {
        return EventEnvelope.novo(TipoEvento.OS_CANCELADA, "42",
                new OsCanceladaPayload("42", "desistiu", "RECEBIDA"), Clock.systemUTC());
    }

    @Test
    void persisteNaOutboxQuandoATransacaoDaOsFazCommit() {
        EventEnvelope<OsCanceladaPayload> evento = evento();

        new TransactionTemplate(transactionManager).executeWithoutResult(s -> publicador.publicar(evento));

        OutboxEventoEntity gravado = repository.findById(evento.eventId()).orElseThrow();
        assertThat(gravado.getStatus()).isEqualTo(StatusOutbox.PENDENTE);
        assertThat(gravado.getTopico()).isEqualTo("os-events");
        assertThat(serializer.desserializar(gravado.getPayload())).isEqualTo(evento);
    }

    @Test
    void naoPersisteNaOutboxQuandoATransacaoDaOsFazRollback() {
        EventEnvelope<OsCanceladaPayload> evento = evento();

        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(s -> {
            publicador.publicar(evento);
            throw new IllegalStateException("falha ao salvar a OS");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(repository.findById(evento.eventId())).isEmpty();
        assertThat(repository.count()).isZero();
    }

    @Test
    void falhaQuandoChamadoForaDeUmaTransacao() {
        assertThatThrownBy(() -> publicador.publicar(evento()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }
}
