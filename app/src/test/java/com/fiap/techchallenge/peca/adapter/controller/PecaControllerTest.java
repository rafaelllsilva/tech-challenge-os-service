package com.fiap.techchallenge.peca.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.peca.adapter.controller.dto.PecaResponse;
import com.fiap.techchallenge.peca.adapter.controller.dto.CreatePecaRequest;
import com.fiap.techchallenge.peca.adapter.controller.dto.UpdatePecaRequest;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.peca.usecase.PecaUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PecaController.class)
@Import({com.fiap.techchallenge.security.TestSecurityConfig.class, PecaMapper.class})
@WithMockUser(roles = "GERENTE")
public class PecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    @MockitoBean
    private PecaUseCase pecaUseCase;

    private static final String CONTROLLER_PATH = "/api/peca/";

    private Peca criarPeca(Long id) {
        TipoPeca tp = new TipoPeca();
        tp.setId(2L);
        OrdemDeServico os = new OrdemDeServico();
        os.setId(1L);
        Peca peca = new Peca();
        peca.setId(id);
        peca.setOrdemDeServico(os);
        peca.setTipoPeca(tp);
        peca.setQuantidade(3);
        return peca;
    }

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void buscarPecaPorId_quandoExiste_returnsOk() throws Exception {
        var id = 1L;
        var peca = criarPeca(id);
        var url = CONTROLLER_PATH + "{id}";

        when(pecaUseCase.getPeca(id)).thenReturn(Optional.of(peca));

        mockMvc.perform(get(url, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void buscarPecaPorId_quandoNaoExiste_returnsNotFound() throws Exception {
        var url = CONTROLLER_PATH + "{id}";

        when(pecaUseCase.getPeca(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(url, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void buscarPecas_quandoExistem_returnsOk() throws Exception {
        when(pecaUseCase.getAllPecas()).thenReturn(List.of(criarPeca(1L)));

        mockMvc.perform(get(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void buscarPecas_quandoNaoExistem_returnsOkListaVazia() throws Exception {
        when(pecaUseCase.getAllPecas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void criarPeca_quandoRequestValida_returnsCreated() throws Exception {
        var request = new CreatePecaRequest();
        request.setIdOs(1);
        request.setIdTipoPeca(2);
        request.setQuantidade(3);

        when(pecaUseCase.createPeca(any(), any(), any())).thenReturn(criarPeca(1L));

        mockMvc.perform(post(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void atualizarPeca_quandoExiste_returnsOk() throws Exception {
        var id = 1L;
        var request = new UpdatePecaRequest();
        request.setQuantidade(10);
        var url = CONTROLLER_PATH + "{id}";

        when(pecaUseCase.updatePeca(eq(id), any(), any(), any())).thenReturn(criarPeca(id));

        mockMvc.perform(put(url, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void atualizarPeca_quandoNaoExiste_returnsNotFound() throws Exception {
        var request = new UpdatePecaRequest();
        request.setQuantidade(10);
        var url = CONTROLLER_PATH + "{id}";

        when(pecaUseCase.updatePeca(eq(1L), any(), any(), any())).thenThrow(new com.fiap.techchallenge.exception.NotFoundException("Not found"));

        mockMvc.perform(put(url, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deletarPeca_quandoExiste_returnsOk() throws Exception {
        var id = 1L;
        var url = CONTROLLER_PATH + "{id}";

        when(pecaUseCase.deletePeca(id)).thenReturn(criarPeca(id));

        mockMvc.perform(delete(url, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void deletarPeca_quandoNaoExiste_returnsNotFound() throws Exception {
        var url = CONTROLLER_PATH + "{id}";

        when(pecaUseCase.deletePeca(1L)).thenThrow(new com.fiap.techchallenge.exception.NotFoundException("Not found"));

        mockMvc.perform(delete(url, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
