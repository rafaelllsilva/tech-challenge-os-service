package com.fiap.techchallenge.servico.usecase.interactor;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.exception.ServicoBadStatusException;
import com.fiap.techchallenge.exception.TipoServicoUnableToModify;
import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.TipoServicoUseCase;
import com.fiap.techchallenge.servico.usecase.command.TipoServicoRequestsCommand;
import com.fiap.techchallenge.servico.usecase.gateway.ServicoGateway;
import com.fiap.techchallenge.servico.usecase.gateway.TipoServicoGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoServicoInteractor implements TipoServicoUseCase {

    private final TipoServicoGateway tipoServicoGateway;
    private final ServicoGateway servicoGateway;

    public TipoServicoInteractor(TipoServicoGateway tipoServicoGateway, ServicoGateway servicoGateway) {
        this.tipoServicoGateway = tipoServicoGateway;
        this.servicoGateway = servicoGateway;
    }


    @Override
    public TipoServico getById(Long id) {
        return tipoServicoGateway.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Tipo de serviço não encontrado com ID: " + id));
    }

    @Override
    public List<TipoServico> listAll() {
        return tipoServicoGateway.findAll();
    }

    @Override
    public List<TempoMedioServicoDTO> getTempoMedio(Long tipoServicoId) {
        if (tipoServicoId == null) {
            return tipoServicoGateway.findTempoMedioPorTipoServico();
        }

        return tipoServicoGateway.findTempoMedioPorTipoServicoId(tipoServicoId)
                .map(List::of)
                .orElseThrow(() ->
                        new NotFoundException("Tipo de serviço não encontrado com ID: " + tipoServicoId));
    }

    @Override
    @Transactional
    public TipoServico create(TipoServicoRequestsCommand command) {
        TipoServico tipoServico = TipoServico.criar(
                command.getNome(),
                command.getValor()
        );

        return tipoServicoGateway.save(tipoServico);    }

    @Override
    @Transactional
    public TipoServico update(Long id, TipoServicoRequestsCommand command) {
        TipoServico tipoServico = getById(id);

        tipoServico.atualizar(
                command.getNome(),
                command.getValor()
        );

        return tipoServicoGateway.save(tipoServico);
    }

    @Override
    public void delete(Long id) {
        TipoServico tipoServico = getById(id);

        boolean hasAssociatedServicos = servicoGateway.existsByTipoServicoId(tipoServico.getId());
        if (hasAssociatedServicos) {
            throw new TipoServicoUnableToModify("Não é possível excluir o tipo de serviço, pois existem serviços associados a ele.");
        }

        tipoServicoGateway.delete(tipoServico.getId());
    }
}
