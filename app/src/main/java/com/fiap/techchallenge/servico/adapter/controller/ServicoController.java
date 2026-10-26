package com.fiap.techchallenge.servico.adapter.controller;


import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestCreateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestUpdateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoResponseDTO;
import com.fiap.techchallenge.servico.adapter.controller.mapper.ServicoMapper;
import com.fiap.techchallenge.servico.usecase.ServicoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servico")
@Tag(name = "Serviços", description = "Operações de registo e manutenção de serviços realizados nas ordens de serviço")
public class ServicoController {

    private ServicoUseCase serviceUseCase;

    public ServicoController(ServicoUseCase serviceUseCase) {
        this.serviceUseCase = serviceUseCase;
    }


    //Criação de um serviço
    @PostMapping
    @Operation(summary = "Criar um serviço", description = "Regista um novo serviço associado a uma ordem de serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content)
    })
    public ResponseEntity<ServicoResponseDTO> criaServico(@RequestBody ServicoRequestCreateDTO servicoRequestCreateDTO) {
        var command = ServicoMapper.toCommandCreate(servicoRequestCreateDTO);
        var servico = serviceUseCase.creteServico(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicoMapper.toServicoDTO(servico));
    }

    //Retorna uma lista de serviços cadastrados e filtra também por OS
    @GetMapping
    @Operation(summary = "Listar serviços", description = "Retorna uma lista de serviços, permitindo filtrar por ID da Ordem de Serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso",
                    content = @Content(mediaType = "application/json", 
                    array = @ArraySchema(schema = @Schema(implementation = ServicoResponseDTO.class))))
    })
    public ResponseEntity<List<ServicoResponseDTO>> getServicos(@RequestParam(required = false) Long id) {
        var listaServicos = serviceUseCase.listaServicos(id);
        return ResponseEntity.ok(listaServicos.stream().map(ServicoMapper::toServicoDTO).toList());
    }

    // retorna serviço pelo id
    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID", description = "Retorna os detalhes de um serviço específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServicoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content)
    })
    public ResponseEntity<ServicoResponseDTO> getServicoById(@PathVariable Long id) {
        var servico = serviceUseCase.getServicoById(id);
        return ResponseEntity.ok(ServicoMapper.toServicoDTO(servico));
    }

    //faz deleção lógica de um serviço da OS
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover serviço", description = "Realiza a remoção lógica de um serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteServico(@PathVariable Long id) {
        serviceUseCase.deleteServico(id);
        return ResponseEntity.noContent().build();
    }

    //atualiza um serviço de uma os
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar serviço", description = "Atualiza parcialmente os dados de um serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço atualizado com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content)
    })
    public ResponseEntity<ServicoResponseDTO> updateServico(@PathVariable Long id, @RequestBody ServicoRequestUpdateDTO servicoRequestUpdateDTO) {
        var command = ServicoMapper.toCommandUpdate(servicoRequestUpdateDTO);
        var servico = serviceUseCase.updateServico(id, command);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ServicoMapper.toServicoDTO(servico));
    }

}
