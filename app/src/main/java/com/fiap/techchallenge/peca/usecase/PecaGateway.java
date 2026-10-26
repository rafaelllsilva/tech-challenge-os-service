package com.fiap.techchallenge.peca.usecase;

import com.fiap.techchallenge.peca.domain.Peca;
import java.util.List;
import java.util.Optional;

public interface PecaGateway {

    List<Peca> findAll();

    Optional<Peca> findById(Long id);

    Peca save(Peca peca);

    void delete(Peca peca);
}
