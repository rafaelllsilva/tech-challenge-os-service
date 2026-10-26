package com.fiap.techchallenge.servico.framework.persistence.repository;

import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {

    @Query("""
        SELECT s
        FROM ServicoEntity s
        WHERE s.ordemDeServico.id = :id
        """
    )
    List<ServicoEntity> findAllByOsId(@Param("id") Long id);

    boolean existsByTipoServicoId(Long id);
}
