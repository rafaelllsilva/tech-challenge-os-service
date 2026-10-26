package com.fiap.techchallenge.os.adapter.gateway;

import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoStatusHistoricoEntity;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoStatusHistoricoRepository;
import com.fiap.techchallenge.os.usecase.HistoricoStatusOrdemDeServicoGateway;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class HistoricoStatusOrdemDeServicoGatewayImpl implements HistoricoStatusOrdemDeServicoGateway {

    private final OrdemDeServicoStatusHistoricoRepository repository;

    public HistoricoStatusOrdemDeServicoGatewayImpl(OrdemDeServicoStatusHistoricoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void registrarStatus(Long idOrdemDeServico, StatusOrdemDeServico status, Instant alteradoEm) {
        OrdemDeServicoStatusHistoricoEntity historico = new OrdemDeServicoStatusHistoricoEntity();
        historico.setIdOrdemDeServico(idOrdemDeServico);
        historico.setStatus(status);
        historico.setAlteradoEm(alteradoEm);
        repository.save(historico);
    }

    @Override
    public Optional<Instant> buscarInicioDoStatusAtual(Long idOrdemDeServico) {
        return repository.findTopByIdOrdemDeServicoOrderByAlteradoEmDescIdDesc(idOrdemDeServico)
                .map(OrdemDeServicoStatusHistoricoEntity::getAlteradoEm);
    }
}
