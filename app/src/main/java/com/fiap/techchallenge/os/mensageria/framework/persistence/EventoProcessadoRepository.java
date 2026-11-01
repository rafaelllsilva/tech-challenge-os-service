package com.fiap.techchallenge.os.mensageria.framework.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoProcessadoRepository extends JpaRepository<EventoProcessadoEntity, UUID> {

    List<EventoProcessadoEntity> findByCorrelationId(String correlationId);
}
