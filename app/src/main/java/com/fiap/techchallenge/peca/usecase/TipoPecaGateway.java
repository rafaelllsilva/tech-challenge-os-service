package com.fiap.techchallenge.peca.usecase;

import com.fiap.techchallenge.peca.domain.TipoPeca;
import java.util.List;
import java.util.Optional;

public interface TipoPecaGateway {

    List<TipoPeca> findAll();

    Optional<TipoPeca> findById(Long id);

    TipoPeca save(TipoPeca tipoPeca);

    void delete(TipoPeca tipoPeca);

    List<TipoPeca> saveAll(List<TipoPeca> tipoPeca);

    List<TipoPeca> findAllByIds(List<Long> ids);
}
