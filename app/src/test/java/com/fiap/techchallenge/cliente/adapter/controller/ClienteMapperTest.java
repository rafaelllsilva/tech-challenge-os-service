package com.fiap.techchallenge.cliente.adapter.controller;

import com.fiap.techchallenge.cliente.adapter.controller.ClienteMapper;
import com.fiap.techchallenge.cliente.adapter.controller.dto.ClienteResponse;
import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

    private final ClienteMapper mapper = new ClienteMapper();

    @Test
    void toResponse_mapeiaTodasAsPropriedades() {
        Cliente cliente = new Cliente(1L, "Maria Silva", TipoPessoa.PF, "52998224725",
                LocalDate.of(1990, 5, 15), "maria@email.com");

        ClienteResponse response = mapper.toResponse(cliente);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Maria Silva");
        assertThat(response.getTipoPessoa()).isEqualTo("PF");
        assertThat(response.getDocumento()).isEqualTo("52998224725");
        assertThat(response.getDataNascimento()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(response.getEmail()).isEqualTo("maria@email.com");
    }

    @Test
    void toResponse_mapeiaTodasAsPropriedades_PJ() {
        Cliente cliente = new Cliente(2L, "Tech Solutions Ltda", TipoPessoa.PJ,
                "11222333000181", null, "contato@tech.com");

        ClienteResponse response = mapper.toResponse(cliente);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getNome()).isEqualTo("Tech Solutions Ltda");
        assertThat(response.getTipoPessoa()).isEqualTo("PJ");
        assertThat(response.getDocumento()).isEqualTo("11222333000181");
        assertThat(response.getDataNascimento()).isNull();
        assertThat(response.getEmail()).isEqualTo("contato@tech.com");
    }

    @Test
    void toResponse_semEmail_mapeiaEmailNulo() {
        Cliente cliente = new Cliente(3L, "Ana Costa", TipoPessoa.PF, "52998224725", null, null);

        ClienteResponse response = mapper.toResponse(cliente);

        assertThat(response.getEmail()).isNull();
    }
}
