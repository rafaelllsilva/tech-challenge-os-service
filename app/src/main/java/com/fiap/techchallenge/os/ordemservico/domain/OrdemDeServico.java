package com.fiap.techchallenge.os.ordemservico.domain;

import com.fiap.techchallenge.os.cliente.domain.Cliente;
import com.fiap.techchallenge.os.veiculo.domain.Veiculo;

import com.fiap.techchallenge.os.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OrdemDeServico {

    private Long id;
    private StatusOrdemDeServico status;
    private String orcamentoId;
    private BigDecimal valorTotal;
    private int tentativasPagamento;
    private String solicitacao;
    private LocalDateTime dataHoraCriacao;
    private Cliente cliente;
    private Veiculo veiculo;

    public OrdemDeServico() {}

    public OrdemDeServico(Long id, StatusOrdemDeServico status, String orcamentoId, BigDecimal valorTotal, int tentativasPagamento, String solicitacao, LocalDateTime dataHoraCriacao, Cliente cliente, Veiculo veiculo) {
        this.id = id;
        this.status = status;
        this.orcamentoId = orcamentoId;
        this.valorTotal = valorTotal;
        this.tentativasPagamento = tentativasPagamento;
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

    public String getOrcamentoId() {
        return orcamentoId;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public int getTentativasPagamento() {
        return tentativasPagamento;
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

    public void setOrcamentoId(String orcamentoId) {
        this.orcamentoId = orcamentoId;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setTentativasPagamento(int tentativasPagamento) {
        this.tentativasPagamento = tentativasPagamento;
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

    private static final Map<StatusOrdemDeServico, Set<StatusOrdemDeServico>> TRANSICOES_PERMITIDAS = new EnumMap<>(StatusOrdemDeServico.class);

    static {
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.RECEBIDA, EnumSet.of(StatusOrdemDeServico.AGUARDANDO_APROVACAO, StatusOrdemDeServico.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.EM_DIAGNOSTICO, EnumSet.of(StatusOrdemDeServico.AGUARDANDO_APROVACAO, StatusOrdemDeServico.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.AGUARDANDO_APROVACAO, EnumSet.of(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO, StatusOrdemDeServico.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO, EnumSet.of(StatusOrdemDeServico.APROVADA, StatusOrdemDeServico.AGUARDANDO_APROVACAO, StatusOrdemDeServico.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.APROVADA, EnumSet.of(StatusOrdemDeServico.EM_EXECUCAO, StatusOrdemDeServico.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.EM_EXECUCAO, EnumSet.of(StatusOrdemDeServico.FINALIZADA, StatusOrdemDeServico.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.FINALIZADA, EnumSet.of(StatusOrdemDeServico.ENTREGUE));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.ENTREGUE, EnumSet.noneOf(StatusOrdemDeServico.class));
        TRANSICOES_PERMITIDAS.put(StatusOrdemDeServico.CANCELADA, EnumSet.noneOf(StatusOrdemDeServico.class));
    }

    public record ValidacaoTransicaoDeStatus(boolean permitida, String erroDeValidacao) {}

    public ValidacaoTransicaoDeStatus podeTransicionarParaStatus(StatusOrdemDeServico novoStatus) {
        Set<StatusOrdemDeServico> destinos = TRANSICOES_PERMITIDAS.getOrDefault(this.status, Set.of());
        boolean permitida = destinos.contains(novoStatus);
        return new ValidacaoTransicaoDeStatus(permitida, permitida
                ? null
                : "Erro: A Ordem de Serviço %d no status '%s' não pode ir para o status '%s'!".formatted(this.id, this.status, novoStatus));
    }

    public boolean valorTotalMaiorQueZero() {
        return this.valorTotal != null && this.valorTotal.compareTo(BigDecimal.ZERO) > 0;
    }

    public void registrarOrcamento(String orcamentoId, BigDecimal valorTotal) {
        if (orcamentoId == null || orcamentoId.isBlank()) {
            throw new BadRequestException("Erro: O identificador do orçamento da Ordem de Serviço %d é obrigatório!".formatted(this.id));
        }
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Erro: O valor total da Ordem de Serviço %d deve ser maior que zero!".formatted(this.id));
        }
        exigirStatusEm(EnumSet.of(StatusOrdemDeServico.RECEBIDA, StatusOrdemDeServico.EM_DIAGNOSTICO));
        transicionarPara(StatusOrdemDeServico.AGUARDANDO_APROVACAO);
        this.orcamentoId = orcamentoId;
        this.valorTotal = valorTotal;
    }

    public void aprovarOrcamento() {
        if (this.orcamentoId == null) {
            throw new IllegalStateException("Erro: A Ordem de Serviço %d não possui orçamento registrado!".formatted(this.id));
        }
        transicionarPara(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);
    }

    public void rejeitarOrcamento() {
        exigirStatusEm(EnumSet.of(StatusOrdemDeServico.AGUARDANDO_APROVACAO, StatusOrdemDeServico.AGUARDANDO_PAGAMENTO));
        transicionarPara(StatusOrdemDeServico.CANCELADA);
    }

    public void confirmarPagamento() {
        transicionarPara(StatusOrdemDeServico.APROVADA);
        this.tentativasPagamento = 0;
    }

    public void registrarFalhaPagamento(int maxTentativas) {
        if (!StatusOrdemDeServico.AGUARDANDO_PAGAMENTO.equals(this.status)) {
            throw new BadRequestException("Erro: A Ordem de Serviço %d deve estar no status '%s'!".formatted(this.id, StatusOrdemDeServico.AGUARDANDO_PAGAMENTO));
        }
        this.tentativasPagamento++;
        transicionarPara(this.tentativasPagamento >= maxTentativas
                ? StatusOrdemDeServico.CANCELADA
                : StatusOrdemDeServico.AGUARDANDO_APROVACAO);
    }

    public void iniciarExecucao() {
        transicionarPara(StatusOrdemDeServico.EM_EXECUCAO);
    }

    public void finalizarExecucao() {
        transicionarPara(StatusOrdemDeServico.FINALIZADA);
    }

    public void falharExecucao() {
        exigirStatusEm(EnumSet.of(StatusOrdemDeServico.APROVADA, StatusOrdemDeServico.EM_EXECUCAO));
        transicionarPara(StatusOrdemDeServico.CANCELADA);
    }

    public void cancelar() {
        exigirStatusEm(EnumSet.of(StatusOrdemDeServico.RECEBIDA, StatusOrdemDeServico.EM_DIAGNOSTICO,
                StatusOrdemDeServico.AGUARDANDO_APROVACAO, StatusOrdemDeServico.AGUARDANDO_PAGAMENTO));
        transicionarPara(StatusOrdemDeServico.CANCELADA);
    }

    public void entregar() {
        transicionarPara(StatusOrdemDeServico.ENTREGUE);
    }

    private void exigirStatusEm(Set<StatusOrdemDeServico> permitidos) {
        if (!permitidos.contains(this.status)) {
            throw new BadRequestException("Erro: A Ordem de Serviço %d deve estar em um dos status %s!".formatted(this.id, permitidos));
        }
    }

    private void transicionarPara(StatusOrdemDeServico novoStatus) {
        ValidacaoTransicaoDeStatus validacao = podeTransicionarParaStatus(novoStatus);
        if (!validacao.permitida()) {
            throw new BadRequestException(validacao.erroDeValidacao());
        }
        this.status = novoStatus;
    }
}
