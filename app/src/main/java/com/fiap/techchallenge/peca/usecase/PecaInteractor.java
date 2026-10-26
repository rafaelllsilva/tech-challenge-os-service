package com.fiap.techchallenge.peca.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoGateway;
import com.fiap.techchallenge.peca.domain.Peca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// substituir OrdemDeServicoGateway 
@Service
@Slf4j
public class PecaInteractor implements PecaUseCase {

    private final PecaGateway pecaGateway;
    private final TipoPecaGateway tipoPecaGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway;

    public PecaInteractor(PecaGateway pecaGateway, TipoPecaGateway tipoPecaGateway, OrdemDeServicoGateway ordemDeServicoGateway) {
        this.pecaGateway = pecaGateway;
        this.tipoPecaGateway = tipoPecaGateway;
        this.ordemDeServicoGateway = ordemDeServicoGateway;
    }

    @Override
    public List<Peca> getAllPecas() {
        return this.pecaGateway.findAll();
    }

    @Override
    public Optional<Peca> getPeca(Long id) {
        return this.pecaGateway.findById(id);
    }

    @Override
    public Peca createPeca(Long idOs, Long idTipoPeca, Integer quantidade) {
        Peca peca = new Peca();
        peca.setOrdemDeServico(ordemDeServicoGateway.findOrdemDeServicoById(idOs)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço de id " + idOs + " não encontrada!")));
        peca.setTipoPeca(tipoPecaGateway.findById(idTipoPeca)
                .orElseThrow(() -> new NotFoundException("Tipo de peça de id " + idTipoPeca + " não encontrado!")));
        peca.setQuantidade(quantidade);

        return this.pecaGateway.save(peca);
    }

    @Override
    public Peca updatePeca(Long id, Long idOs, Long idTipoPeca, Integer quantidade) {
        Peca pecaEditada = this.pecaGateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Peça de id {} não encontrada!", id);
                    return new NotFoundException("Peça de id " + id + " não encontrada!");
                });

        if (idOs != null) {
            pecaEditada.setOrdemDeServico(ordemDeServicoGateway.findOrdemDeServicoById(idOs)
                    .orElseThrow(() -> new NotFoundException("Ordem de serviço de id " + idOs + " não encontrada!")));
        }
        if (idTipoPeca != null) {
            pecaEditada.setTipoPeca(tipoPecaGateway.findById(idTipoPeca)
                    .orElseThrow(() -> new NotFoundException("Tipo de peça de id " + idTipoPeca + " não encontrado!")));
        }
        if (quantidade != null) {
            pecaEditada.setQuantidade(quantidade);
        }

        return this.pecaGateway.save(pecaEditada);
    }

    @Override
    public Peca deletePeca(Long id) {
        Peca pecaDeletada = this.pecaGateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Peça de id {} não encontrada!", id);
                    return new NotFoundException("Peça de id " + id + " não encontrada!");
                });

        this.pecaGateway.delete(pecaDeletada);
        return pecaDeletada;
    }
}
