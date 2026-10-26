package com.fiap.techchallenge.peca.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.techchallenge.peca.adapter.controller.dto.CreateTipoPecaRequest;
import com.fiap.techchallenge.peca.adapter.controller.dto.UpdateTipoPecaRequest;
import com.fiap.techchallenge.peca.framework.persistence.repository.TipoPecaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "GERENTE")
@Transactional
class TipoPecaApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TipoPecaRepository tipoPecaRepository;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void listarTodos_quandoBancoComSeeds_returnsListaComTipos() throws Exception {
        mockMvc.perform(get("/api/tipoPeca/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(10)));
    }

    @Test
    void buscarPorId_quandoExiste_returns200() throws Exception {
        mockMvc.perform(get("/api/tipoPeca/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Pastilha Freio"));
    }

    @Test
    void buscarPorId_quandoNaoExiste_returns404() throws Exception {
        mockMvc.perform(get("/api/tipoPeca/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_quandoNomeNull_returns400() throws Exception {
        var request = new CreateTipoPecaRequest();
        request.setValorUnitario(BigDecimal.valueOf(99.99));
        request.setQuantidadeEstoque(10);

        mockMvc.perform(post("/api/tipoPeca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoValorUnitarioNull_returns400() throws Exception {
        var request = new CreateTipoPecaRequest();
        request.setNome("Peça Teste");
        request.setQuantidadeEstoque(10);

        mockMvc.perform(post("/api/tipoPeca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoQuantidadeEstoqueNull_returns400() throws Exception {
        var request = new CreateTipoPecaRequest();
        request.setNome("Peça Teste");
        request.setValorUnitario(BigDecimal.valueOf(99.99));

        mockMvc.perform(post("/api/tipoPeca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoPayloadValido_returns201EPersiste() throws Exception {
        var request = new CreateTipoPecaRequest();
        request.setNome("Peça de Teste Integração");
        request.setValorUnitario(BigDecimal.valueOf(99.99));
        request.setQuantidadeEstoque(10);

        mockMvc.perform(post("/api/tipoPeca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var tipoPecaCriado = tipoPecaRepository.findAll()
                .stream()
                .filter(tp -> tp.getNome().equals("Peça de Teste Integração"))
                .findFirst();

        assertThat(tipoPecaCriado).isPresent();
        assertThat(tipoPecaCriado.get().getValorUnitario()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    @Test
    void atualizar_quandoExiste_returns200EAlteracoesPersistem() throws Exception {
        var id = tipoPecaRepository.findAll().get(0).getId();

        var request = new UpdateTipoPecaRequest();
        request.setNome("Nome Atualizado");

        mockMvc.perform(put("/api/tipoPeca/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));

        assertThat(tipoPecaRepository.findById(id).orElseThrow().getNome()).isEqualTo("Nome Atualizado");
    }

    @Test
    void atualizar_quandoNaoExiste_returns404() throws Exception {
        var request = new UpdateTipoPecaRequest();
        request.setNome("Nome Atualizado");

        mockMvc.perform(put("/api/tipoPeca/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletar_quandoExiste_returns200ERemove() throws Exception {
        var id = tipoPecaRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/tipoPeca/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        assertThat(tipoPecaRepository.findById(id)).isEmpty();
    }

    @Test
    void deletar_quandoNaoExiste_returns404() throws Exception {
        mockMvc.perform(delete("/api/tipoPeca/999999"))
                .andExpect(status().isNotFound());
    }
}
