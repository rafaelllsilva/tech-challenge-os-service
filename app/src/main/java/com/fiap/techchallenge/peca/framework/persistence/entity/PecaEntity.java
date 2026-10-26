package com.fiap.techchallenge.peca.framework.persistence.entity;

import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "oficina", name = "peca")
public class PecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    @Column(name = "quantidade", columnDefinition = "integer")
    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "id_tipo_peca", nullable = false)
    private TipoPecaEntity tipoPeca;

    @ManyToOne
    @JoinColumn(name = "id_os", nullable = false)
    private OrdemDeServicoEntity ordemDeServico;
}
