package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import com.fiap.techchallenge.os.mensageria.usecase.PublicadorDeEventos;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublicadorDeEventos implements PublicadorDeEventos {

    private final OutboxEventoRepository repository;
    private final EventoSerializer serializer;

    public OutboxPublicadorDeEventos(OutboxEventoRepository repository, EventoSerializer serializer) {
        this.repository = repository;
        this.serializer = serializer;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publicar(EventEnvelope<?> evento) {
        // Grava o envelope completo, que é o que o relay publica no SNS.
        repository.save(new OutboxEventoEntity(
                evento.eventId(),
                evento.type().name(),
                evento.type().topico().nome(),
                evento.correlationId(),
                serializer.serializar(evento),
                evento.occurredAt(),
                StatusOutbox.PENDENTE,
                0,
                null,
                null
        ));
    }
}
