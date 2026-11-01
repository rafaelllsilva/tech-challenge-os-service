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
@Table(name = "outbox_evento")
public class OutboxEventoEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tipo", length = 40, nullable = false)
    private String tipo;

    @Column(name = "topico", length = 60, nullable = false)
    private String topico;

    @Column(name = "correlation_id", length = 64, nullable = false)
    private String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private StatusOutbox status = StatusOutbox.PENDENTE;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "ultimo_erro", columnDefinition = "text")
    private String ultimoErro;

    @Column(name = "enviado_em")
    private Instant enviadoEm;
}
