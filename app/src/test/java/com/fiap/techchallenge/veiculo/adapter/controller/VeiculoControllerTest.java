package com.fiap.techchallenge.veiculo.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.CreateVeiculoRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.UpdateVeiculoByIdRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.UpdateVeiculoByPlacaRequest;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import com.fiap.techchallenge.veiculo.usecase.VeiculoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VeiculoController.class)
@Import({com.fiap.techchallenge.security.TestSecurityConfig.class, VeiculoWebMapper.class})
@WithMockUser(roles = "GERENTE")
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VeiculoUseCase veiculoUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Veiculo sampleVeiculo() {
        return new Veiculo(1L, "ABC1234", "Toyota", "Corolla", 2020);
    }

    @Test
    void getAll_returnsAllVeiculos() throws Exception {
        when(veiculoUseCase.getAll()).thenReturn(List.of(sampleVeiculo()));

        mockMvc.perform(get("/api/veiculo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placa").value("ABC1234"));
    }

    @Test
    void getAll_withPlaca_found_returnsSingletonList() throws Exception {
        when(veiculoUseCase.getByPlaca("ABC1234")).thenReturn(Optional.of(sampleVeiculo()));

        mockMvc.perform(get("/api/veiculo").param("placa", "ABC1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placa").value("ABC1234"));
    }

    @Test
    void getAll_withPlaca_notFound_returnsEmptyList() throws Exception {
        when(veiculoUseCase.getByPlaca("ZZZ9999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/veiculo").param("placa", "ZZZ9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found_returns200() throws Exception {
        when(veiculoUseCase.getById(1L)).thenReturn(Optional.of(sampleVeiculo()));

        mockMvc.perform(get("/api/veiculo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(veiculoUseCase.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/veiculo/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        CreateVeiculoRequest request = new CreateVeiculoRequest();
        request.setPlaca("ABC1234");
        request.setMarca("Toyota");
        request.setModelo("Corolla");
        request.setAno(2020);

        when(veiculoUseCase.create(any(), any(), any(), any())).thenReturn(sampleVeiculo());

        mockMvc.perform(post("/api/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_invalidPlaca_returns400() throws Exception {
        CreateVeiculoRequest request = new CreateVeiculoRequest();
        request.setPlaca("invalid");
        request.setMarca("Toyota");
        request.setModelo("Corolla");
        request.setAno(2020);

        mockMvc.perform(post("/api/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateById_found_returns200() throws Exception {
        UpdateVeiculoByIdRequest request = new UpdateVeiculoByIdRequest();
        request.setMarca("Honda");

        when(veiculoUseCase.updateById(eq(1L), any(), any(), any(), any())).thenReturn(sampleVeiculo());

        mockMvc.perform(put("/api/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateById_notFound_returns404() throws Exception {
        when(veiculoUseCase.updateById(eq(99L), any(), any(), any(), any()))
                .thenThrow(new NotFoundException("Veiculo com id 99 não encontrado"));

        mockMvc.perform(put("/api/veiculo/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateByPlaca_found_returns200() throws Exception {
        UpdateVeiculoByPlacaRequest request = new UpdateVeiculoByPlacaRequest();
        request.setMarca("Ford");

        when(veiculoUseCase.updateByPlaca(eq("ABC1234"), any(), any(), any())).thenReturn(sampleVeiculo());

        mockMvc.perform(put("/api/veiculo")
                        .param("placa", "ABC1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateByPlaca_notFound_returns404() throws Exception {
        when(veiculoUseCase.updateByPlaca(eq("ZZZ9999"), any(), any(), any()))
                .thenThrow(new NotFoundException("Veiculo com placa ZZZ9999 não encontrado"));

        mockMvc.perform(put("/api/veiculo")
                        .param("placa", "ZZZ9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_found_returns200() throws Exception {
        when(veiculoUseCase.deleteById(1L)).thenReturn(sampleVeiculo());

        mockMvc.perform(delete("/api/veiculo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteById_notFound_returns404() throws Exception {
        when(veiculoUseCase.deleteById(99L))
                .thenThrow(new NotFoundException("Veiculo com id 99 não encontrado"));

        mockMvc.perform(delete("/api/veiculo/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteByPlaca_found_returns200() throws Exception {
        when(veiculoUseCase.deleteByPlaca("ABC1234")).thenReturn(sampleVeiculo());

        mockMvc.perform(delete("/api/veiculo").param("placa", "ABC1234"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteByPlaca_notFound_returns404() throws Exception {
        when(veiculoUseCase.deleteByPlaca("ZZZ9999"))
                .thenThrow(new NotFoundException("Veiculo com placa ZZZ9999 não encontrado"));

        mockMvc.perform(delete("/api/veiculo").param("placa", "ZZZ9999"))
                .andExpect(status().isNotFound());
    }
}
