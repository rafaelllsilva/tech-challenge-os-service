package com.fiap.techchallenge.os.peca.framework.persistence.repository;

import com.fiap.techchallenge.os.peca.framework.persistence.entity.PecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PecaRepository extends JpaRepository<PecaEntity, Long> {

}
