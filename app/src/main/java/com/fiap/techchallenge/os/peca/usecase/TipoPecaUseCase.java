package com.fiap.techchallenge.os.peca.usecase;

import com.fiap.techchallenge.os.peca.domain.TipoPeca;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TipoPecaUseCase {

    List<TipoPeca> getAllTiposPeca();

    Optional<TipoPeca> getTipoPeca(Long id);

    TipoPeca createTipoPeca(String nome, BigDecimal valorUnitario, Integer quantidadeEstoque);

    TipoPeca updateTipoPeca(Long id, String nome, BigDecimal valorUnitario, Integer quantidadeEstoque);

    TipoPeca deleteTipoPeca(Long id);
}
