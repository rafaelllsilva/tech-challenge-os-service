package com.fiap.techchallenge.peca.adapter.gateway;

import com.fiap.techchallenge.os.adapter.gateway.OrdemDeServicoGatewayImpl;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.peca.framework.persistence.entity.PecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.repository.PecaRepository;
import com.fiap.techchallenge.peca.usecase.PecaGateway;
import com.fiap.techchallenge.peca.usecase.TipoPecaGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PecaGatewayImpl implements PecaGateway {

    private final PecaRepository repository;

    public PecaGatewayImpl(PecaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Peca> findAll() {
        return repository.findAll().stream().map(PecaGatewayImpl::toDomain).toList();
    }

    @Override
    public Optional<Peca> findById(Long id) {
        return repository.findById(id).map(PecaGatewayImpl::toDomain);
    }

    @Override
    public Peca save(Peca peca) {
        PecaEntity entity = toEntity(peca);
        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(Peca peca) {
        repository.delete(toEntity(peca));
    }

    public static Peca toDomain(PecaEntity entity) {
        Peca domain = new Peca();
        domain.setId(entity.getId());
        domain.setQuantidade(entity.getQuantidade());
        domain.setTipoPeca(TipoPecaGatewayImpl.toDomain(entity.getTipoPeca()));

        if (entity.getOrdemDeServico() != null) {
            OrdemDeServico os = new OrdemDeServico();
            os.setId(entity.getOrdemDeServico().getId());
            domain.setOrdemDeServico(os);
        }

        return domain;
    }

    public static PecaEntity toEntity(Peca domain) {
        PecaEntity entity = new PecaEntity();
        entity.setId(domain.getId());
        entity.setQuantidade(domain.getQuantidade());
        entity.setTipoPeca(TipoPecaGatewayImpl.toEntity(domain.getTipoPeca()));

        if (domain.getOrdemDeServico() != null) {
            OrdemDeServicoEntity ose = new OrdemDeServicoEntity();
            ose.setId(domain.getOrdemDeServico().getId());
            entity.setOrdemDeServico(ose);
        }

        return entity;
    }
}
