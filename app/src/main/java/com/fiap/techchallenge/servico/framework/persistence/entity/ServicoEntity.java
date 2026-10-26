package com.fiap.techchallenge.servico.framework.persistence.entity;

import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "servico")
public class ServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tipo_servico", nullable = false)
    private TipoServicoEntity tipoServico;

    @ManyToOne
    @JoinColumn(name = "id_ordem_de_servico", nullable = false)
    private OrdemDeServicoEntity ordemDeServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServicoStatus status;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    public ServicoEntity(TipoServicoEntity tipoServico, OrdemDeServicoEntity ordemDeServico, ServicoStatus status) {
        this.tipoServico = tipoServico;
        this.ordemDeServico = ordemDeServico;
        this.status = status;
    }

    public ServicoEntity(Long id, TipoServicoEntity tipoServico, OrdemDeServicoEntity ordemDeServico, ServicoStatus status, LocalDateTime dataInicio, LocalDateTime dataFim) {
        this.id = id;
        this.tipoServico = tipoServico;
        this.ordemDeServico = ordemDeServico;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public ServicoEntity(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoServicoEntity getTipoServico() {
        return tipoServico;
    }

    public void setTipoServico(TipoServicoEntity tipoServico) {
        this.tipoServico = tipoServico;
    }

    public OrdemDeServicoEntity getOrdemDeServico() {
        return ordemDeServico;
    }

    public void setOrdemDeServico(OrdemDeServicoEntity ordemDeServico) {
        this.ordemDeServico = ordemDeServico;
    }

    public ServicoStatus getStatus() {
        return status;
    }

    public void setStatus(ServicoStatus status) {
        this.status = status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }
}
