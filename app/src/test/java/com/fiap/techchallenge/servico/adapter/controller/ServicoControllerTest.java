package com.fiap.techchallenge.servico.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.exception.ServicoBadStatusException;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestCreateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestUpdateDTO;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.ServicoUseCase;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
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

@WebMvcTest(ServicoController.class)
@Import(com.fiap.techchallenge.security.TestSecurityConfig.class)
@WithMockUser(roles = "GERENTE")
@TestPropertySource(properties = {"spring.docker.compose.skip.in-tests=true"})
class ServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicoUseCase servicoUseCase;

    private static ObjectMapper objectMapper;

    private Servico servico;
    private ServicoRequestCreateDTO createDTO;
    private ServicoRequestUpdateDTO updateDTO;
    private TipoServico tipoServico;
    private OrdemDeServico ordemDeServico;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(1L);

        servico = new Servico(1L, tipoServico, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO, null, null);

        createDTO = ServicoRequestCreateDTO.builder()
                .tipoServicoId(1L)
                .ordemDeServicoId(1L)
                .build();

        updateDTO = ServicoRequestUpdateDTO.builder()
                .tipoServicoId(2L)
                .build();
    }

    @Test
    void testCriaServico_sucesso() throws Exception {
        when(servicoUseCase.creteServico(any())).thenReturn(servico);

        mockMvc.perform(post("/api/servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_INICIO")))
                .andExpect(jsonPath("$.tipoServico.id", is(1)));

        verify(servicoUseCase, times(1)).creteServico(any());
    }

    @Test
    void testCriaServico_statusInvalido_retorna409() throws Exception {
        when(servicoUseCase.creteServico(any()))
                .thenThrow(new ServicoBadStatusException("OS não permite adicionar serviços"));

        mockMvc.perform(post("/api/servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());

        verify(servicoUseCase, times(1)).creteServico(any());
    }

    @Test
    void testGetServicos_semFiltro_retornaTodos() throws Exception {
        Servico servico2 = new Servico(2L, tipoServico, ordemDeServico, ServicoStatus.EM_EXECUCAO, null, null);
        when(servicoUseCase.listaServicos(null)).thenReturn(List.of(servico, servico2));

        mockMvc.perform(get("/api/servico")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(servicoUseCase, times(1)).listaServicos(null);
    }

    @Test
    void testGetServicos_comFiltroOs_retornaFiltrado() throws Exception {
        when(servicoUseCase.listaServicos(1L)).thenReturn(List.of(servico));

        mockMvc.perform(get("/api/servico")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(servicoUseCase, times(1)).listaServicos(1L);
    }

    @Test
    void testGetServicoById_sucesso() throws Exception {
        when(servicoUseCase.getServicoById(1L)).thenReturn(servico);

        mockMvc.perform(get("/api/servico/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_INICIO")));

        verify(servicoUseCase, times(1)).getServicoById(1L);
    }

    @Test
    void testGetServicoById_naoEncontrado_retorna404() throws Exception {
        when(servicoUseCase.getServicoById(999L))
                .thenThrow(new NotFoundException("Serviço não encontrado para o id: 999"));

        mockMvc.perform(get("/api/servico/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(servicoUseCase, times(1)).getServicoById(999L);
    }

    @Test
    void testDeleteServico_sucesso() throws Exception {
        doNothing().when(servicoUseCase).deleteServico(1L);

        mockMvc.perform(delete("/api/servico/1"))
                .andExpect(status().isNoContent());

        verify(servicoUseCase, times(1)).deleteServico(1L);
    }

    @Test
    void testDeleteServico_naoEncontrado_retorna404() throws Exception {
        doThrow(new NotFoundException("Serviço não encontrado para o id: 999"))
                .when(servicoUseCase).deleteServico(999L);

        mockMvc.perform(delete("/api/servico/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteServico_statusInvalido_retorna409() throws Exception {
        doThrow(new ServicoBadStatusException("Não pode deletar em execução"))
                .when(servicoUseCase).deleteServico(1L);

        mockMvc.perform(delete("/api/servico/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateServico_sucesso() throws Exception {
        TipoServico tipoAtualizado = new TipoServico(2L, "Alinhamento", new BigDecimal("200.00"));
        Servico servicoAtualizado = new Servico(1L, tipoAtualizado, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO, null, null);

        when(servicoUseCase.updateServico(anyLong(), any())).thenReturn(servicoAtualizado);

        mockMvc.perform(put("/api/servico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tipoServico.id", is(2)));

        verify(servicoUseCase, times(1)).updateServico(anyLong(), any());
    }

    @Test
    void testUpdateServico_naoEncontrado_retorna404() throws Exception {
        doThrow(new NotFoundException("Serviço não encontrado para o id: 999"))
                .when(servicoUseCase).updateServico(anyLong(), any());

        mockMvc.perform(put("/api/servico/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateServico_statusInvalido_retorna409() throws Exception {
        doThrow(new ServicoBadStatusException("Não pode atualizar finalizado"))
                .when(servicoUseCase).updateServico(anyLong(), any());

        mockMvc.perform(put("/api/servico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict());
    }
}
