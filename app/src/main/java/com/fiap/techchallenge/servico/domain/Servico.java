package com.fiap.techchallenge.servico.domain;

import com.fiap.techchallenge.exception.ServicoBadStatusException;
import com.fiap.techchallenge.os.domain.OrdemDeServico;

import java.time.LocalDateTime;

public class Servico {

    private Long id;
    private TipoServico tipoServico;
    private OrdemDeServico ordemDeServico;
    private ServicoStatus status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    public Servico(Long id, TipoServico tipoServico, OrdemDeServico ordemDeServico,
                   ServicoStatus status, LocalDateTime dataInicio, LocalDateTime dataFim) {
        this.id = id;
        this.tipoServico = tipoServico;
        this.ordemDeServico = ordemDeServico;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public Servico() {}

    public static Servico criar(TipoServico tipoServico, OrdemDeServico ordemDeServico) {
        return new Servico(null,
                tipoServico,
                ordemDeServico,
                ServicoStatus.AGUARDANDO_INICIO,
                null,
                null);
    }

    public Long getId() {
        return id;
    }

    public TipoServico getTipoServico() {
        return tipoServico;
    }

    public OrdemDeServico getOrdemDeServico() {
        return ordemDeServico;
    }

    public ServicoStatus getStatus() {
        return status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setId(Long id) { this.id = id; }

    public void setStatus(ServicoStatus status) {
        this.status = status;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public void setTipoServico(TipoServico tipoServico) {
        this.tipoServico = tipoServico;
    }

    public void setOrdemDeServico(OrdemDeServico ordemDeServico) {
        this.ordemDeServico = ordemDeServico;
    }

    public void atualizar(OrdemDeServico ordem, TipoServico tipo) {

            if (this.status == ServicoStatus.FINALIZADO) {
                throw new ServicoBadStatusException("Não pode atualizar finalizado");
            }


        if (ordem != null) {
            this.ordemDeServico = ordem;
        }

        if (tipo != null) {
            this.tipoServico = tipo;
        }
    }

    public void finalizar() {
        if (status != ServicoStatus.EM_EXECUCAO) {
            throw new ServicoBadStatusException("Só pode finalizar em execução");
        }

        this.status = ServicoStatus.FINALIZADO;
        this.dataFim = LocalDateTime.now();
    }


    public void iniciar() {
        if (status != ServicoStatus.AGUARDANDO_INICIO) {
            throw new ServicoBadStatusException("Serviço já iniciado ou finalizado");
        }

        this.status = ServicoStatus.EM_EXECUCAO;
        this.dataInicio = LocalDateTime.now();
    }

    public void deletar() {
        if (status == ServicoStatus.EM_EXECUCAO) {
            throw new ServicoBadStatusException("Não pode deletar em execução");
        }

        this.status = ServicoStatus.DELETADO;
    }
}