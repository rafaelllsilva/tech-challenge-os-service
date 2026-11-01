package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.cliente.domain.Cliente;
import com.fiap.techchallenge.os.cliente.usecase.ClienteGateway;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.TransicaoDeStatusDTO;
import com.fiap.techchallenge.os.exception.BadRequestException;
import com.fiap.techchallenge.os.exception.NotFoundException;
import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.observabilidade.OrdemDeServicoEventLogger;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OrcamentoAprovadoPayload;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OrcamentoRejeitadoPayload;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OsCanceladaPayload;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OsCriadaPayload;
import com.fiap.techchallenge.os.mensageria.usecase.PublicadorDeEventos;
import com.fiap.techchallenge.os.veiculo.domain.Veiculo;
import com.fiap.techchallenge.os.veiculo.usecase.VeiculoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Slf4j
public class OrdemDeServicoInteractor implements OrdemDeServicoUseCase {

    private static final String MOTIVO_PAGAMENTO_RECUSADO = "PAGAMENTO_RECUSADO";
    private static final String OS_NAO_EXISTE_MSG_TEMPLATE = "Erro: Ordem de Serviço '%d' não existe!";

    private final OrdemDeServicoGateway osGateway;
    private final ClienteGateway clienteGateway;
    private final VeiculoGateway veiculoGateway;
    private final HistoricoStatusOrdemDeServicoGateway historicoStatusOrdemDeServicoGateway;
    private final EventosPublicadosGateway eventosPublicadosGateway;
    private final EventosRecebidosGateway eventosRecebidosGateway;
    private final OrdemDeServicoEventLogger loggerEventos;
    private final PublicadorDeEventos publicadorDeEventos;
    private final Clock clock;

    @Autowired
    public OrdemDeServicoInteractor(
            OrdemDeServicoGateway osGateway,
            ClienteGateway clienteGateway, VeiculoGateway veiculoGateway,
            HistoricoStatusOrdemDeServicoGateway historicoStatusOrdemDeServicoGateway,
            EventosPublicadosGateway eventosPublicadosGateway, EventosRecebidosGateway eventosRecebidosGateway,
            OrdemDeServicoEventLogger loggerEventos, PublicadorDeEventos publicadorDeEventos, Clock clock) {
        this.osGateway = osGateway;
        this.clienteGateway = clienteGateway;
        this.veiculoGateway = veiculoGateway;
        this.historicoStatusOrdemDeServicoGateway = historicoStatusOrdemDeServicoGateway;
        this.eventosPublicadosGateway = eventosPublicadosGateway;
        this.eventosRecebidosGateway = eventosRecebidosGateway;
        this.loggerEventos = loggerEventos;
        this.publicadorDeEventos = publicadorDeEventos;
        this.clock = clock;
    }

    // ###########################################################################################
    // # CRUD
    // ###########################################################################################

    @Override
    public List<OrdemDeServico> getAllOrdemDeServico() {
        return this.osGateway.findAllOrdensDeServico();
    }

    @Override
    public Optional<OrdemDeServico> getOrdemDeServico(Long id) {
        return this.osGateway.findOrdemDeServicoById(id);
    }

    @Override
    public List<OrdemDeServico> getOrdensDeServicoPorClienteId(Long clienteId) {
        return this.osGateway.findAllOrdensDeServicoClienteId(clienteId);
    }

    @Override
    public List<OrdemDeServico> getOrdensDeServicoPorVeiculoId(Long veiculoId) {
        return this.osGateway.findAllOrdensDeServicoVeiculoId(veiculoId);
    }

    @Transactional
    @Override
    public OrdemDeServico createOrdemDeServico(String solicitacao, Long idCliente, Long idVeiculo) {
        try {
            Optional<Cliente> cOpt = this.clienteGateway.findById(idCliente);
            Optional<Veiculo> vOpt = this.veiculoGateway.findById(idVeiculo);

            if (cOpt.isEmpty()) {
                throw new BadRequestException("Erro: Cliente '%d' não existe!".formatted(idCliente));
            }

            if (vOpt.isEmpty()) {
                throw new BadRequestException("Erro: Veículo '%d' não existe!".formatted(idVeiculo));
            }

            Instant ocorreuEm = clock.instant();
            OrdemDeServico novaOs = new OrdemDeServico();
            novaOs.setSolicitacao(solicitacao);
            novaOs.setValorTotal(BigDecimal.valueOf(0L, 2));
            novaOs.setDataHoraCriacao(LocalDateTime.ofInstant(ocorreuEm, ZoneOffset.UTC));
            novaOs.setCliente(cOpt.get());
            novaOs.setVeiculo(vOpt.get());
            novaOs.setStatus(StatusOrdemDeServico.RECEBIDA);

            novaOs = this.osGateway.saveOrdemDeServico(novaOs);
            historicoStatusOrdemDeServicoGateway.registrarStatus(novaOs.getId(), StatusOrdemDeServico.RECEBIDA, ocorreuEm);

            String osId = String.valueOf(novaOs.getId());
            publicadorDeEventos.publicar(EventEnvelope.novo(TipoEvento.OS_CRIADA, osId,
                    new OsCriadaPayload(osId, String.valueOf(cOpt.get().getId()), String.valueOf(vOpt.get().getId()),
                            novaOs.getSolicitacao(), ocorreuEm), clock));

            loggerEventos.registrarOrdemCriada(novaOs.getId(), novaOs.getStatus(), ocorreuEm);
            return novaOs;
        } catch (BadRequestException ex) {
            loggerEventos.registrarFalhaProcessamento(null, "creation", "BUSINESS_RULE_VIOLATION", clock.instant(), false);
            throw ex;
        } catch (RuntimeException ex) {
            loggerEventos.registrarFalhaProcessamento(null, "creation", "UNEXPECTED_ERROR", clock.instant(), true);
            throw ex;
        }
    }

    @Override
    @Transactional
    public OrdemDeServico updateOrdemDeServico(Long id, String solicitacao) {
        try {
            Optional<OrdemDeServico> osAtualOpt = this.osGateway.findOrdemDeServicoById(id);

            if (osAtualOpt.isPresent()) {
                var osEditada = osAtualOpt.get();
                if (solicitacao != null) osEditada.setSolicitacao(solicitacao);
                return this.osGateway.updateOrdemDeServico(osEditada);
            }

            String message = getMensagemOsNaoExiste(id);
            loggerEventos.registrarFalhaProcessamento(id, "update", "ORDER_NOT_FOUND", clock.instant(), false);
            throw new BadRequestException(message);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            loggerEventos.registrarFalhaProcessamento(id, "update", "UNEXPECTED_ERROR", clock.instant(), true);
            throw ex;
        }
    }

    @Override
    public OrdemDeServico deleteOrdemDeServico(Long id) {
        Optional<OrdemDeServico> osAtualOpt = this.osGateway.findOrdemDeServicoById(id);

        if (osAtualOpt.isPresent()) {
            var osDeletada = osAtualOpt.get();
            this.osGateway.deleteOrdemDeServico(osDeletada);
            return osDeletada;
        } else {
            String message = getMensagemOsNaoExiste(id);
            log.error(message);
            throw new BadRequestException(message);
        }
    }

    // ###########################################################################################
    // # Comandos
    // ###########################################################################################

    @Override
    @Transactional
    public OrdemDeServico aprovarOrcamento(Long id) {
        OrdemDeServico os = buscarOuFalhar(id);
        aplicarTransicao(os, os::aprovarOrcamento);
        String osId = String.valueOf(os.getId());
        publicadorDeEventos.publicar(EventEnvelope.novo(TipoEvento.ORCAMENTO_APROVADO, osId,
                new OrcamentoAprovadoPayload(osId, os.getOrcamentoId()), clock));
        return os;
    }

    @Override
    @Transactional
    public OrdemDeServico rejeitarOrcamento(Long id, String motivo) {
        OrdemDeServico os = buscarOuFalhar(id);
        aplicarTransicao(os, os::rejeitarOrcamento);
        String osId = String.valueOf(os.getId());
        publicadorDeEventos.publicar(EventEnvelope.novo(TipoEvento.ORCAMENTO_REJEITADO, osId,
                new OrcamentoRejeitadoPayload(osId, os.getOrcamentoId(), motivo), clock));
        return os;
    }

    @Override
    @Transactional
    public OrdemDeServico cancelarOrdemDeServico(Long id, String motivo) {
        OrdemDeServico os = buscarOuFalhar(id);
        StatusOrdemDeServico statusAnterior = os.getStatus();
        aplicarTransicao(os, os::cancelar);
        String osId = String.valueOf(os.getId());
        publicadorDeEventos.publicar(EventEnvelope.novo(TipoEvento.OS_CANCELADA, osId,
                new OsCanceladaPayload(osId, motivo, statusAnterior.name()), clock));
        return os;
    }

    @Override
    @Transactional
    public TransicaoDeStatusDTO entregar(Long id) {
        Optional<OrdemDeServico> osOpt = this.osGateway.findOrdemDeServicoById(id);
        if (osOpt.isEmpty()) {
            loggerEventos.registrarFalhaProcessamento(id, "delivery", "ORDER_NOT_FOUND", clock.instant(), false);
            return new TransicaoDeStatusDTO(false, getMensagemOsNaoExiste(id));
        }
        OrdemDeServico os = osOpt.get();
        try {
            aplicarTransicao(os, os::entregar);
        } catch (BadRequestException ex) {
            loggerEventos.registrarFalhaProcessamento(id, "delivery", "INVALID_STATUS_TRANSITION", clock.instant(), false);
            return new TransicaoDeStatusDTO(false, ex.getMessage());
        }
        return new TransicaoDeStatusDTO(true, "A Ordem de Serviço %d mudou para '%s'".formatted(id, os.getStatus()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemHistorico> consultarHistorico(Long id) {
        if (this.osGateway.findOrdemDeServicoById(id).isEmpty()) {
            throw new NotFoundException(getMensagemOsNaoExiste(id));
        }
        List<ItemHistorico> itens = new ArrayList<>();
        itens.addAll(historicoStatusOrdemDeServicoGateway.listarPorOrdemDeServico(id));
        itens.addAll(eventosPublicadosGateway.listarPorOrdemDeServico(id));
        itens.addAll(eventosRecebidosGateway.listarPorOrdemDeServico(id));
        // sort estável: em empate de instante, mantém status, depois publicados, depois recebidos
        itens.sort(Comparator.comparing(ItemHistorico::ocorridoEm));
        return itens;
    }

    private OrdemDeServico buscarOuFalhar(Long id) {
        return this.osGateway.findOrdemDeServicoById(id).orElseThrow(() -> {
            loggerEventos.registrarFalhaProcessamento(id, "command", "ORDER_NOT_FOUND", clock.instant(), false);
            return new NotFoundException(getMensagemOsNaoExiste(id));
        });
    }

    // ###########################################################################################
    // # Saga: eventos do billing
    // ###########################################################################################

    @Override
    @Transactional
    public void aplicarOrcamentoGerado(Long id, String orcamentoId, BigDecimal valorTotal) {
        buscarParaSaga(id).ifPresent(os ->
                aplicarTransicao(os, () -> os.registrarOrcamento(orcamentoId, valorTotal)));
    }

    @Override
    @Transactional
    public void aplicarOrcamentoRejeitadoPeloBilling(Long id) {
        buscarParaSaga(id).ifPresent(os -> {
            if (StatusOrdemDeServico.CANCELADA.equals(os.getStatus())) {
                log.info("Ordem de Serviço {} já está CANCELADA, ignorando ORCAMENTO_REJEITADO do billing", id);
                return;
            }
            aplicarTransicao(os, os::rejeitarOrcamento);
            loggerEventos.registrarCompensacao(os.getId(), "ORCAMENTO_REJEITADO", os.getStatus(), clock.instant());
        });
    }

    @Override
    @Transactional
    public void aplicarPagamentoConfirmado(Long id) {
        buscarParaSaga(id).ifPresent(os -> aplicarTransicao(os, os::confirmarPagamento));
    }

    @Override
    @Transactional
    public void aplicarPagamentoFalhou(Long id, int maxTentativas) {
        buscarParaSaga(id).ifPresent(os -> {
            StatusOrdemDeServico statusAnterior = os.getStatus();
            aplicarTransicao(os, () -> os.registrarFalhaPagamento(maxTentativas));
            loggerEventos.registrarCompensacao(os.getId(), "PAGAMENTO_FALHOU", os.getStatus(), clock.instant());
            if (StatusOrdemDeServico.CANCELADA.equals(os.getStatus())) {
                String osId = String.valueOf(os.getId());
                publicadorDeEventos.publicar(EventEnvelope.novo(TipoEvento.OS_CANCELADA, osId,
                        new OsCanceladaPayload(osId, MOTIVO_PAGAMENTO_RECUSADO, statusAnterior.name()), clock));
            }
        });
    }

    @Override
    @Transactional
    public void aplicarPagamentoEstornado(Long id) {
        // A OS já foi cancelada por EXECUCAO_FALHOU: o estorno só fica registrado em evento_processado.
        buscarParaSaga(id).ifPresent(os ->
                log.info("Pagamento da Ordem de Serviço {} estornado (status atual '{}')", id, os.getStatus()));
    }

    // ###########################################################################################
    // # Saga: eventos do execution
    // ###########################################################################################

    @Override
    @Transactional
    public void aplicarExecucaoIniciada(Long id) {
        buscarParaSaga(id).ifPresent(os -> aplicarTransicao(os, os::iniciarExecucao));
    }

    @Override
    @Transactional
    public void aplicarExecucaoFinalizada(Long id) {
        buscarParaSaga(id).ifPresent(os -> aplicarTransicao(os, os::finalizarExecucao));
    }

    @Override
    @Transactional
    public void aplicarExecucaoFalhou(Long id) {
        // O os-service não publica estorno: o billing reage a EXECUCAO_FALHOU e publica PAGAMENTO_ESTORNADO.
        buscarParaSaga(id).ifPresent(os -> {
            aplicarTransicao(os, os::falharExecucao);
            loggerEventos.registrarCompensacao(os.getId(), "EXECUCAO_FALHOU", os.getStatus(), clock.instant());
        });
    }

    @Override
    @Transactional
    public void registrarEventoInformativo(Long id, String tipoEvento) {
        buscarParaSaga(id).ifPresent(os ->
                log.info("Evento {} registrado para a Ordem de Serviço {} (status atual '{}')", tipoEvento, id, os.getStatus()));
    }

    private Optional<OrdemDeServico> buscarParaSaga(Long id) {
        Optional<OrdemDeServico> os = this.osGateway.findOrdemDeServicoById(id);
        if (os.isEmpty()) {
            log.error(getMensagemOsNaoExiste(id));
            loggerEventos.registrarFalhaProcessamento(id, "saga_event", "ORDER_NOT_FOUND", clock.instant(), false);
        }
        return os;
    }

    private void aplicarTransicao(OrdemDeServico os, Runnable acao) {
        StatusOrdemDeServico statusAnterior = os.getStatus();
        acao.run();
        Instant ocorreuEm = clock.instant();
        long duracaoNoStatusAtual = calcularDuracaoNoStatusAtual(os.getId(), ocorreuEm);
        this.osGateway.updateOrdemDeServico(os);
        historicoStatusOrdemDeServicoGateway.registrarStatus(os.getId(), os.getStatus(), ocorreuEm);
        loggerEventos.registrarStatusAlterado(os.getId(), statusAnterior, os.getStatus(), duracaoNoStatusAtual, ocorreuEm);
    }

    private String getMensagemOsNaoExiste(Long id) {
        return OS_NAO_EXISTE_MSG_TEMPLATE.formatted(id);
    }

    private long calcularDuracaoNoStatusAtual(Long idOrdemDeServico, Instant ocorreuEm) {
        Instant inicioDoStatusAtual = historicoStatusOrdemDeServicoGateway.buscarInicioDoStatusAtual(idOrdemDeServico)
                .orElseThrow(() -> new IllegalStateException("Status history is missing for service order"));
        return Duration.between(inicioDoStatusAtual, ocorreuEm).toMillis();
    }
}
