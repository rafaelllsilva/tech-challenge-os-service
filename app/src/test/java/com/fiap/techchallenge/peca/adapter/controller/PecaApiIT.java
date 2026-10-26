package com.fiap.techchallenge.peca.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.techchallenge.peca.adapter.controller.dto.CreatePecaRequest;
import com.fiap.techchallenge.peca.adapter.controller.dto.UpdatePecaRequest;
import com.fiap.techchallenge.peca.framework.persistence.repository.PecaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "GERENTE")
@Transactional
class PecaApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PecaRepository pecaRepository;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void listarTodos_quandoBancoComSeeds_returnsListaComPecas() throws Exception {
        mockMvc.perform(get("/api/peca/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void buscarPorId_quandoExiste_returns200() throws Exception {
        var pecaSeed = pecaRepository.findAll().get(0);

        mockMvc.perform(get("/api/peca/" + pecaSeed.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pecaSeed.getId().intValue()))
                .andExpect(jsonPath("$.quantidade").value(pecaSeed.getQuantidade()));
    }

    @Test
    void buscarPorId_quandoNaoExiste_returns404() throws Exception {
        mockMvc.perform(get("/api/peca/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_quandoIdOsNull_returns400() throws Exception {
        var request = new CreatePecaRequest();
        request.setIdTipoPeca(1);
        request.setQuantidade(5);

        mockMvc.perform(post("/api/peca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoIdTipoPecaNull_returns400() throws Exception {
        var request = new CreatePecaRequest();
        request.setIdOs(1);
        request.setQuantidade(5);

        mockMvc.perform(post("/api/peca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoQuantidadeNull_returns400() throws Exception {
        var request = new CreatePecaRequest();
        request.setIdOs(1);
        request.setIdTipoPeca(1);

        mockMvc.perform(post("/api/peca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoPayloadValido_returns201EPersiste() throws Exception {
        var request = new CreatePecaRequest();
        request.setIdOs(1);
        request.setIdTipoPeca(1);
        request.setQuantidade(5);

        MvcResult resultado = mockMvc.perform(post("/api/peca/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOs").value(1))
                .andExpect(jsonPath("$.idTipoPeca").value(1))
                .andExpect(jsonPath("$.quantidade").value(5))
                .andReturn();

        var pecaCriada = pecaRepository.findAll()
                .stream()
                .filter(p -> p.getQuantidade().equals(5))
                .filter(p -> p.getOrdemDeServico().getId().equals(1L))
                .filter(p -> p.getTipoPeca().getId().equals(1L))
                .findFirst();

        assertThat(pecaCriada).isPresent();
    }

    @Test
    void atualizar_quandoExiste_returns200EAlteracoesPersistem() throws Exception {
        var peca = pecaRepository.findAll().get(0);
        var id = peca.getId();

        var request = new UpdatePecaRequest();
        request.setQuantidade(99);

        mockMvc.perform(put("/api/peca/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(99));

        assertThat(pecaRepository.findById(id).orElseThrow().getQuantidade()).isEqualTo(99);
    }

    @Test
    void atualizar_quandoNaoExiste_returns404() throws Exception {
        var request = new UpdatePecaRequest();
        request.setQuantidade(10);

        mockMvc.perform(put("/api/peca/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletar_quandoExiste_returns200ERemove() throws Exception {
        var peca = pecaRepository.findAll().get(0);
        var id = peca.getId();

        mockMvc.perform(delete("/api/peca/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.intValue()));

        assertThat(pecaRepository.findById(id)).isEmpty();
    }

    @Test
    void deletar_quandoNaoExiste_returns404() throws Exception {
        mockMvc.perform(delete("/api/peca/999999"))
                .andExpect(status().isNotFound());
    }
}
