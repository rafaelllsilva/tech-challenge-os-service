package com.fiap.techchallenge.veiculo.adapter.controller;

import com.fiap.techchallenge.veiculo.adapter.controller.dto.CreateVeiculoRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.UpdateVeiculoByIdRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.UpdateVeiculoByPlacaRequest;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.VeiculoResponse;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import com.fiap.techchallenge.veiculo.usecase.VeiculoUseCase;
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
@RequestMapping("/api/veiculo")
@Tag(name = "Veículos", description = "Operações de cadastro e consulta de veículos")
public class VeiculoController {

    private final VeiculoUseCase veiculoUseCase;
    private final VeiculoWebMapper mapper;

    public VeiculoController(VeiculoUseCase veiculoUseCase, VeiculoWebMapper mapper) {
        this.veiculoUseCase = veiculoUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar veículos", description = "Retorna todos os veículos ou filtra por placa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    })
    public ResponseEntity<List<VeiculoResponse>> getAll(@RequestParam(required = false) String placa) {
        if (placa != null) {
            return veiculoUseCase.getByPlaca(placa)
                    .map(v -> ResponseEntity.ok(List.of(mapper.toResponse(v))))
                    .orElse(ResponseEntity.ok(List.of()));
        }
        List<VeiculoResponse> responses = veiculoUseCase.getAll().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar veículo por ID", description = "Retorna um veículo específico pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    public ResponseEntity<VeiculoResponse> getById(@PathVariable Long id) {
        return veiculoUseCase.getById(id)
                .map(v -> ResponseEntity.ok(mapper.toResponse(v)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar veículo", description = "Cadastra um novo veículo com validação de placa e ano")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veículo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content)
    })
    public ResponseEntity<VeiculoResponse> create(@Valid @RequestBody CreateVeiculoRequest request) {
        Veiculo veiculo = veiculoUseCase.create(request.getPlaca(), request.getMarca(), request.getModelo(), request.getAno());
        return ResponseEntity.created(URI.create(veiculo.getId().toString())).build();
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar veículo por ID", description = "Atualiza os dados de um veículo existente usando o identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content)
    })
    public ResponseEntity<VeiculoResponse> updateById(@PathVariable Long id, @Valid @RequestBody UpdateVeiculoByIdRequest request) {
        Veiculo veiculo = veiculoUseCase.updateById(id, request.getPlaca(), request.getMarca(), request.getModelo(), request.getAno());
        return ResponseEntity.ok(mapper.toResponse(veiculo));
    }

    @PutMapping
    @Operation(summary = "Atualizar veículo por placa", description = "Atualiza os dados de um veículo a partir da placa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content)
    })
    public ResponseEntity<VeiculoResponse> updateByPlaca(@RequestParam String placa, @Valid @RequestBody UpdateVeiculoByPlacaRequest request) {
        Veiculo veiculo = veiculoUseCase.updateByPlaca(placa, request.getMarca(), request.getModelo(), request.getAno());
        return ResponseEntity.ok(mapper.toResponse(veiculo));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Remover veículo por ID", description = "Remove um veículo pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo removido com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Veículo com dependências ativas", content = @Content)
    })
    public ResponseEntity<VeiculoResponse> deleteById(@PathVariable Long id) {
        Veiculo veiculo = veiculoUseCase.deleteById(id);
        return ResponseEntity.ok(mapper.toResponse(veiculo));
    }

    @DeleteMapping
    @Operation(summary = "Remover veículo por placa", description = "Remove um veículo usando a placa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo removido com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Veículo com dependências ativas", content = @Content)
    })
    public ResponseEntity<VeiculoResponse> deleteByPlaca(@RequestParam String placa) {
        Veiculo veiculo = veiculoUseCase.deleteByPlaca(placa);
        return ResponseEntity.ok(mapper.toResponse(veiculo));
    }
}
