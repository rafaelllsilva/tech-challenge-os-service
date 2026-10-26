package com.fiap.techchallenge.usuario.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.usuario.adapter.controller.dto.LoginRequest;
import com.fiap.techchallenge.usuario.domain.Role;
import com.fiap.techchallenge.usuario.domain.Usuario;
import com.fiap.techchallenge.usuario.usecase.UsuarioGateway;
import com.fiap.techchallenge.security.JwtUtil;
import com.fiap.techchallenge.usuario.usecase.UsuarioUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.fiap.techchallenge.security.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioUseCase usuarioUseCase;

    @MockitoBean
    private UsuarioGateway usuarioGateway;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
    }

    // POST /api/auth/login

    @Test
    void login_credenciaisValidas_retorna200ComTokens() throws Exception {
        var usuario = new Usuario("gerente", "$hash", Role.GERENTE);
        usuario.setId(1L);

        LoginRequest req = new LoginRequest();
        req.setLogin("gerente");
        req.setSenha("senha@123");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("gerente", null));
        when(usuarioGateway.findByLogin("gerente")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateAccessToken("gerente", "GERENTE")).thenReturn("access-token-mock");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-mock"))
                .andExpect(jsonPath("$.role").value("GERENTE"));
    }

    @Test
    void login_credenciaisInvalidas_retorna401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin("usuario");
        req.setSenha("errada");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_camposVazios_retorna400() throws Exception {
        LoginRequest req = new LoginRequest();
        // login e senha em branco

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // POST /api/auth/usuario

    @Test
    @WithMockUser(roles = "GERENTE")
    void criarUsuario_gerente_retorna201() throws Exception {
        var novoUsuario = new Usuario("mecanico2", "$hash", Role.MECANICO);
        novoUsuario.setId(10L);

        when(usuarioUseCase.criarUsuario(any())).thenReturn(novoUsuario);

        String body = """
                {"login":"mecanico2","senha":"senha@123","role":"MECANICO"}
                """;

        mockMvc.perform(post("/api/auth/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void criarUsuario_semPermissao_retorna403() throws Exception {
        String body = """
                {"login":"mecanico3","senha":"senha@123","role":"MECANICO"}
                """;

        mockMvc.perform(post("/api/auth/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

}
