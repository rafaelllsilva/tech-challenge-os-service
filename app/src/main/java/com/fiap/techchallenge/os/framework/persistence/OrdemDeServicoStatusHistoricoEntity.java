package com.fiap.techchallenge.os.framework.persistence;

import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "oficina", name = "ordem_de_servico_status_historico")
public class OrdemDeServicoStatusHistoricoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_ordem_de_servico", nullable = false)
    private Long idOrdemDeServico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusOrdemDeServico status;

    @Column(name = "alterado_em", nullable = false)
    private Instant alteradoEm;
}
