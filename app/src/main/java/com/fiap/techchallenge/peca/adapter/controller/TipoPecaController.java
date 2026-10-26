package com.fiap.techchallenge.peca.adapter.controller;

import com.fiap.techchallenge.peca.adapter.controller.dto.TipoPecaResponse;
import com.fiap.techchallenge.peca.adapter.controller.dto.CreateTipoPecaRequest;
import com.fiap.techchallenge.peca.adapter.controller.dto.UpdateTipoPecaRequest;
import com.fiap.techchallenge.peca.usecase.TipoPecaUseCase;
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
@RequestMapping({ "/api/tipoPeca", "/api/tipoPeca/" })
@Tag(name = "Tipos de Peça", description = "Operações de catálogo de peças e controle de estoque")
public class TipoPecaController {

    private final TipoPecaUseCase tipoPecaUseCase;
    private final TipoPecaMapper mapper;

    public TipoPecaController(TipoPecaUseCase tipoPecaUseCase, TipoPecaMapper mapper) {
        this.tipoPecaUseCase = tipoPecaUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de peça", description = "Retorna todos os tipos de peça cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<TipoPecaResponse>> getAllTiposPeca() {
        var tiposPeca = this.tipoPecaUseCase.getAllTiposPeca().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(tiposPeca);
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar tipo de peça por ID", description = "Retorna um tipo de peça específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de peça encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoPecaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de peça não encontrado", content = @Content)
    })
    public ResponseEntity<TipoPecaResponse> getTipoPecaById(@PathVariable Long id) {
        return this.tipoPecaUseCase.getTipoPeca(id)
                .map(tipoPeca -> ResponseEntity.ok(mapper.toResponse(tipoPeca)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar tipo de peça", description = "Cadastra um novo tipo de peça com valor unitário e estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de peça criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoPecaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content)
    })
    public ResponseEntity<TipoPecaResponse> createTipoPeca(@Valid @RequestBody CreateTipoPecaRequest request) {
        var tipoPeca = this.tipoPecaUseCase.createTipoPeca(request.getNome(), request.getValorUnitario(), request.getQuantidadeEstoque());
        return ResponseEntity.created(URI.create(tipoPeca.getId().toString())).body(mapper.toResponse(tipoPeca));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar tipo de peça", description = "Atualiza nome, valor unitário e estoque de um tipo de peça")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de peça atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoPecaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de peça não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content)
    })
    public ResponseEntity<TipoPecaResponse> updateTipoPeca(@PathVariable Long id, @RequestBody UpdateTipoPecaRequest request) {
        var tipoPeca = this.tipoPecaUseCase.updateTipoPeca(id, request.getNome(), request.getValorUnitario(), request.getQuantidadeEstoque());
        return ResponseEntity.ok(mapper.toResponse(tipoPeca));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Remover tipo de peça", description = "Remove um tipo de peça pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de peça removido com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoPecaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de peça não encontrado", content = @Content)
    })
    public ResponseEntity<TipoPecaResponse> deleteTipoPeca(@PathVariable Long id) {
        var tipoPeca = this.tipoPecaUseCase.deleteTipoPeca(id);
        return ResponseEntity.ok(mapper.toResponse(tipoPeca));
    }
}
