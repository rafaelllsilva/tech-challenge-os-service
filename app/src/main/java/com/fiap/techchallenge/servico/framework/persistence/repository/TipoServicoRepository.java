package com.fiap.techchallenge.servico.framework.persistence.repository;

import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoServicoRepository  extends JpaRepository<TipoServicoEntity, Long> {

    @Query(value = """
        SELECT ts.nome,
              ROUND(AVG(EXTRACT(EPOCH FROM (s.data_fim - s.data_inicio)) / 60), 2) AS tempo_medio_minutos
        FROM oficina.servico s
        JOIN oficina.tipo_servico ts ON ts.id = s.id_tipo_servico
        WHERE s.data_inicio IS NOT NULL
          AND s.data_fim IS NOT NULL
        GROUP BY ts.id, ts.nome
        ORDER BY ts.nome
    """, nativeQuery = true)
    List<TempoMedioServicoDTO> findTempoMedioPorTipoServico();

    @Query(value = """
        SELECT ts.nome,
               ROUND(AVG(EXTRACT(EPOCH FROM (s.data_fim - s.data_inicio)) / 60), 2) AS tempo_medio_minutos
        FROM oficina.servico s
        JOIN oficina.tipo_servico ts ON ts.id = s.id_tipo_servico
        WHERE ts.id = :tipoServicoId
          AND s.data_inicio IS NOT NULL
          AND s.data_fim IS NOT NULL
        GROUP BY ts.id, ts.nome
    """, nativeQuery = true)
    Optional<TempoMedioServicoDTO> findTempoMedioPorTipoServicoId(@Param("tipoServicoId") Long tipoServicoId);
}
