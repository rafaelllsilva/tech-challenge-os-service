package com.fiap.techchallenge.servico.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.exception.TipoServicoUnableToModify;
import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoRequestDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.TipoServicoUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoServicoController.class)
@Import(com.fiap.techchallenge.security.TestSecurityConfig.class)
@WithMockUser(roles = "GERENTE")
@TestPropertySource(properties = {"spring.docker.compose.skip.in-tests=true"})
class TipoServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TipoServicoUseCase tipoServicoUseCase;

    private static ObjectMapper objectMapper;

    private TipoServico tipoServico;
    private TipoServicoRequestDTO requestDTO;
    private TempoMedioServicoDTO tempoMedioDTO1;
    private TempoMedioServicoDTO tempoMedioDTO2;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        requestDTO = TipoServicoRequestDTO.builder()
                .nome("Troca de Óleo")
                .valor(new BigDecimal("150.00"))
                .build();

        tempoMedioDTO1 = TempoMedioServicoDTO.builder()
                .nome("Troca de Óleo")
                .tempoMedioMinutos(BigDecimal.valueOf(45.0))
                .build();

        tempoMedioDTO2 = TempoMedioServicoDTO.builder()
                .nome("Alinhamento")
                .tempoMedioMinutos(BigDecimal.valueOf(30.0))
                .build();
    }

    // --- GET /api/tipo-servico/tempo-medio ---

    @Test
    void testGetTempoMedio_semFiltro_retornaTodos() throws Exception {
        when(tipoServicoUseCase.getTempoMedio(null)).thenReturn(List.of(tempoMedioDTO1, tempoMedioDTO2));

        mockMvc.perform(get("/api/tipo-servico/tempo-medio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Troca de Óleo")))
                .andExpect(jsonPath("$[0].tempoMedioMinutos", is(45.0)))
                .andExpect(jsonPath("$[1].nome", is("Alinhamento")))
                .andExpect(jsonPath("$[1].tempoMedioMinutos", is(30.0)));

        verify(tipoServicoUseCase, times(1)).getTempoMedio(null);
    }

    @Test
    void testGetTempoMedio_comId_retornaUnico() throws Exception {
        when(tipoServicoUseCase.getTempoMedio(1L)).thenReturn(List.of(tempoMedioDTO1));

        mockMvc.perform(get("/api/tipo-servico/tempo-medio")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Troca de Óleo")));

        verify(tipoServicoUseCase, times(1)).getTempoMedio(1L);
    }

    @Test
    void testGetTempoMedio_listaVazia() throws Exception {
        when(tipoServicoUseCase.getTempoMedio(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/tipo-servico/tempo-medio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetTempoMedio_idNaoEncontrado_retorna404() throws Exception {
        when(tipoServicoUseCase.getTempoMedio(999L))
                .thenThrow(new NotFoundException("Tipo de serviço não encontrado com ID: 999"));

        mockMvc.perform(get("/api/tipo-servico/tempo-medio")
                        .param("id", "999"))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/tipo-servico ---

    @Test
    void testGetAll_retornaLista() throws Exception {
        TipoServico outro = new TipoServico(2L, "Alinhamento", new BigDecimal("100.00"));
        when(tipoServicoUseCase.listAll()).thenReturn(List.of(tipoServico, outro));

        mockMvc.perform(get("/api/tipo-servico")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nome", is("Troca de Óleo")))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(tipoServicoUseCase, times(1)).listAll();
    }

    @Test
    void testGetAll_listaVazia() throws Exception {
        when(tipoServicoUseCase.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/tipo-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- GET /api/tipo-servico/{id} ---

    @Test
    void testGetById_sucesso() throws Exception {
        when(tipoServicoUseCase.getById(1L)).thenReturn(tipoServico);

        mockMvc.perform(get("/api/tipo-servico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Troca de Óleo")))
                .andExpect(jsonPath("$.valor", is(150.00)));

        verify(tipoServicoUseCase, times(1)).getById(1L);
    }

    @Test
    void testGetById_naoEncontrado_retorna404() throws Exception {
        when(tipoServicoUseCase.getById(999L))
                .thenThrow(new NotFoundException("Tipo de serviço não encontrado com ID: 999"));

        mockMvc.perform(get("/api/tipo-servico/999"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/tipo-servico ---

    @Test
    void testCreate_sucesso() throws Exception {
        when(tipoServicoUseCase.create(any())).thenReturn(tipoServico);

        mockMvc.perform(post("/api/tipo-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Troca de Óleo")));

        verify(tipoServicoUseCase, times(1)).create(any());
    }

    // --- PUT /api/tipo-servico/{id} ---

    @Test
    void testUpdate_sucesso() throws Exception {
        TipoServico atualizado = new TipoServico(1L, "Troca de Óleo Atualizado", new BigDecimal("180.00"));
        when(tipoServicoUseCase.update(anyLong(), any())).thenReturn(atualizado);

        mockMvc.perform(put("/api/tipo-servico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Troca de Óleo Atualizado")));

        verify(tipoServicoUseCase, times(1)).update(anyLong(), any());
    }

    @Test
    void testUpdate_naoEncontrado_retorna404() throws Exception {
        when(tipoServicoUseCase.update(anyLong(), any()))
                .thenThrow(new NotFoundException("Tipo de serviço não encontrado com ID: 999"));

        mockMvc.perform(put("/api/tipo-servico/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/tipo-servico/{id} ---

    @Test
    void testDelete_sucesso() throws Exception {
        doNothing().when(tipoServicoUseCase).delete(1L);

        mockMvc.perform(delete("/api/tipo-servico/1"))
                .andExpect(status().isNoContent());

        verify(tipoServicoUseCase, times(1)).delete(1L);
    }

    @Test
    void testDelete_naoEncontrado_retorna404() throws Exception {
        doThrow(new NotFoundException("Tipo de serviço não encontrado com ID: 999"))
                .when(tipoServicoUseCase).delete(999L);

        mockMvc.perform(delete("/api/tipo-servico/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_comServicosAssociados_retorna409() throws Exception {
        doThrow(new TipoServicoUnableToModify("Não é possível excluir o tipo de serviço, pois existem serviços associados a ele."))
                .when(tipoServicoUseCase).delete(1L);

        mockMvc.perform(delete("/api/tipo-servico/1"))
                .andExpect(status().isConflict());
    }
}
