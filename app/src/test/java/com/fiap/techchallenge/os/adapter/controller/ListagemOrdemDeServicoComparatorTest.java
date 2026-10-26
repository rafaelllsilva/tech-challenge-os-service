package com.fiap.techchallenge.os.adapter.controller;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ListagemOrdemDeServicoComparatorTest {

    private final ListagemOrdemDeServicoComparator comparator = new ListagemOrdemDeServicoComparator();

    @Test
    public void given_osEmExecucaoEOsAguardandoAprovacao_when_compara_then_emExecucaoVemAntes() {
        var osEmExecucao = criarOrdemServico(1L, StatusOrdemDeServico.EM_EXECUCAO, LocalDateTime.now());
        var osAguardandoAprovacao = criarOrdemServico(2L, StatusOrdemDeServico.AGUARDANDO_APROVACAO, LocalDateTime.now());

        var resultado = comparator.compare(osEmExecucao, osAguardandoAprovacao);

        assertTrue(resultado < 0, "EM_EXECUCAO deve ter prioridade maior (vir antes)");
    }

    @Test
    public void given_osAguardandoAprovacaoEOsEmDiagnostico_when_compara_then_aguardandoAprovacaoVemAntes() {
        var osAguardandoAprovacao = criarOrdemServico(1L, StatusOrdemDeServico.AGUARDANDO_APROVACAO, LocalDateTime.now());
        var osEmDiagnostico = criarOrdemServico(2L, StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now());

        var resultado = comparator.compare(osAguardandoAprovacao, osEmDiagnostico);

        assertTrue(resultado < 0);
    }

    @Test
    public void given_osEmDiagnosticoEOsRecebida_when_compara_then_emDiagnosticoVemAntes() {
        var osEmDiagnostico = criarOrdemServico(1L, StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now());
        var osRecebida = criarOrdemServico(2L, StatusOrdemDeServico.RECEBIDA, LocalDateTime.now());

        var resultado = comparator.compare(osEmDiagnostico, osRecebida);

        assertTrue(resultado < 0);
    }

    @Test
    public void given_statusInvertidos_when_compara_then_retornaComparacaoInversa() {
        var osRecebida = criarOrdemServico(1L, StatusOrdemDeServico.RECEBIDA, LocalDateTime.now());
        var osEmDiagnostico = criarOrdemServico(2L, StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now());

        var resultado1 = comparator.compare(osEmDiagnostico, osRecebida);
        var resultado2 = comparator.compare(osRecebida, osEmDiagnostico);

        assertTrue(resultado1 < 0);
        assertTrue(resultado2 > 0);
    }

    @Test
    public void given_osComMesmoStatusMasDataDiferente_when_compara_then_maisAntugaVemAntes() {
        var data1 = LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0);
        var data2 = LocalDateTime.of(2026, Month.JANUARY, 2, 10, 0, 0);

        var os1 = criarOrdemServico(1L, StatusOrdemDeServico.RECEBIDA, data1);
        var os2 = criarOrdemServico(2L, StatusOrdemDeServico.RECEBIDA, data2);

        var resultado = comparator.compare(os1, os2);

        assertTrue(resultado < 0, "OS mais antiga deve vir antes");
    }

    @Test
    public void given_osComMesmoStatusMasDataMaisRecente_when_compara_then_maisRecenteVemDepois() {
        var dataAntiga = LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0);
        var dataRecente = LocalDateTime.of(2026, Month.JANUARY, 2, 10, 0, 0);

        var osAntiga = criarOrdemServico(1L, StatusOrdemDeServico.RECEBIDA, dataAntiga);
        var osRecente = criarOrdemServico(2L, StatusOrdemDeServico.RECEBIDA, dataRecente);

        var resultado = comparator.compare(osRecente, osAntiga);

        assertTrue(resultado > 0);
    }

    @Test
    public void given_osComMesmaDataEStatus_when_compara_then_retornaZero() {
        var mesmaData = LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0);

        var os1 = criarOrdemServico(1L, StatusOrdemDeServico.RECEBIDA, mesmaData);
        var os2 = criarOrdemServico(2L, StatusOrdemDeServico.RECEBIDA, mesmaData);

        var resultado = comparator.compare(os1, os2);

        assertEquals(0, resultado);
    }

    @Test
    public void given_statusTemPrioridadeMaiorQueData_when_ordena_then_statusVemAntes() {
        var dataAntiga = LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0);
        var dataRecente = LocalDateTime.of(2026, Month.JANUARY, 10, 10, 0, 0);

        var osRecebidaRecente = criarOrdemServico(1L, StatusOrdemDeServico.RECEBIDA, dataRecente);
        var osEmExecucaoAntiga = criarOrdemServico(2L, StatusOrdemDeServico.EM_EXECUCAO, dataAntiga);

        var resultado = comparator.compare(osEmExecucaoAntiga, osRecebidaRecente);

        assertTrue(resultado < 0, "EM_EXECUCAO (antiga) deve vir antes de RECEBIDA (recente)");
    }

    @Test
    public void given_listaComMultiplosStatusEDatas_when_ordena_then_classificadaCertamente() {
        var data1 = LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0);
        var data2 = LocalDateTime.of(2026, Month.JANUARY, 2, 10, 0, 0);
        var data3 = LocalDateTime.of(2026, Month.JANUARY, 3, 10, 0, 0);
        var data4 = LocalDateTime.of(2026, Month.JANUARY, 4, 10, 0, 0);

        var os1 = criarOrdemServico(1L, StatusOrdemDeServico.RECEBIDA, data4);
        var os2 = criarOrdemServico(2L, StatusOrdemDeServico.EM_EXECUCAO, data3);
        var os3 = criarOrdemServico(3L, StatusOrdemDeServico.AGUARDANDO_APROVACAO, data1);
        var os4 = criarOrdemServico(4L, StatusOrdemDeServico.EM_DIAGNOSTICO, data2);
        var os5 = criarOrdemServico(5L, StatusOrdemDeServico.EM_EXECUCAO, data1);
        var os6 = criarOrdemServico(6L, StatusOrdemDeServico.AGUARDANDO_APROVACAO, data2);

        var ordens = new ArrayList<>(List.of(os1, os2, os3, os4, os5, os6));
        ordens.sort(comparator);

        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, ordens.get(0).getStatus());
        assertEquals(data1, ordens.get(0).getDataHoraCriacao());
        assertEquals(data3, ordens.get(1).getDataHoraCriacao());

        assertEquals(StatusOrdemDeServico.AGUARDANDO_APROVACAO, ordens.get(2).getStatus());
        assertEquals(data1, ordens.get(2).getDataHoraCriacao());

        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, ordens.get(4).getStatus());
        assertEquals(StatusOrdemDeServico.RECEBIDA, ordens.get(5).getStatus());
    }

    @Test
    public void given_multiplosOsComMesmoStatus_when_ordena_then_ordenadasPorDataCrescente() {
        var data1 = LocalDateTime.of(2026, Month.JANUARY, 5, 10, 0, 0);
        var data2 = LocalDateTime.of(2026, Month.JANUARY, 3, 10, 0, 0);
        var data3 = LocalDateTime.of(2026, Month.JANUARY, 8, 10, 0, 0);

        var os1 = criarOrdemServico(1L, StatusOrdemDeServico.EM_EXECUCAO, data1);
        var os2 = criarOrdemServico(2L, StatusOrdemDeServico.EM_EXECUCAO, data2);
        var os3 = criarOrdemServico(3L, StatusOrdemDeServico.EM_EXECUCAO, data3);

        var ordens = new ArrayList<>(List.of(os3, os1, os2));
        ordens.sort(comparator);

        assertEquals(data2, ordens.get(0).getDataHoraCriacao());
        assertEquals(data1, ordens.get(1).getDataHoraCriacao());
        assertEquals(data3, ordens.get(2).getDataHoraCriacao());
    }

    @Test
    public void given_statusFinalizadoEEntregue_when_compara_then_retornaZero() {
        var mesmaData = LocalDateTime.now();
        var finalizada = criarOrdemServico(1L, StatusOrdemDeServico.FINALIZADA, mesmaData);
        var aprovada = criarOrdemServico(2L, StatusOrdemDeServico.APROVADA, mesmaData);

        var resultado = comparator.compare(finalizada, aprovada);

        assertEquals(0, resultado);
    }

    private OrdemDeServico criarOrdemServico(Long id, StatusOrdemDeServico status, LocalDateTime dataHoraCriacao) {
        var os = new OrdemDeServico();
        os.setId(id);
        os.setStatus(status);
        os.setDataHoraCriacao(dataHoraCriacao);
        os.setOrcamento(BigDecimal.ZERO);
        os.setSolicitacao("Solicitação teste");
        return os;
    }
}