package com.fiap.techchallenge.os.mensageria.framework.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "evento_processado")
public class EventoProcessadoEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "tipo", length = 40, nullable = false)
    private String tipo;

    @Column(name = "correlation_id", length = 64, nullable = false)
    private String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "processado_em", nullable = false)
    private Instant processadoEm;
}
