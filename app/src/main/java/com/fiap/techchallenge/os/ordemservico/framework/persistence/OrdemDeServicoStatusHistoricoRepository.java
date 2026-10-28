package com.fiap.techchallenge.os.ordemservico.framework.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdemDeServicoStatusHistoricoRepository extends JpaRepository<OrdemDeServicoStatusHistoricoEntity, Long> {

    Optional<OrdemDeServicoStatusHistoricoEntity> findTopByIdOrdemDeServicoOrderByAlteradoEmDescIdDesc(Long idOrdemDeServico);

    List<OrdemDeServicoStatusHistoricoEntity> findByIdOrdemDeServico(Long idOrdemDeServico);
}
