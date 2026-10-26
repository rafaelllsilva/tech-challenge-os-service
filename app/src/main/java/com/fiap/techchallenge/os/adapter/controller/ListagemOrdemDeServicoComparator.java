package com.fiap.techchallenge.os.adapter.controller;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class ListagemOrdemDeServicoComparator implements Comparator<OrdemDeServico> {

    // 1. Define a ordem de prioridade explícita dos status
    private static final List<StatusOrdemDeServico> ORDEM_STATUS = Arrays.asList(
            StatusOrdemDeServico.EM_EXECUCAO,
            StatusOrdemDeServico.AGUARDANDO_APROVACAO,
            StatusOrdemDeServico.EM_DIAGNOSTICO,
            StatusOrdemDeServico.RECEBIDA
    );

    // 2. Constrói o comparator de forma declarativa e encadeada
    private final Comparator<OrdemDeServico> comparator = Comparator
            .comparingInt((OrdemDeServico os) -> obterPrioridade(os.getStatus()))
            .thenComparing(
                    OrdemDeServico::getDataHoraCriacao,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );

    @Override
    public int compare(OrdemDeServico o1, OrdemDeServico o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        return comparator.compare(o1, o2);
    }

    // 3. Mtodo auxiliar para traduzir o enum para um peso (inteiro)
    private static int obterPrioridade(StatusOrdemDeServico status) {
        if (status == null) return Integer.MAX_VALUE; // Status nulos vão para o final

        int index = ORDEM_STATUS.indexOf(status);
        // Se for um status que não está na lista principal, joga para o final
        return index == -1 ? Integer.MAX_VALUE : index;
    }
}
