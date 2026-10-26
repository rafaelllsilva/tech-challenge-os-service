package com.fiap.techchallenge.os.domain;

import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.veiculo.domain.Veiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrdemDeServico {

    private Long id;
    private StatusOrdemDeServico status;
    private BigDecimal orcamento;
    private String solicitacao;
    private LocalDateTime dataHoraCriacao;
    private Cliente cliente;
    private Veiculo veiculo;
    private List<Peca> pecas;
    private List<Servico> servicos;

    public OrdemDeServico() {}

    public OrdemDeServico(Long id, StatusOrdemDeServico status, BigDecimal orcamento, String solicitacao, LocalDateTime dataHoraCriacao, Cliente cliente, Veiculo veiculo, List<Peca> pecas, List<Servico> servicos) {
        this.id = id;
        this.status = status;
        this.orcamento = orcamento;
        this.solicitacao = solicitacao;
        this.dataHoraCriacao = dataHoraCriacao;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.pecas = pecas;
        this.servicos = servicos;
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

    public List<Peca> getPecas() {
        return pecas;
    }

    public List<Servico> getServicos() {
        return servicos;
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

    public void setPecas(List<Peca> pecas) {
        this.pecas = pecas;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    // Logica de dominio

    public record ValidacaoTransicaoDeStatus(boolean permitida, String erroDeValidacao) {}

    public boolean podeEditarOuAdicionarServico() {
        return this.status == StatusOrdemDeServico.EM_DIAGNOSTICO ||
                this.status == StatusOrdemDeServico.RECEBIDA;
    }

    public ValidacaoTransicaoDeStatus podeTransicionarParaStatus(StatusOrdemDeServico novoStatus) {
        return switch (novoStatus) {
            case EM_DIAGNOSTICO -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.RECEBIDA);
                yield new ValidacaoTransicaoDeStatus(permitida, permitida ? null : "Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.RECEBIDA));
            }
            case AGUARDANDO_APROVACAO -> {
                boolean permitida = this.status.equals(StatusOrdemDeServico.EM_DIAGNOSTICO) && this.orcamentoMaiorQueZero() && possuiPecasCadastradas() && possuiServicosCadastrados();
                yield new ValidacaoTransicaoDeStatus(permitida, permitida
                        ? null
                        : "Erro: A Ordem de Serviço %d deve estar no status '%s', deve possuir peças e serviços cadastrados e o orçamento deve estar calculado!".formatted(this.id, StatusOrdemDeServico.EM_DIAGNOSTICO));
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

    public boolean possuiServicosCadastrados() {
        return servicos != null && !servicos.isEmpty();
    }

    public boolean possuiPecasCadastradas() {
        return pecas != null && !pecas.isEmpty();
    }

    public void calcularOrcamento() {
        BigDecimal orcamento = BigDecimal.valueOf(0L, 2);

        if (this.pecas != null && !this.pecas.isEmpty()) {
            for (Peca peca : this.pecas) {
                int quantidade = peca.getQuantidade();
                BigDecimal valorUnitario = peca.getTipoPeca().getValorUnitario();
                orcamento = orcamento.add(valorUnitario.multiply(BigDecimal.valueOf(quantidade)));
            }
        }

        if (this.servicos != null && ! this.servicos.isEmpty()) {
            for (Servico servico : this.servicos) {
                BigDecimal valor = servico.getTipoServico().getValor();
                orcamento = orcamento.add(valor);
            }
        }

        this.orcamento = orcamento;
    }
}
