package com.fiap.techchallenge.os.mensageria.framework.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.UUID;

public interface OutboxEventoRepository extends JpaRepository<OutboxEventoEntity, UUID> {

    /**
     * Lê o lote de pendentes com FOR UPDATE SKIP LOCKED (timeout -2), para que várias réplicas
     * do relay não processem os mesmos eventos.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<OutboxEventoEntity> findByStatusOrderByOcorridoEmAsc(StatusOutbox status, Pageable pageable);

    long countByStatus(StatusOutbox status);

    List<OutboxEventoEntity> findByCorrelationId(String correlationId);
}
