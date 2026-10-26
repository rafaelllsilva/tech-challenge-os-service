package com.fiap.techchallenge.cliente.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.techchallenge.cliente.adapter.controller.ClienteController;
import com.fiap.techchallenge.cliente.adapter.controller.ClienteMapper;
import com.fiap.techchallenge.cliente.adapter.controller.dto.ClienteResponse;
import com.fiap.techchallenge.cliente.adapter.controller.dto.CreateClienteRequest;
import com.fiap.techchallenge.cliente.adapter.controller.dto.UpdateClienteRequest;
import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import com.fiap.techchallenge.cliente.usecase.ClienteUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import({com.fiap.techchallenge.security.TestSecurityConfig.class, ClienteMapper.class})
@WithMockUser(roles = "GERENTE")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteUseCase clienteUseCase;

    @Autowired
    private ClienteMapper clienteWebMapper;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para LocalDate
    }

    private Cliente exemploCliente() {
        return new Cliente(1L, "Maria Silva", TipoPessoa.PF, "52998224725", LocalDate.of(1990, 5, 15), null);
    }

    @Test
    void createCliente_payloadValido_retorna201EDto() throws Exception {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setNome("Maria Silva");
        request.setTipoPessoa(TipoPessoa.PF);
        request.setDocumento("52998224725");
        request.setDataNascimento(LocalDate.of(1990, 5, 15));

        Cliente cliente = exemploCliente();
        when(clienteUseCase.create(any(), any(), any(), any(), any())).thenReturn(cliente);

        mockMvc.perform(post("/api/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "1"));
    }

    @Test
    void getAllClientes_retornaLista200() throws Exception {
        when(clienteUseCase.getAll()).thenReturn(List.of(exemploCliente()));

        mockMvc.perform(get("/api/cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"));
    }

    @Test
    void getClienteById_existente_retorna200() throws Exception {
        when(clienteUseCase.getById(1L)).thenReturn(Optional.of(exemploCliente()));

        mockMvc.perform(get("/api/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void getClienteById_inexistente_retorna404() throws Exception {
        when(clienteUseCase.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cliente/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getClienteByDocumento_existente_retorna200() throws Exception {
        when(clienteUseCase.getByDocumento("52998224725")).thenReturn(Optional.of(exemploCliente()));

        mockMvc.perform(get("/api/cliente").param("documento", "52998224725"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"));
    }

    @Test
    void getClienteByDocumento_inexistente_retorna404() throws Exception {
        when(clienteUseCase.getByDocumento("000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cliente").param("documento", "000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCliente_payloadValido_retorna200() throws Exception {
        UpdateClienteRequest request = new UpdateClienteRequest();
        request.setNome("Maria Silva Atualizada");
        
        Cliente atualizado = new Cliente(1L, "Maria Silva Atualizada", TipoPessoa.PF, "52998224725", LocalDate.of(1990, 5, 15), null);

        when(clienteUseCase.updateById(eq(1L), eq("Maria Silva Atualizada"), any(), any(), any(), any())).thenReturn(atualizado);

        mockMvc.perform(put("/api/cliente/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva Atualizada"));
    }

    @Test
    void deleteCliente_existente_retorna200() throws Exception {
        when(clienteUseCase.deleteById(1L)).thenReturn(exemploCliente());

        mockMvc.perform(delete("/api/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }
}
