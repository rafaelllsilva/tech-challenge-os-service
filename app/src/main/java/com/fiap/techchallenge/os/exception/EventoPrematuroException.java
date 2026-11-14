package com.fiap.techchallenge.os.exception;

/**
 * O evento chegou antes do pré-requisito (ex.: EXECUCAO_FALHOU com a OS ainda aguardando pagamento).
 * Não é erro de negócio: a mensagem não é confirmada e o SQS a reentrega.
 */
public class EventoPrematuroException extends RuntimeException {

    public EventoPrematuroException(String message) {
        super(message);
    }
}
