package com.fiap.techchallenge.servico.adapter.gateway;

import com.fiap.techchallenge.servico.adapter.gateway.mapper.ServicoGatewayMapper;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.repository.ServicoRepository;
import com.fiap.techchallenge.servico.usecase.gateway.ServicoGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ServicoGatewayImpl implements ServicoGateway {

    private final ServicoRepository servicoRepository;

    public ServicoGatewayImpl(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public Servico save(Servico servico) {
        ServicoEntity entity = ServicoGatewayMapper.toEntity(servico);
        return ServicoGatewayMapper.toDomain(servicoRepository.save(entity));
    }

    @Override
    public Optional<Servico> findById(Long id) {
        return servicoRepository.findById(id)
                .map(ServicoGatewayMapper::toDomain);
    }

    @Override
    public List<Servico> findAll() {
        return servicoRepository.findAll().stream()
                .map(ServicoGatewayMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<List<Servico>> findAllByOsId(Long idOS) {
        List<Servico> servicos = servicoRepository.findAllByOsId(idOS).stream()
                .map(ServicoGatewayMapper::toDomain)
                .toList();
        return servicos.isEmpty() ? Optional.empty() : Optional.of(servicos);
    }

    @Override
    public boolean existsByTipoServicoId(Long idTipoServico) {
        return servicoRepository.existsByTipoServicoId(idTipoServico);
    }

    @Override
    public void delete(Long idServico) {
        servicoRepository.deleteById(idServico);
    }

}
