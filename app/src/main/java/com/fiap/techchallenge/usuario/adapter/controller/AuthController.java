package com.fiap.techchallenge.usuario.adapter.controller;

import com.fiap.techchallenge.usuario.adapter.controller.dto.CreateUsuarioRequest;
import com.fiap.techchallenge.usuario.adapter.controller.dto.LoginRequest;
import com.fiap.techchallenge.usuario.adapter.controller.dto.LoginResponse;
import com.fiap.techchallenge.usuario.domain.Usuario;
import com.fiap.techchallenge.security.JwtUtil;
import com.fiap.techchallenge.usuario.usecase.UsuarioGateway;
import com.fiap.techchallenge.usuario.usecase.UsuarioUseCase;
import com.fiap.techchallenge.usuario.inputdata.CreateUsuarioInputData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Operações de login e gerenciamento de usuários")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioUseCase usuarioUseCase;
    private final UsuarioGateway usuarioGateway;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UsuarioUseCase usuarioUseCase,
                          UsuarioGateway usuarioGateway) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioUseCase = usuarioUseCase;
        this.usuarioGateway = usuarioGateway;
    }

    // Autentica o usuário e retorna o access token JWT
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica o usuário e retorna um access token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        }

        Usuario usuario = usuarioGateway.findByLogin(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        String accessToken = jwtUtil.generateAccessToken(usuario.getLogin(), usuario.getRole().name());

        return ResponseEntity.ok(new LoginResponse(accessToken, usuario.getRole().name(), 3600L));
    }

    // Cria um novo usuário (role GERENTE)
    @PreAuthorize("hasRole('GERENTE')")
    @PostMapping("/usuario")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema. Requer role GERENTE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer role GERENTE", content = @Content)
    })
    public ResponseEntity<?> criarUsuario(@Valid @RequestBody CreateUsuarioRequest request) {
        try {
            CreateUsuarioInputData inputData = new CreateUsuarioInputData(
                    request.getLogin(),
                    request.getSenha(),
                    request.getRole(),
                    request.getClienteId()
            );
            Usuario criado = usuarioUseCase.criarUsuario(inputData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuário criado com ID: " + criado.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
