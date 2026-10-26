package com.fiap.techchallenge.servico.usecase.interactor;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.exception.ServicoBadStatusException;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoGateway;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.ServicoUseCase;
import com.fiap.techchallenge.servico.usecase.gateway.ServicoGateway;
import com.fiap.techchallenge.servico.usecase.gateway.TipoServicoGateway;
import com.fiap.techchallenge.servico.usecase.command.ServicoCreateCommand;
import com.fiap.techchallenge.servico.usecase.command.ServicoUpdateCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoInteractor implements ServicoUseCase {

    private final ServicoGateway servicoGateway;
    private final TipoServicoGateway tipoServicoGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway; // mudar para o gateway referente

    public ServicoInteractor(ServicoGateway gateway, TipoServicoGateway tipoServicoGateway, OrdemDeServicoGateway ordemDeServicoGateway) {
        this.servicoGateway = gateway;
        this.tipoServicoGateway = tipoServicoGateway;
        this.ordemDeServicoGateway = ordemDeServicoGateway;
    }

    @Override
    @Transactional
    public Servico updateServico(Long idServico, ServicoUpdateCommand servicoUpdateCommand) {
        Servico servico = getServico(idServico);

        OrdemDeServico ordem = null;
        TipoServico tipo = null;

        if (servicoUpdateCommand.getOrdemDeServicoId() != null) {
            ordem = ordemDeServicoGateway.findOrdemDeServicoById(servicoUpdateCommand.getOrdemDeServicoId())
                    .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada para o id: "
                            + servicoUpdateCommand.getOrdemDeServicoId()));
        }

        if (servicoUpdateCommand.getTipoServicoId() != null) {
            tipo = tipoServicoGateway.findById(servicoUpdateCommand.getTipoServicoId())
                    .orElseThrow(() -> new NotFoundException("Tipo de serviço não encontrado para o id: "
                            + servicoUpdateCommand.getTipoServicoId()));
        }

        servico.atualizar(ordem, tipo);

        return servicoGateway.save(servico);
    }

    @Override
    @Transactional
    public void finalizarServico(Long idServico) {

        Servico servico = getServico(idServico);
        servico.finalizar();

        servicoGateway.save(servico);

    }

    @Override
    @Transactional
    public void iniciarServico(Long idServico) {

        Servico servico = getServico(idServico);

        servico.iniciar();

        servicoGateway.save(servico);

    }

    @Override
    @Transactional
    public void deleteServico(Long idServico) {

        Servico servico = getServico(idServico);

        servico.deletar();

        servicoGateway.save(servico);
    }

    @Override
    public Servico getServicoById(Long idServico) {
        return getServico(idServico);
    }


    @Override
    public List<Servico> listaServicos(Long ordemServicoId) {
        if (ordemServicoId != null) {
            return servicoGateway.findAllByOsId(ordemServicoId).orElseThrow(
                    () -> new NotFoundException("Não existem serviços cadastrados para a ordem de serviço com id: " + ordemServicoId)
            );
        }
        return servicoGateway.findAll();
    }

    @Override
    @Transactional
    public Servico creteServico(ServicoCreateCommand servicoCreateCommand) {
        var tipoServico = tipoServicoGateway.findById(servicoCreateCommand.getTipoServicoId())
                .orElseThrow(() -> new NotFoundException("Tipo de serviço não encontrado para o id: " + servicoCreateCommand.getTipoServicoId()));
        var ordemDeServico = ordemDeServicoExistsAndIsValid(servicoCreateCommand.getOrdemDeServicoId());
        Servico servico = Servico.criar(tipoServico, ordemDeServico);
        return servicoGateway.save(servico);
    }


    private OrdemDeServico ordemDeServicoExistsAndIsValid(Long id) {
        OrdemDeServico os = ordemDeServicoGateway.findOrdemDeServicoById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada para o id: " + id));

        if (!os.podeEditarOuAdicionarServico()) {
            throw new ServicoBadStatusException("Só é permitido adicionar serviços em OS que estejam com status " +
                    "EM_DIAGNOSTICO ou RECEBIDA");
        }

        return os;
    }

    private Servico getServico(Long idServico) {
        return servicoGateway.findById(idServico)
                .orElseThrow(() ->
                        new NotFoundException("Serviço não encontrado para o id: " + idServico));
    }
}
