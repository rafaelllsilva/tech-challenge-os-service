package com.fiap.techchallenge.os.peca.usecase;

import com.fiap.techchallenge.os.peca.domain.Peca;

import java.util.List;
import java.util.Optional;

public interface PecaUseCase {

    List<Peca> getAllPecas();

    Optional<Peca> getPeca(Long id);

    Peca createPeca(Long idOs, Long idTipoPeca, Integer quantidade);

    Peca updatePeca(Long id, Long idOs, Long idTipoPeca, Integer quantidade);

    Peca deletePeca(Long id);
}
