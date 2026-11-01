package com.fiap.techchallenge.os.ordemservico.domain;

import java.time.Instant;
import java.util.UUID;

/** Item da linha do tempo de uma OS: mudança de status ou evento da saga (publicado ou recebido). */
public record ItemHistorico(TipoItemHistorico tipo, String nome, Instant ocorridoEm, UUID eventId, String detalhes) {

    public enum TipoItemHistorico {
        STATUS,
        EVENTO_PUBLICADO,
        EVENTO_RECEBIDO
    }
}
