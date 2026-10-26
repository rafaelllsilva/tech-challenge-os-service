package com.fiap.techchallenge.veiculo.adapter.controller;

import com.fiap.techchallenge.veiculo.adapter.controller.dto.VeiculoResponse;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoWebMapperTest {

    private final VeiculoWebMapper mapper = new VeiculoWebMapper();

    @Test
    void toResponse_mapsAllFields() {
        Veiculo veiculo = new Veiculo(1L, "ABC1234", "Toyota", "Corolla", 2020);

        VeiculoResponse response = mapper.toResponse(veiculo);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPlaca()).isEqualTo("ABC1234");
        assertThat(response.getMarca()).isEqualTo("Toyota");
        assertThat(response.getModelo()).isEqualTo("Corolla");
        assertThat(response.getAno()).isEqualTo(2020);
    }
}
