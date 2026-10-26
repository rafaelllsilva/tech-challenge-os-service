package com.fiap.techchallenge.os.adapter.gateway;

import com.fiap.techchallenge.cliente.adapter.gateway.ClienteGatewayImpl;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoRepository;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoGateway;
import com.fiap.techchallenge.peca.adapter.gateway.PecaGatewayImpl;
import com.fiap.techchallenge.servico.adapter.gateway.mapper.ServicoGatewayMapper;
import com.fiap.techchallenge.veiculo.adapter.gateway.VeiculoGatewayImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

        if (os.getPecas() != null && !os.getPecas().isEmpty()) {
            var pecasJpa = os.getPecas().stream().map(PecaGatewayImpl::toEntity).collect(Collectors.toList());
            OrdemDeServicoEntity finalOsJpaEditada = osJpaEditada;
            pecasJpa.forEach(p -> p.setOrdemDeServico(finalOsJpaEditada));

            if (osJpaEditada.getPecas() == null) {
                osJpaEditada.setPecas(new ArrayList<>());
            } else {
                osJpaEditada.getPecas().clear();
            }

            osJpaEditada.getPecas().addAll(pecasJpa);
        } else if ((osJpaEditada.getPecas() != null && !osJpaEditada.getPecas().isEmpty()) && (os.getPecas() == null || os.getPecas().isEmpty())) {
            osJpaEditada.getPecas().clear();
        }

        if (os.getServicos() != null && !os.getServicos().isEmpty()) {
            var servicosJpa = os.getServicos().stream().map(ServicoGatewayMapper::toEntity).collect(Collectors.toList());
            OrdemDeServicoEntity finalOsJpaEditada1 = osJpaEditada;
            servicosJpa.forEach(s -> s.setOrdemDeServico(finalOsJpaEditada1));

            if (osJpaEditada.getServicos() == null) {
                osJpaEditada.setServicos(new ArrayList<>());
            } else {
                osJpaEditada.getServicos().clear();
            }

            osJpaEditada.getServicos().addAll(servicosJpa);
        } else if ((osJpaEditada.getServicos() != null && !osJpaEditada.getServicos().isEmpty()) && (os.getServicos() == null || os.getServicos().isEmpty())) {
            osJpaEditada.getServicos().clear();
        }

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
                    entity.getVeiculo() != null ? VeiculoGatewayImpl.toDomain(entity.getVeiculo()) : null,
                    entity.getPecas() != null && !entity.getPecas().isEmpty() ? entity.getPecas().stream().map(PecaGatewayImpl::toDomain).collect(Collectors.toList()) : null,
                    entity.getServicos() != null && !entity.getServicos().isEmpty() ? entity.getServicos().stream().map(ServicoGatewayMapper::toDomain).collect(Collectors.toList()) : null
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
                    domain.getVeiculo() != null ? VeiculoGatewayImpl.toEntity(domain.getVeiculo()) : null,
                    domain.getPecas() != null && !domain.getPecas().isEmpty() ? domain.getPecas().stream().map(PecaGatewayImpl::toEntity).collect(Collectors.toList()) : null,
                    domain.getServicos() != null && !domain.getServicos().isEmpty() ? domain.getServicos().stream().map(ServicoGatewayMapper::toEntity).collect(Collectors.toList()) : null
                )
                : null;
    }
}
