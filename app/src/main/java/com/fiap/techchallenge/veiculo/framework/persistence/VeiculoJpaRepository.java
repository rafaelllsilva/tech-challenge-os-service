package com.fiap.techchallenge.veiculo.framework.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, Long> {

    Optional<VeiculoEntity> findByPlaca(String placa);
}
