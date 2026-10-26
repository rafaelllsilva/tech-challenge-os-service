package com.fiap.techchallenge.os.framework.persistence;

import com.fiap.techchallenge.cliente.framework.persistence.ClienteEntity;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.peca.framework.persistence.entity.PecaEntity;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import com.fiap.techchallenge.veiculo.framework.persistence.VeiculoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "oficina", name = "ordem_de_servico")
public class OrdemDeServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", columnDefinition = "varchar")
    private StatusOrdemDeServico status;

    @Column(name = "orcamento", columnDefinition = "numeric")
    private BigDecimal orcamento;

    @Column(name = "solicitacao", columnDefinition = "varchar")
    private String solicitacao;

    @Column(name = "data_hora_criacao")
    private LocalDateTime dataHoraCriacao;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private ClienteEntity cliente;

    @ManyToOne
    @JoinColumn(name = "id_veiculo")
    private VeiculoEntity veiculo;

    @OneToMany(mappedBy = "ordemDeServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PecaEntity> pecas;

    @OneToMany(mappedBy = "ordemDeServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoEntity> servicos;
}
