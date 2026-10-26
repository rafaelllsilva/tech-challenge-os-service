package com.fiap.techchallenge.peca.adapter.controller;

import com.fiap.techchallenge.peca.adapter.controller.dto.PecaResponse;
import com.fiap.techchallenge.peca.adapter.controller.dto.CreatePecaRequest;
import com.fiap.techchallenge.peca.adapter.controller.dto.UpdatePecaRequest;
import com.fiap.techchallenge.peca.usecase.PecaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping({ "/api/peca", "/api/peca/" })
@Tag(name = "Peças", description = "Operações de cadastro e manutenção de peças associadas à ordem de serviço")
public class PecaController {

    private final PecaUseCase pecaUseCase;
    private final PecaMapper mapper;

    public PecaController(PecaUseCase pecaUseCase, PecaMapper mapper) {
        this.pecaUseCase = pecaUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar peças", description = "Retorna todas as peças cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<PecaResponse>> getAllPecas() {
        var pecas = this.pecaUseCase.getAllPecas().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(pecas);
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar peça por ID", description = "Retorna uma peça específica pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Peça encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada", content = @Content)
    })
    public ResponseEntity<PecaResponse> getPecaById(@PathVariable Long id) {
        return this.pecaUseCase.getPeca(id)
                .map(peca -> ResponseEntity.ok(mapper.toResponse(peca)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar peça", description = "Cadastra uma nova peça")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Peça criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content)
    })
    public ResponseEntity<PecaResponse> createPeca(@Valid @RequestBody CreatePecaRequest request) {
        var peca = this.pecaUseCase.createPeca(
                request.getIdOs() != null ? request.getIdOs().longValue() : null,
                request.getIdTipoPeca() != null ? request.getIdTipoPeca().longValue() : null,
                request.getQuantidade()
        );
        return ResponseEntity.created(URI.create(peca.getId().toString())).body(mapper.toResponse(peca));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar peça", description = "Atualiza os dados de uma peça existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Peça atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content)
    })
    public ResponseEntity<PecaResponse> updatePeca(@PathVariable Long id, @RequestBody UpdatePecaRequest request) {
        var peca = this.pecaUseCase.updatePeca(
                id,
                request.getIdOs() != null ? request.getIdOs().longValue() : null,
                request.getIdTipoPeca() != null ? request.getIdTipoPeca().longValue() : null,
                request.getQuantidade()
        );
        return ResponseEntity.ok(mapper.toResponse(peca));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Remover peça", description = "Remove uma peça pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Peça removida com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada", content = @Content)
    })
    public ResponseEntity<PecaResponse> deletePeca(@PathVariable Long id) {
        var peca = this.pecaUseCase.deletePeca(id);
        return ResponseEntity.ok(mapper.toResponse(peca));
    }
}
