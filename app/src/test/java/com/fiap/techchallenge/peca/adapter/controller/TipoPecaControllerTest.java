package com.fiap.techchallenge.peca.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.peca.adapter.controller.dto.TipoPecaResponse;
import com.fiap.techchallenge.peca.adapter.controller.dto.CreateTipoPecaRequest;
import com.fiap.techchallenge.peca.adapter.controller.dto.UpdateTipoPecaRequest;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.peca.usecase.TipoPecaUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TipoPecaController.class)
@Import({com.fiap.techchallenge.security.TestSecurityConfig.class, TipoPecaMapper.class})
@org.springframework.security.test.context.support.WithMockUser(roles = "GERENTE")
public class TipoPecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    @MockitoBean
    private TipoPecaUseCase tipoPecaUseCase;

    private static final String CONTROLLER_PATH = "/api/tipoPeca/";

    private TipoPeca criarTipoPeca(Long id) {
        TipoPeca tp = new TipoPeca();
        tp.setId(id);
        tp.setNome("Pastilha de freio");
        tp.setValorUnitario(BigDecimal.valueOf(120));
        tp.setQuantidadeEstoque(10);
        return tp;
    }

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void buscarTipoPecaPorId_quandoExiste_returnsOk() throws Exception {
        var id = 1L;
        var tipoPeca = criarTipoPeca(id);
        var url = CONTROLLER_PATH + "{id}";

        when(tipoPecaUseCase.getTipoPeca(id)).thenReturn(Optional.of(tipoPeca));

        mockMvc.perform(get(url, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void buscarTipoPecaPorId_quandoNaoExiste_returnsNotFound() throws Exception {
        var url = CONTROLLER_PATH + "{id}";

        when(tipoPecaUseCase.getTipoPeca(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(url, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void buscarTiposPeca_quandoExistem_returnsOk() throws Exception {
        when(tipoPecaUseCase.getAllTiposPeca()).thenReturn(List.of(criarTipoPeca(1L)));

        mockMvc.perform(get(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void buscarTiposPeca_quandoNaoExistem_returnsOkListaVazia() throws Exception {
        when(tipoPecaUseCase.getAllTiposPeca()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void criarTipoPeca_quandoRequestValida_returnsCreated() throws Exception {
        var request = new CreateTipoPecaRequest();
        request.setNome("Pastilha de freio");
        request.setValorUnitario(BigDecimal.valueOf(120));
        request.setQuantidadeEstoque(10);

        when(tipoPecaUseCase.createTipoPeca(any(), any(), any())).thenReturn(criarTipoPeca(1L));

        mockMvc.perform(post(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void atualizarTipoPeca_quandoExiste_returnsOk() throws Exception {
        var id = 1L;
        var request = new UpdateTipoPecaRequest();
        request.setQuantidadeEstoque(20);
        var url = CONTROLLER_PATH + "{id}";

        when(tipoPecaUseCase.updateTipoPeca(eq(id), any(), any(), any())).thenReturn(criarTipoPeca(id));

        mockMvc.perform(put(url, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void atualizarTipoPeca_quandoNaoExiste_returnsNotFound() throws Exception {
        var request = new UpdateTipoPecaRequest();
        request.setQuantidadeEstoque(20);
        var url = CONTROLLER_PATH + "{id}";

        when(tipoPecaUseCase.updateTipoPeca(eq(1L), any(), any(), any())).thenThrow(new com.fiap.techchallenge.exception.NotFoundException("Not found"));

        mockMvc.perform(put(url, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deletarTipoPeca_quandoExiste_returnsOk() throws Exception {
        var id = 1L;
        var url = CONTROLLER_PATH + "{id}";

        when(tipoPecaUseCase.deleteTipoPeca(id)).thenReturn(criarTipoPeca(id));

        mockMvc.perform(delete(url, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void deletarTipoPeca_quandoNaoExiste_returnsNotFound() throws Exception {
        var url = CONTROLLER_PATH + "{id}";

        when(tipoPecaUseCase.deleteTipoPeca(1L)).thenThrow(new com.fiap.techchallenge.exception.NotFoundException("Not found"));

        mockMvc.perform(delete(url, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
