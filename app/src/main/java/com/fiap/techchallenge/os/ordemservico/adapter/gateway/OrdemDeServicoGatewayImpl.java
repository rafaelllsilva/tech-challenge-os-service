package com.fiap.techchallenge.os.ordemservico.adapter.gateway;

import com.fiap.techchallenge.os.cliente.adapter.gateway.ClienteGatewayImpl;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.os.ordemservico.framework.persistence.OrdemDeServicoRepository;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoGateway;
import com.fiap.techchallenge.os.veiculo.adapter.gateway.VeiculoGatewayImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrdemDeServicoGatewayImpl implements OrdemDeServicoGateway {

    private final OrdemDeServicoRepository repository;

    @Autowired
    public OrdemDeServicoGatewayImpl(OrdemDeServicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<OrdemDeServico> findAllOrdensDeServico() {
        List<OrdemDeServicoEntity> osJpaList = this.repository.findAll();
        return !osJpaList.isEmpty() ? osJpaList.stream().map(OrdemDeServicoGatewayImpl::toDomain).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public Optional<OrdemDeServico> findOrdemDeServicoById(Long id) {
        Optional<OrdemDeServicoEntity> optOsJpa = this.repository.findById(id);
        return optOsJpa.map(OrdemDeServicoGatewayImpl::toDomain);
    }

    @Override
    public List<OrdemDeServico> findAllOrdensDeServicoClienteId(Long clienteId) {
        List<OrdemDeServicoEntity> osJpaList = this.repository.findAllByClienteId(clienteId);
        return !osJpaList.isEmpty() ? osJpaList.stream().map(OrdemDeServicoGatewayImpl::toDomain).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public List<OrdemDeServico> findAllOrdensDeServicoVeiculoId(Long veiculoId) {
        List<OrdemDeServicoEntity> osJpaList = this.repository.findAllByVeiculoId(veiculoId);
        return !osJpaList.isEmpty() ? osJpaList.stream().map(OrdemDeServicoGatewayImpl::toDomain).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public OrdemDeServico saveOrdemDeServico(OrdemDeServico os) {
        OrdemDeServicoEntity novaOsJpa = OrdemDeServicoGatewayImpl.toEntity(os);
        novaOsJpa = this.repository.save(novaOsJpa);
        return OrdemDeServicoGatewayImpl.toDomain(novaOsJpa);
    }

    @Override
    public OrdemDeServico updateOrdemDeServico(OrdemDeServico os) {
        OrdemDeServicoEntity osJpaEditada = this.repository.findById(os.getId()).orElseThrow();
        osJpaEditada.setStatus(os.getStatus());
        osJpaEditada.setOrcamento(os.getOrcamento());
        osJpaEditada.setSolicitacao(os.getSolicitacao());
        osJpaEditada.setCliente(os.getCliente() != null ? ClienteGatewayImpl.toEntity(os.getCliente()) : null);
        osJpaEditada.setVeiculo(os.getVeiculo() != null ? VeiculoGatewayImpl.toEntity(os.getVeiculo()) : null);

        osJpaEditada = this.repository.save(osJpaEditada);
        return OrdemDeServicoGatewayImpl.toDomain(osJpaEditada);
    }

    @Override
    public OrdemDeServico deleteOrdemDeServico(OrdemDeServico os) {
        Optional<OrdemDeServicoEntity> optOsJpa = this.repository.findById(os.getId());
        if (optOsJpa.isPresent()) {
            var osJpa = optOsJpa.get();
            this.repository.delete(osJpa);
            return OrdemDeServicoGatewayImpl.toDomain(osJpa);
        }
        return null;
    }

    public static OrdemDeServico toDomain(OrdemDeServicoEntity entity) {
        return entity != null
                ? new OrdemDeServico(
                    entity.getId(),
                    entity.getStatus(),
                    entity.getOrcamento(),
                    entity.getSolicitacao(),
                    entity.getDataHoraCriacao(),
                    entity.getCliente() != null ? ClienteGatewayImpl.toDomain(entity.getCliente()) : null,
                    entity.getVeiculo() != null ? VeiculoGatewayImpl.toDomain(entity.getVeiculo()) : null
                )
                : null;
    }

    public static OrdemDeServicoEntity toEntity(OrdemDeServico domain) {
        return domain != null
                ? new OrdemDeServicoEntity(
                    domain.getId(),
                    domain.getStatus(),
                    domain.getOrcamento(),
                    domain.getSolicitacao(),
                    domain.getDataHoraCriacao(),
                    domain.getCliente() != null ? ClienteGatewayImpl.toEntity(domain.getCliente()) : null,
                    domain.getVeiculo() != null ? VeiculoGatewayImpl.toEntity(domain.getVeiculo()) : null
                )
                : null;
    }
}
