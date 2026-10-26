package com.fiap.techchallenge.peca.framework.persistence.repository;

import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoPecaRepository extends JpaRepository<TipoPecaEntity, Long> {

}
