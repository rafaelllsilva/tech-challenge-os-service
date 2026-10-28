package com.fiap.techchallenge.os.ordemservico.adapter.gateway;

import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;
import com.fiap.techchallenge.os.ordemservico.usecase.EventosPublicadosGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventosPublicadosGatewayImpl implements EventosPublicadosGateway {

    private final OutboxEventoRepository repository;
    private final EventoSerializer serializer;

    public EventosPublicadosGatewayImpl(OutboxEventoRepository repository, EventoSerializer serializer) {
        this.repository = repository;
        this.serializer = serializer;
    }

    @Override
    public List<ItemHistorico> listarPorOrdemDeServico(Long idOrdemDeServico) {
        return repository.findByCorrelationId(String.valueOf(idOrdemDeServico)).stream()
                // a outbox guarda o envelope completo; o histórico mostra só o payload
                .map(e -> new ItemHistorico(ItemHistorico.TipoItemHistorico.EVENTO_PUBLICADO, e.getTipo(),
                        e.getOcorridoEm(), e.getId(),
                        serializer.serializarPayload(serializer.desserializar(e.getPayload()).payload())))
                .toList();
    }
}
