package com.fiap.techchallenge.os.mensageria.contrato;

import com.fiap.techchallenge.os.mensageria.contrato.payload.*;

public enum TipoEvento {
    OS_CRIADA(Topico.OS_EVENTS, OsCriadaPayload.class),
    ORCAMENTO_APROVADO(Topico.OS_EVENTS, OrcamentoAprovadoPayload.class),
    ORCAMENTO_REJEITADO(Topico.OS_EVENTS, OrcamentoRejeitadoPayload.class),
    OS_CANCELADA(Topico.OS_EVENTS, OsCanceladaPayload.class),

    ORCAMENTO_GERADO(Topico.BILLING_EVENTS, OrcamentoGeradoPayload.class),
    PAGAMENTO_CONFIRMADO(Topico.BILLING_EVENTS, PagamentoConfirmadoPayload.class),
    PAGAMENTO_FALHOU(Topico.BILLING_EVENTS, PagamentoFalhouPayload.class),
    PAGAMENTO_ESTORNADO(Topico.BILLING_EVENTS, PagamentoEstornadoPayload.class),

    DIAGNOSTICO_CONCLUIDO(Topico.EXECUTION_EVENTS, DiagnosticoConcluidoPayload.class),
    ESTOQUE_RESERVADO(Topico.EXECUTION_EVENTS, EstoqueReservadoPayload.class),
    EXECUCAO_INICIADA(Topico.EXECUTION_EVENTS, ExecucaoIniciadaPayload.class),
    EXECUCAO_FINALIZADA(Topico.EXECUTION_EVENTS, ExecucaoFinalizadaPayload.class),
    EXECUCAO_FALHOU(Topico.EXECUTION_EVENTS, ExecucaoFalhouPayload.class);

    private final Topico topico;
    private final Class<?> tipoPayload;

    TipoEvento(Topico topico, Class<?> tipoPayload) {
        this.topico = topico;
        this.tipoPayload = tipoPayload;
    }

    public Topico topico() {
        return topico;
    }

    public Class<?> tipoPayload() {
        return tipoPayload;
    }
}
