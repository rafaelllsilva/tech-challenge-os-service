package com.fiap.techchallenge.os.ordemservico.domain;

public enum StatusOrdemDeServico {
    RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, AGUARDANDO_PAGAMENTO, APROVADA,
    EM_EXECUCAO, FINALIZADA, ENTREGUE, CANCELADA;
}
