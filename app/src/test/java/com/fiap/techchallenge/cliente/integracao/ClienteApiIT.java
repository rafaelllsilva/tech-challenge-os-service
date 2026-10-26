package com.fiap.techchallenge.cliente.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.cliente.adapter.controller.dto.CreateClienteRequest;
import com.fiap.techchallenge.cliente.adapter.controller.dto.UpdateClienteRequest;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import com.fiap.techchallenge.cliente.framework.persistence.ClienteJpaRepository;
import com.fiap.techchallenge.cliente.framework.persistence.ClienteEntity;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "GERENTE") // Evita 401/403
public class ClienteApiIT {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    @Autowired
    private ClienteJpaRepository clienteRepository;

    private static final String CONTROLLER_PATH = "/api/cliente/";

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // CPFs para testes
    private static final String CPF_VALIDO_1 = "55736593084";
    private static final String CPF_VALIDO_2 = "63518099914"; // para exclusão
    private static final String CPF_INVALIDO = "00000000000";

    // CNPJs para testes
    private static final String CNPJ_NUMERICO_VALIDO = "11222333000181";
    private static final String CNPJ_NUMERICO_INVALIDO = "11222333000191";
    private static final String CNPJ_NUMERICO_COM_MASCARA = "11.222.333/0001-81";

    private static final String CNPJ_ALFANUMERICO_VALIDO = "DC123AB456FE70";
    private static final String CNPJ_ALFANUMERICO_INVALIDO = "DC123AB456FE75";
    private static final String CNPJ_ALFANUMERICO_COM_MASCARA = "DC.123.AB4/56FE-70";

    @BeforeEach
    public void setUp() {
        // Inicializações adicionais se necessário
    }

    @AfterEach
    public void tearDown() {
        // Limpezas adicionais se necessário
    }

    // ================== testes createCliente ==================

    @Test
    public void given_payloadValidoPF_when_createCliente_then_retorna201EClienteSalvo() throws Exception {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setNome("Maria Integração");
        request.setTipoPessoa(TipoPessoa.PF);
        request.setDocumento(CPF_VALIDO_1);
        request.setDataNascimento(LocalDate.of(1990, 5, 15));

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = resultado.getResponse().getHeader("Location");
        Long idCriado = Long.parseLong(location);

        // verifica banco
        Optional<ClienteEntity> clienteNoBanco = clienteRepository.findById(idCriado);
        assertThat(clienteNoBanco).isPresent();
        assertThat(clienteNoBanco.get().getNome()).isEqualTo("Maria Integração");
        assertThat(clienteNoBanco.get().getDocumento()).isEqualTo(CPF_VALIDO_1);
    }

    @Test
    public void given_cpfInvalido_when_createCliente_then_retorna400() throws Exception {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setNome("Invalido");
        request.setTipoPessoa(TipoPessoa.PF);
        request.setDocumento(CPF_INVALIDO);

        mockMvc.perform(post(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ================== testes getClienteById ==================

    @Test
    public void given_idExistente_when_getClienteById_then_retorna200EDados() throws Exception {
        Long clienteId = criarClientePFCustom("Maria Busca", CPF_VALIDO_2);

        mockMvc.perform(get(CONTROLLER_PATH + clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Busca"))
                .andExpect(jsonPath("$.documento").value(CPF_VALIDO_2));
    }

    @Test
    public void given_idInexistente_when_getClienteById_then_retorna404() throws Exception {
        mockMvc.perform(get(CONTROLLER_PATH + "999999"))
                .andExpect(status().isNotFound());
    }

    // ================== testes getClienteByDocumento ==================

    @Test
    public void given_documentoExistente_when_getClienteByDocumento_then_retorna200EDados() throws Exception {
        criarClientePFCustom("João Documento", CPF_VALIDO_1);

        mockMvc.perform(get(CONTROLLER_PATH).param("documento", CPF_VALIDO_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João Documento"))
                .andExpect(jsonPath("$[0].documento").value(CPF_VALIDO_1));
    }

    @Test
    public void given_documentoInexistente_when_getClienteByDocumento_then_retorna404() throws Exception {
        mockMvc.perform(get(CONTROLLER_PATH).param("documento", CPF_INVALIDO))
                .andExpect(status().isNotFound());
    }

    // ================== testes updateCliente ==================

    @Test
    public void given_idExistente_when_updateCliente_then_retorna200EAtualizaBanco() throws Exception {
        Long clienteId = criarClientePFCustom("Teste Antigo", CPF_VALIDO_1);

        UpdateClienteRequest updateRequest = new UpdateClienteRequest();
        updateRequest.setNome("Teste Novo");

        mockMvc.perform(put(CONTROLLER_PATH + clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.nome").value("Teste Novo"));

        // Banco
        ClienteEntity clienteNoBanco = clienteRepository.findById(clienteId).orElseThrow();
        assertThat(clienteNoBanco.getNome()).isEqualTo("Teste Novo");
    }

    @Test
    public void given_idInexistente_when_updateCliente_then_retorna404() throws Exception {
        UpdateClienteRequest updateRequest = new UpdateClienteRequest();
        updateRequest.setNome("Ninguém");

        mockMvc.perform(put(CONTROLLER_PATH + "999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // ================== testes deleteCliente ==================

    @Test
    public void given_idExistente_when_deleteCliente_then_retorna200EClienteRemovidoDoBanco() throws Exception {
        Long clienteId = criarClientePFCustom("Para Deletar", CPF_VALIDO_2);

        mockMvc.perform(delete(CONTROLLER_PATH + clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.nome").value("Para Deletar"));

        assertThat(clienteRepository.findById(clienteId)).isEmpty();
    }

    @Test
    public void given_idInexistente_when_deleteCliente_then_retorna404() throws Exception {
        mockMvc.perform(delete(CONTROLLER_PATH + "999999"))
                .andExpect(status().isNotFound());
    }

    // utilitário
    private Long criarClientePFCustom(String nome, String cpf) throws Exception {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setNome(nome);
        request.setTipoPessoa(TipoPessoa.PF);
        request.setDocumento(cpf);
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = resultado.getResponse().getHeader("Location");
        return Long.parseLong(location);
    }
}
