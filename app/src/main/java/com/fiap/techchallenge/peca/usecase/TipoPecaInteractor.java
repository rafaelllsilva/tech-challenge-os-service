package com.fiap.techchallenge.peca.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TipoPecaInteractor implements TipoPecaUseCase {

    private final TipoPecaGateway tipoPecaGateway;

    public TipoPecaInteractor(TipoPecaGateway tipoPecaGateway) {
        this.tipoPecaGateway = tipoPecaGateway;
    }

    @Override
    public List<TipoPeca> getAllTiposPeca() {
        return this.tipoPecaGateway.findAll();
    }

    @Override
    public Optional<TipoPeca> getTipoPeca(Long id) {
        return this.tipoPecaGateway.findById(id);
    }

    @Override
    public TipoPeca createTipoPeca(String nome, BigDecimal valorUnitario, Integer quantidadeEstoque) {
        TipoPeca tipoPeca = new TipoPeca();
        tipoPeca.setNome(nome);
        tipoPeca.setValorUnitario(valorUnitario);
        tipoPeca.setQuantidadeEstoque(quantidadeEstoque);

        return this.tipoPecaGateway.save(tipoPeca);
    }

    @Override
    public TipoPeca updateTipoPeca(Long id, String nome, BigDecimal valorUnitario, Integer quantidadeEstoque) {
        TipoPeca tipoPecaEditada = this.tipoPecaGateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tipo de peça de id {} não encontrado!", id);
                    return new NotFoundException("Tipo de peça de id " + id + " não encontrado!");
                });

        if (nome != null) tipoPecaEditada.setNome(nome);
        if (valorUnitario != null) tipoPecaEditada.setValorUnitario(valorUnitario);
        if (quantidadeEstoque != null) tipoPecaEditada.setQuantidadeEstoque(quantidadeEstoque);

        return this.tipoPecaGateway.save(tipoPecaEditada);
    }

    @Override
    public TipoPeca deleteTipoPeca(Long id) {
        TipoPeca tipoPecaDeletada = this.tipoPecaGateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tipo de peça de id {} não encontrado!", id);
                    return new NotFoundException("Tipo de peça de id " + id + " não encontrado!");
                });

        this.tipoPecaGateway.delete(tipoPecaDeletada);
        return tipoPecaDeletada;
    }
}
