package com.fiap.techchallenge.os.framework.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemDeServicoRepository extends JpaRepository<OrdemDeServicoEntity, Long> {

    List<OrdemDeServicoEntity> findAllByVeiculoId(Long veiculoId);

    List<OrdemDeServicoEntity> findAllByClienteId(Long clienteId);
}
