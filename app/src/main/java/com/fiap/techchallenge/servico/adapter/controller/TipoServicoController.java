package com.fiap.techchallenge.servico.adapter.controller;

import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoRequestDTO;
import com.fiap.techchallenge.servico.adapter.controller.mapper.TipoServicoMapper;
import com.fiap.techchallenge.servico.usecase.TipoServicoUseCase;
import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-servico")
@Tag(name = "Tipos de Serviço", description = "Operações de consulta e monitoramento de tipos de serviço")
public class TipoServicoController {

    private final TipoServicoUseCase tipoServicoUseCase;

    public TipoServicoController(TipoServicoUseCase tipoServicoUseCase) {
        this.tipoServicoUseCase = tipoServicoUseCase;
    }

    //Calcula o tempo médio dos serviços, podendo ser procurado tanto por id tanto por listagem de todos os tipos de serviços
    @GetMapping("/tempo-medio")
    @Operation(summary = "Consultar tempo médio de execução", description = "Retorna o tempo médio de execução dos serviços. Pode ser filtrado por tipo de serviço informando o parâmetro id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TempoMedioServicoDTO.class)))
    })
    public ResponseEntity<List<TempoMedioServicoDTO>> getTempoMedioServico(@RequestParam(required = false) Long id) {
        var tempoMedio = tipoServicoUseCase.getTempoMedio(id);
        return ResponseEntity.ok(tempoMedio);
    }

    // Get all para tipos de serviço
    @GetMapping()
    @Operation(summary = "Listar todos os tipos de serviço", description = "Retorna uma lista com todos os tipos de serviço cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoServicoDTO.class)))
    })
    public  ResponseEntity<List<TipoServicoDTO>> getAllTipoServico(){
        var listTipoServico = tipoServicoUseCase.listAll();
        return ResponseEntity.ok(listTipoServico.stream().map(TipoServicoMapper::toDto).toList());
    }

    //Get por id do tipo serviço
    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de serviço por ID", description = "Retorna um tipo de serviço específico com base no ID informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de serviço encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoServicoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de serviço não encontrado", content = @Content)
    })
    public  ResponseEntity<TipoServicoDTO> getByIdTipoServico(@PathVariable Long id){
        var tipoServico = tipoServicoUseCase.getById(id);
        return ResponseEntity.ok(TipoServicoMapper.toDto(tipoServico));
    }

    //Post criação de tipo serviço
    @PostMapping
    @Operation(summary = "Criar tipo de serviço", description = "Cria um novo tipo de serviço com base nos dados informados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de serviço criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoServicoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content)
    })
    public ResponseEntity<TipoServicoDTO> createTipoServico(@RequestBody TipoServicoRequestDTO tipoServicoRequestDTO){
        var command = TipoServicoMapper.toCommand(tipoServicoRequestDTO);
        var tipoServicoDTO = tipoServicoUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(TipoServicoMapper.toDto(tipoServicoDTO));
    }

    //Update de tipo serviço
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de serviço", description = "Atualiza um tipo de serviço existente com base no ID e nos dados informados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de serviço atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TipoServicoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content)
    })
    public ResponseEntity<TipoServicoDTO> updateTipoServico(@PathVariable Long id,
                                                            @RequestBody TipoServicoRequestDTO tipoServicoRequestDTO){
        var command = TipoServicoMapper.toCommand(tipoServicoRequestDTO);
        var tipoServicoDTO = tipoServicoUseCase.update(id, command);
        return ResponseEntity.ok(TipoServicoMapper.toDto(tipoServicoDTO));
    }

    //Deletar tipo seviço
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tipo de serviço", description = "Exclui um tipo de serviço com base no ID informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tipo de serviço excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tipo de serviço não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteTipoServico(@PathVariable Long id){
        tipoServicoUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
