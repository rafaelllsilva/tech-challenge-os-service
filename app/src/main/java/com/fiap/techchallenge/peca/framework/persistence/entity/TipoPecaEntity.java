package com.fiap.techchallenge.peca.framework.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "oficina", name = "tipo_peca")
public class TipoPecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    @Column(name = "nome", columnDefinition = "varchar")
    private String nome;

    @Column(name = "valor_unitario", columnDefinition = "numeric")
    private BigDecimal valorUnitario;

    @Column(name = "quantidade_estoque", columnDefinition = "integer")
    private Integer quantidadeEstoque;
}
