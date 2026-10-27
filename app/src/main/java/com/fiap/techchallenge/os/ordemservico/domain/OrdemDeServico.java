package com.fiap.techchallenge.os.ordemservico.domain;

import com.fiap.techchallenge.os.cliente.domain.Cliente;
import com.fiap.techchallenge.os.veiculo.domain.Veiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdemDeServico {

    private Long id;
    private StatusOrdemDeServico status;
    private BigDecimal orcamento;
    private String solicitacao;
    private LocalDateTime dataHoraCriacao;
    private Cliente cliente;
    private Veiculo veiculo;

    public OrdemDeServico() {}

    public OrdemDeServico(Long id, StatusOrdemDeServico status, BigDecimal orcamento, String solicitacao, LocalDateTime dataHoraCriacao, Cliente cliente, Veiculo veiculo) {
        this.id = id;
        this.status = status;
        this.orcamento = orcamento;
        this.solicitacao = solicitacao;
        this.dataHoraCriacao = dataHoraCriacao;
        this.cliente = cliente;
        this.veiculo = veiculo;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public StatusOrdemDeServico getStatus() {
        return status;
    }

    public BigDecimal getOrcamento() {
        return orcamento;
    }

    public String getSolicitacao() {
        return solicitacao;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(StatusOrdemDeServico status) {
        this.status = status;
    }

    public void setOrcamento(BigDecimal orcamento) {
        this.orcamento = orcamento;
    }

    public void setSolicitacao(String solicitacao) {
        this.solicitacao = solicitacao;
    }

    public void setDataHoraCriacao(LocalDateTime dataHoraCriacao) {
        this.dataHoraCriacao = dataHoraCriacao;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    // Logica de dominio

    public record ValidacaoTransicaoDeStatus(boolean permitida, String erroDeValidacao) {}

    public ValidacaoTransicaoDeStatus podeTransicionarParaStatus(StatusOrdemDeServico novoStatus) {
        return switch (novoStatus) {
            case EM_DIAGNOSTICO -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.RECEBIDA);
                yield new ValidacaoTransicaoDeStatus(permitida, permitida ? null : "Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.RECEBIDA));
            }
            case AGUARDANDO_APROVACAO -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.EM_DIAGNOSTICO) && this.orcamentoMaiorQueZero();
                yield new ValidacaoTransicaoDeStatus(permitida, permitida
                        ? null
                        : "Erro: A Ordem de Serviço %d deve estar no status '%s', o orçamento deve ser maior que zero!".formatted(this.id, StatusOrdemDeServico.EM_DIAGNOSTICO));
            }
            case APROVADA -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.AGUARDANDO_APROVACAO);
                yield new ValidacaoTransicaoDeStatus(permitida, permitida
                        ? null
                        : "Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.AGUARDANDO_APROVACAO));
            }
            case EM_EXECUCAO -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.APROVADA);
                yield new ValidacaoTransicaoDeStatus(permitida, permitida
                        ? null
                        : "Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.APROVADA));
            }
            case FINALIZADA -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.AGUARDANDO_APROVACAO) || this.status.equals(StatusOrdemDeServico.EM_EXECUCAO);
                yield new ValidacaoTransicaoDeStatus(permitida, permitida
                        ? null
                        : "Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.EM_EXECUCAO));
            }
            case ENTREGUE -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.FINALIZADA);
                yield new ValidacaoTransicaoDeStatus(permitida, permitida
                        ? null
                        : "Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.FINALIZADA));
            }
            default -> new ValidacaoTransicaoDeStatus(false, "Status inválido!");
        };
    }

    public boolean orcamentoMaiorQueZero() {
        return this.orcamento != null && this.orcamento.compareTo(BigDecimal.ZERO) > 0;
    }
}
