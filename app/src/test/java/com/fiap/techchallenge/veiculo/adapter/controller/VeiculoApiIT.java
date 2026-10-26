package com.fiap.techchallenge.veiculo.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.CreateVeiculoRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.UpdateVeiculoByIdRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.UpdateVeiculoByPlacaRequest;
import com.fiap.techchallenge.veiculo.framework.persistence.VeiculoJpaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "GERENTE")
@Transactional
class VeiculoApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VeiculoJpaRepository jpaRepository;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void getAll_noPlacaParam_returnsSeedList() throws Exception {
        mockMvc.perform(get("/api/veiculo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(10)));
    }

    @Test
    void getAll_withPlaca_returnsSingletonList() throws Exception {
        mockMvc.perform(get("/api/veiculo").param("placa", "ABC-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placa").value("ABC-1234"))
                .andExpect(jsonPath("$[0].marca").value("Fiat"));
    }

    @Test
    void getAll_withUnknownPlaca_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/veiculo").param("placa", "NOP3Q45"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_existingSeedId_returns200() throws Exception {
        Long anyId = jpaRepository.findByPlaca("ABC-1234").orElseThrow().getId();

        mockMvc.perform(get("/api/veiculo/" + anyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC-1234"));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/veiculo/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validPayload_returns201AndPersists() throws Exception {
        CreateVeiculoRequest request = new CreateVeiculoRequest();
        request.setPlaca("API1234");
        request.setMarca("Ford");
        request.setModelo("Ka");
        request.setAno(2022);

        mockMvc.perform(post("/api/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(jpaRepository.findByPlaca("API1234")).isPresent();
    }

    @Test
    void create_invalidPlaca_returns400() throws Exception {
        CreateVeiculoRequest request = new CreateVeiculoRequest();
        request.setPlaca("bad-plate");
        request.setMarca("Ford");
        request.setModelo("Ka");
        request.setAno(2022);

        mockMvc.perform(post("/api/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_invalidAno_returns400() throws Exception {
        CreateVeiculoRequest request = new CreateVeiculoRequest();
        request.setPlaca("API1A99");
        request.setMarca("Ford");
        request.setModelo("Ka");
        request.setAno(1800);

        mockMvc.perform(post("/api/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingRequiredField_returns400() throws Exception {
        CreateVeiculoRequest request = new CreateVeiculoRequest();
        request.setPlaca("API1A99");
        request.setModelo("Ka");
        request.setAno(2022);

        mockMvc.perform(post("/api/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateById_existingId_returns200AndPersistsChanges() throws Exception {
        Long id = jpaRepository.findByPlaca("DEF-5678").orElseThrow().getId();

        UpdateVeiculoByIdRequest request = new UpdateVeiculoByIdRequest();
        request.setMarca("Chevrolet");

        mockMvc.perform(put("/api/veiculo/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Chevrolet"));

        assertThat(jpaRepository.findById(id).orElseThrow().getMarca()).isEqualTo("Chevrolet");
    }

    @Test
    void updateById_missing_returns404() throws Exception {
        mockMvc.perform(put("/api/veiculo/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateByPlaca_existing_returns200AndPersistsChanges() throws Exception {
        UpdateVeiculoByPlacaRequest request = new UpdateVeiculoByPlacaRequest();
        request.setModelo("Mobi");
        request.setAno(2021);

        mockMvc.perform(put("/api/veiculo")
                        .param("placa", "GHI-9012")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Mobi"))
                .andExpect(jsonPath("$.ano").value(2021));

        var updated = jpaRepository.findByPlaca("GHI-9012").orElseThrow();
        assertThat(updated.getModelo()).isEqualTo("Mobi");
        assertThat(updated.getAno()).isEqualTo(2021);
    }

    @Test
    void updateByPlaca_missing_returns404() throws Exception {
        mockMvc.perform(put("/api/veiculo")
                        .param("placa", "NOP3Q45")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_existing_returns200AndRemoves() throws Exception {
        Long id = jpaRepository.findByPlaca("RIO3F45").orElseThrow().getId();

        mockMvc.perform(delete("/api/veiculo/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        assertThat(jpaRepository.findById(id)).isEmpty();
    }

    @Test
    void deleteById_missing_returns404() throws Exception {
        mockMvc.perform(delete("/api/veiculo/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteByPlaca_existing_returns200AndRemoves() throws Exception {
        mockMvc.perform(delete("/api/veiculo").param("placa", "BRA2E19"))
                .andExpect(status().isOk());

        assertThat(jpaRepository.findByPlaca("BRA2E19")).isEmpty();
    }

    @Test
    void deleteVeiculo_withActiveDependency_returns409() throws Exception {
        Long id = jpaRepository.findByPlaca("JKL-3456").orElseThrow().getId();

        mockMvc.perform(delete("/api/veiculo/" + id))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteByPlaca_missing_returns404() throws Exception {
        mockMvc.perform(delete("/api/veiculo").param("placa", "NOP3Q45"))
                .andExpect(status().isNotFound());
    }
}
