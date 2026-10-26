package com.fiap.techchallenge.os.adapter.gateway;

import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoStatusHistoricoEntity;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoStatusHistoricoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoStatusOrdemDeServicoGatewayImplTest {

    @Mock
    private OrdemDeServicoStatusHistoricoRepository repository;

    @Test
    void registraUmaEntradaImutavelDeHistorico() {
        HistoricoStatusOrdemDeServicoGatewayImpl gateway = new HistoricoStatusOrdemDeServicoGatewayImpl(repository);
        Instant alteradoEm = Instant.parse("2026-08-29T12:00:00Z");

        gateway.registrarStatus(123L, StatusOrdemDeServico.EM_DIAGNOSTICO, alteradoEm);

        ArgumentCaptor<OrdemDeServicoStatusHistoricoEntity> captor = ArgumentCaptor.forClass(OrdemDeServicoStatusHistoricoEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(123L, captor.getValue().getIdOrdemDeServico());
        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, captor.getValue().getStatus());
        assertEquals(alteradoEm, captor.getValue().getAlteradoEm());
    }

    @Test
    void buscaOInicioDoStatusMaisRecente() {
        HistoricoStatusOrdemDeServicoGatewayImpl gateway = new HistoricoStatusOrdemDeServicoGatewayImpl(repository);
        Instant alteradoEm = Instant.parse("2026-08-29T12:00:00Z");
        OrdemDeServicoStatusHistoricoEntity historico = new OrdemDeServicoStatusHistoricoEntity();
        historico.setAlteradoEm(alteradoEm);
        when(repository.findTopByIdOrdemDeServicoOrderByAlteradoEmDescIdDesc(123L)).thenReturn(Optional.of(historico));

        Optional<Instant> result = gateway.buscarInicioDoStatusAtual(123L);

        assertTrue(result.isPresent());
        assertEquals(alteradoEm, result.get());
    }
}
