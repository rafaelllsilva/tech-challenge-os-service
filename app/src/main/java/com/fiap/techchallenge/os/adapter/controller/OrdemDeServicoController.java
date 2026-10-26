package com.fiap.techchallenge.os.adapter.controller;

import com.fiap.techchallenge.os.adapter.controller.dto.*;
import com.fiap.techchallenge.os.adapter.presenter.OrdemDeServicoMapper;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.usuario.domain.Role;
import com.fiap.techchallenge.usuario.domain.Usuario;
import com.fiap.techchallenge.os.inputdata.AdicionarPecaItemInputData;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping({ "/api/ordemDeServico", "/api/ordemDeServico/" })
@Tag(name = "Ordens de Serviço", description = "Operações de criação, acompanhamento e transição de status das ordens de serviço")
public class OrdemDeServicoController {

    public static final String CONTROLLER_PATH = "/api/ordemDeServico/";

    private final OrdemDeServicoMapper osMapper;
    private final OrdemDeServicoUseCase osUseCase;
    private final ListagemOrdemDeServicoComparator listagemOrdemDeServicoComparator;

    @Value("${webhook.aprovacao-cliente.secret}")
    private String webhookSecret;

    @Autowired
    public OrdemDeServicoController(OrdemDeServicoUseCase osService, OrdemDeServicoMapper osMapper, ListagemOrdemDeServicoComparator listagemOrdemDeServicoComparator) {
        this.osUseCase = osService;
        this.osMapper = osMapper;
        this.listagemOrdemDeServicoComparator = listagemOrdemDeServicoComparator;
    }

    // CRUD
    @GetMapping
    @Operation(summary = "Listar ordens de serviço", description = "Retorna todas as ordens de serviço cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    })
    public ResponseEntity<List<OrdemDeServicoSimplesResponse>> getAllOrdemDeServico() {
        var ordens = this.osUseCase.getAllOrdemDeServico();
        if (!ordens.isEmpty()) {
            ordens = ordens.stream()
                    .filter(os ->
                                    !StatusOrdemDeServico.ENTREGUE.equals(os.getStatus())
                                    && !StatusOrdemDeServico.FINALIZADA.equals(os.getStatus())
                                    && !StatusOrdemDeServico.APROVADA.equals(os.getStatus())
                    )
                    .sorted(listagemOrdemDeServicoComparator)
                    .toList();
        }
        return ResponseEntity.ok(!ordens.isEmpty() ? ordens.stream().map(osMapper::toSimplesResponse).toList() : Collections.emptyList());
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar ordem de serviço por ID", description = "Retorna uma ordem de serviço específica pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de serviço encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> getOrdemDeServicoById(@PathVariable Long id) {
        var os = this.osUseCase.getOrdemDeServico(id);
        var dto = os.map(osMapper::toResponse).orElse(null);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("cliente/{clienteId}/ordens")
    @Operation(summary = "Buscar ordens de serviço por cliente", description = "Retorna as ordens de serviço associadas ao identificador do cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordens de serviço encontradas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordens de serviço não encontradas para o cliente informado", content = @Content)
    })
    public ResponseEntity<List<OrdemDeServicoResponse>> getOrdensDeServicoPorClienteId(@PathVariable Long clienteId,
                                                                                       @AuthenticationPrincipal Usuario usuario) {
        filtrarAcessoDoCliente(usuario, clienteId);
        var ordens = this.osUseCase.getOrdensDeServicoPorClienteId(clienteId);
        List<OrdemDeServicoResponse> dtoList = ordens != null && !ordens.isEmpty() ? ordens.stream().map(osMapper::toResponse).toList() : Collections.emptyList();
        return !dtoList.isEmpty() ? ResponseEntity.ok(dtoList) : ResponseEntity.notFound().build();
    }

    @GetMapping("veiculo/{veiculoId}/ordens")
    @Operation(summary = "Buscar ordens de serviço por veículo", description = "Retorna as ordens de serviço associadas ao identificador do veículo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordens de serviço encontradas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordens de serviço não encontradas para o veículo informado", content = @Content)
    })
    public ResponseEntity<List<OrdemDeServicoResponse>> getOrdensDeServicoPorVeiculoId(@PathVariable Long veiculoId) {
        var ordens = this.osUseCase.getOrdensDeServicoPorVeiculoId(veiculoId);
        List<OrdemDeServicoResponse> dtoList = ordens != null && !ordens.isEmpty() ? ordens.stream().map(osMapper::toResponse).toList() : Collections.emptyList();
        return !dtoList.isEmpty() ? ResponseEntity.ok(dtoList) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Criar ordem de serviço", description = "Cadastra uma nova ordem de serviço vinculada a um cliente e veículo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de serviço criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> createOrdemDeServico(@RequestBody @Valid CreateOrdemDeServicoRequest request) {
        List<AdicionarPecaItemInputData> addPecaItemsInputData = request.getPecas() != null && !request.getPecas().isEmpty()
                ? request.getPecas().stream().map(reqPecaItem -> new AdicionarPecaItemInputData(reqPecaItem.getIdTipoPeca(), reqPecaItem.getQuantidade())).toList()
                : null;
        var os = this.osUseCase.createOrdemDeServico(request.getSolicitacao(), request.getIdCliente(), request.getIdVeiculo(), addPecaItemsInputData, request.getServicos());
        return ResponseEntity.created(URI.create(CONTROLLER_PATH + os.getId().toString())).body(osMapper.toResponse(os));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar ordem de serviço", description = "Atualiza os dados de uma ordem de serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de serviço atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> updateOrdemDeServico(@PathVariable Long id, @RequestBody UpdateOrdemDeServicoRequest request) {
        var os = this.osUseCase.updateOrdemDeServico(id, request.getOrcamento(), request.getStatus() != null ? request.getStatus().toString() : null);
        return os != null ? ResponseEntity.ok(osMapper.toResponse(os)) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Remover ordem de serviço", description = "Remove uma ordem de serviço pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de serviço removida com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> deleteOrdemDeServico(@PathVariable Long id) {
        var os = this.osUseCase.deleteOrdemDeServico(id);
        return os != null ? ResponseEntity.ok(osMapper.toResponse(os)) : ResponseEntity.notFound().build();
    }

    // ###########################################################################################
    // # Comandos do Mecanico
    // ###########################################################################################
    @PutMapping("adicionarServicos/{id}")
    @Operation(summary = "Adicionar serviços à OS", description = "Inclui tipos de serviço na ordem de serviço durante o diagnóstico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviços adicionados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Operação inválida para o estado atual da OS", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> adicionarServicosNaOrdemDeServico(@PathVariable Long id, @RequestBody AdicionarServicosRequest request) {
        var osAtualizada = this.osUseCase.putServicosNaOrdemDeServico(id, request.getIdTipoServicos());
        return osAtualizada != null ? ResponseEntity.ok(osMapper.toResponse(osAtualizada)) : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("removerServicos/{id}")
    @Operation(summary = "Remover serviços da OS", description = "Remove tipos de serviço da ordem de serviço durante o diagnóstico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviços removidos com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Operação inválida para o estado atual da OS", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> removerServicosDaOrdemDeServico(@PathVariable Long id, @RequestBody RemoverServicosRequest request) {
        var osAtualizada = this.osUseCase.deleteServicosDaOrdemDeServico(id, request.getIdTipoServicos());
        return osAtualizada != null ? ResponseEntity.ok(osMapper.toResponse(osAtualizada)) : ResponseEntity.badRequest().build();
    }

    @PutMapping("adicionarPecas/{id}")
    @Operation(summary = "Adicionar peças à OS", description = "Inclui peças e insumos na ordem de serviço durante o diagnóstico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Peças adicionadas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Operação inválida para o estado atual da OS ou estoque insuficiente", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> adicionarPecasNaOrdemDeServico(@PathVariable Long id, @RequestBody AdicionarPecasRequest req) {
        List<AdicionarPecaItemInputData> addPecaItemsInputData = req.getPecasParaAdd().stream().map(reqPecaItem -> new AdicionarPecaItemInputData(reqPecaItem.getIdTipoPeca(), reqPecaItem.getQuantidade())).toList();
        var osAtualizada = this.osUseCase.putPecasNaOrdemDeServico(id, addPecaItemsInputData);
        return osAtualizada != null ? ResponseEntity.ok(osMapper.toResponse(osAtualizada)) : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("removerPecas/{id}")
    @Operation(summary = "Remover peças da OS", description = "Remove peças e insumos da ordem de serviço durante o diagnóstico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Peças removidas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrdemDeServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Operação inválida para o estado atual da OS", content = @Content)
    })
    public ResponseEntity<OrdemDeServicoResponse> removerPecasDaOrdemDeServico(@PathVariable Long id, @RequestBody RemoverPecasRequest req) {
        var osAtualizada = this.osUseCase.deletePecasDaOrdemDeServico(id, req.getIdTipoPecasParaRemover());
        return osAtualizada != null ? ResponseEntity.ok(osMapper.toResponse(osAtualizada)) : ResponseEntity.badRequest().build();
    }

    @PostMapping("transicao/paraDiagnostico/{id}")
    @Operation(summary = "Webhook para iniciar diagnóstico", description = "Endpoint chamado por sistema externo para notificar que a diagnóstico deve ser iniciado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransicaoDeStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida para o estado atual", content = @Content),
            @ApiResponse(responseCode = "401", description = "Segredo do webhook ausente ou inválido", content = @Content)
    })
    public ResponseEntity<TransicaoDeStatusDTO> webhookParaDiagnostico(@PathVariable Long id,
                                                                       @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        validarSegredoDoWebhook(secret);
        var resultado = this.osUseCase.mudarParaStatus(id, StatusOrdemDeServico.EM_DIAGNOSTICO.toString());
        return resultado.isSucesso() ? ResponseEntity.ok(resultado) : ResponseEntity.badRequest().body(resultado);
    }

    @PostMapping("transicao/paraAprovacao/{id}")
    @Operation(summary = "Webhook para enviar para aprovação", description = "Endpoint chamado por sistema externo para notificar que a OS deve ser enviada para aprovação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransicaoDeStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida para o estado atual", content = @Content),
            @ApiResponse(responseCode = "401", description = "Segredo do webhook ausente ou inválido", content = @Content)
    })
    public ResponseEntity<TransicaoDeStatusDTO> webhookParaAprovacao(@PathVariable Long id,
                                                                     @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        validarSegredoDoWebhook(secret);
        var resultado = this.osUseCase.mudarParaStatus(id, StatusOrdemDeServico.AGUARDANDO_APROVACAO.toString());
        return resultado.isSucesso() ? ResponseEntity.ok(resultado) : ResponseEntity.badRequest().body(resultado);
    }

    @PostMapping("transicao/paraAprovacaoOuRejeicaoCliente/{id}")
    @Operation(summary = "Webhook de aprovação/recusa do orçamento", description = "Endpoint chamado por sistema externo para informar a decisão do cliente sobre o orçamento (aprovado ou reprovado)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransicaoDeStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida para o estado atual", content = @Content),
            @ApiResponse(responseCode = "401", description = "Segredo do webhook ausente ou inválido", content = @Content)
    })
    public ResponseEntity<TransicaoDeStatusDTO> webhookParaAprovacaoOuRejeicaoCliente(@PathVariable Long id,
                                                                        @RequestBody @Valid WebhookAprovacaoRequest request,
                                                                        @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        validarSegredoDoWebhook(secret);
        var novoStatus = request.getAprovado() ? StatusOrdemDeServico.APROVADA : StatusOrdemDeServico.FINALIZADA;
        var resultado = this.osUseCase.mudarParaStatus(id, novoStatus.toString());
        return resultado.isSucesso() ? ResponseEntity.ok(resultado) : ResponseEntity.badRequest().body(resultado);
    }

    private void validarSegredoDoWebhook(String secret) {
        if (secret == null || !secret.equals(webhookSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Segredo do webhook inválido.");
        }
    }

    @PostMapping("transicao/paraEmExecucao/{id}")
    @Operation(summary = "Webhook para iniciar execução", description = "Endpoint chamado por sistema externo para notificar que a execução deve ser iniciada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransicaoDeStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida para o estado atual", content = @Content),
            @ApiResponse(responseCode = "401", description = "Segredo do webhook ausente ou inválido", content = @Content)
    })
    public ResponseEntity<TransicaoDeStatusDTO> webhookParaEmExecucao(@PathVariable Long id,
                                                                      @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        validarSegredoDoWebhook(secret);
        var resultado = this.osUseCase.mudarParaStatus(id, StatusOrdemDeServico.EM_EXECUCAO.toString());
        return resultado.isSucesso() ? ResponseEntity.ok(resultado) : ResponseEntity.badRequest().body(resultado);
    }

    @PostMapping("transicao/paraFinalizada/{id}")
    @Operation(summary = "Webhook para finalizar execução", description = "Endpoint chamado por sistema externo para notificar que a execução deve ser finalizada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransicaoDeStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida para o estado atual", content = @Content),
            @ApiResponse(responseCode = "401", description = "Segredo do webhook ausente ou inválido", content = @Content)
    })
    public ResponseEntity<TransicaoDeStatusDTO> webhookParaFinalizada(@PathVariable Long id,
                                                                      @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        validarSegredoDoWebhook(secret);
        var resultado = this.osUseCase.mudarParaStatus(id, StatusOrdemDeServico.FINALIZADA.toString());
        return resultado.isSucesso() ? ResponseEntity.ok(resultado) : ResponseEntity.badRequest().body(resultado);
    }

    @PostMapping("transicao/paraEntregue/{id}")
    @Operation(summary = "Webhook para registrar entrega", description = "Endpoint chamado por sistema externo para notificar que a OS deve ser marcada como Entregue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransicaoDeStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida para o estado atual", content = @Content),
            @ApiResponse(responseCode = "401", description = "Segredo do webhook ausente ou inválido", content = @Content)
    })
    public ResponseEntity<TransicaoDeStatusDTO> webhookParaEntregue(@PathVariable Long id,
                                                                    @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        validarSegredoDoWebhook(secret);
        var resultado = this.osUseCase.mudarParaStatus(id, StatusOrdemDeServico.ENTREGUE.toString());
        return resultado.isSucesso() ? ResponseEntity.ok(resultado) : ResponseEntity.badRequest().body(resultado);
    }

    // Vailidações do JWT


    // Garente que o Cliente só acesse o ID dele
    private void filtrarAcessoDoCliente(Usuario usuario, Long clienteId) {
        if (usuario != null && usuario.getRole() == Role.CLIENTE) {
            if (usuario.getCliente() == null || !usuario.getCliente().getId().equals(clienteId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Acesso negado: você só pode consultar suas próprias informações.");
            }
        }
    }

    // Garente que o Cliente só aprove o ID dele
    private void filtraPropriedadeDaOs(Usuario usuario, Long osId) {
        if (usuario != null && usuario.getRole() == Role.CLIENTE) {
            var os = this.osUseCase.getOrdemDeServico(osId);
            if (os.isEmpty()
                    || usuario.getCliente() == null
                    || os.get().getCliente() == null
                    || !usuario.getCliente().getId().equals(os.get().getCliente().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Acesso negado: você só pode aprovar suas próprias Ordens de Serviço.");
            }
        }
    }

}
