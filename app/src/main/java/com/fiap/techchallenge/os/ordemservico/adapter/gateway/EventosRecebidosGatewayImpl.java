package com.fiap.techchallenge.os.ordemservico.adapter.gateway;

import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoRepository;
import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;
import com.fiap.techchallenge.os.ordemservico.usecase.EventosRecebidosGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventosRecebidosGatewayImpl implements EventosRecebidosGateway {

    private final EventoProcessadoRepository repository;

    public EventosRecebidosGatewayImpl(EventoProcessadoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ItemHistorico> listarPorOrdemDeServico(Long idOrdemDeServico) {
        return repository.findByCorrelationId(String.valueOf(idOrdemDeServico)).stream()
                .map(e -> new ItemHistorico(ItemHistorico.TipoItemHistorico.EVENTO_RECEBIDO, e.getTipo(),
                        e.getOcorridoEm(), e.getEventId(), e.getPayload()))
                .toList();
    }
}
