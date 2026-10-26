package com.fiap.techchallenge.cliente.adapter.controller;

import com.fiap.techchallenge.cliente.adapter.controller.dto.ClienteResponse;
import com.fiap.techchallenge.cliente.adapter.controller.dto.CreateClienteRequest;
import com.fiap.techchallenge.cliente.adapter.controller.dto.UpdateClienteRequest;
import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.usecase.ClienteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping({ "/api/cliente", "/api/cliente/" })
@Tag(name = "Clientes", description = "Operações de cadastro e consulta de clientes")
public class ClienteController {

    public static final String CONTROLLER_PATH = "/api/cliente/";

    private final ClienteUseCase clienteUseCase;
    private final ClienteMapper mapper;

    public ClienteController(ClienteUseCase clienteUseCase, ClienteMapper mapper) {
        this.clienteUseCase = clienteUseCase;
        this.mapper = mapper;
    }

    // Lista todos os clientes, ou filtra por documento se o parâmetro for informado
    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes ou filtra por CPF/CNPJ usando o parâmetro documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado para o documento informado", content = @Content)
    })
    public ResponseEntity<?> getAllClientes(@RequestParam(required = false) String documento) {
        if (documento != null) {
            return clienteUseCase.getByDocumento(documento)
                    .<ResponseEntity<?>>map(c -> ResponseEntity.ok(List.of(mapper.toResponse(c))))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Nenhum cliente encontrado para o documento informado."));
        }

        List<ClienteResponse> responses = clienteUseCase.getAll().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    // Busca um cliente pelo ID
    @GetMapping("{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna um cliente específico pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponse> getClienteById(@PathVariable Long id) {
        return clienteUseCase.getById(id)
                .map(c -> ResponseEntity.ok(mapper.toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Cria um novo cliente com validação de documento (CPF/CNPJ)
    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cadastra um novo cliente com validação de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content)
    })
    public ResponseEntity<ClienteResponse> createCliente(@Valid @RequestBody CreateClienteRequest request) {
        Cliente cliente = clienteUseCase.create(
                request.getNome(), request.getTipoPessoa(), request.getDocumento(),
                request.getDataNascimento(), request.getEmail());
        return ResponseEntity.created(URI.create(cliente.getId().toString())).build();
    }

    // Atualiza um cliente existente pelo ID
    @PutMapping("{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content)
    })
    public ResponseEntity<ClienteResponse> updateClienteById(@PathVariable Long id,
            @Valid @RequestBody UpdateClienteRequest request) {
        Cliente cliente = clienteUseCase.updateById(id,
                request.getNome(), request.getTipoPessoa(), request.getDocumento(),
                request.getDataNascimento(), request.getEmail());
        return ResponseEntity.ok(mapper.toResponse(cliente));
    }

    // Remove um cliente pelo ID e retorna os dados do cliente removido
    @DeleteMapping("{id}")
    @Operation(summary = "Remover cliente", description = "Remove um cliente pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente removido com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponse> deleteClienteById(@PathVariable Long id) {
        Cliente cliente = clienteUseCase.deleteById(id);
        return ResponseEntity.ok(mapper.toResponse(cliente));
    }

    // Trata erros de validação de documento (CPF/CNPJ inválido), retornando 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarDocumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Trata tentativa de deletar/alterar cliente que possui vínculos, retornando
    // 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> tratarViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        String detalhe = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Operação não permitida: o cliente possui registros vinculados e não pode ser removido. Detalhes: "
                        + detalhe);
    }

}
