package com.fiap.techchallenge.servico.adapter.controller.mapper;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestCreateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestUpdateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoResponseDTO;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.command.ServicoCreateCommand;
import com.fiap.techchallenge.servico.usecase.command.ServicoUpdateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicoMapperTest {

    private TipoServico tipoServico;
    private OrdemDeServico ordemDeServico;
    private Servico servico;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(2L);

        servico = new Servico(
                1L,
                tipoServico,
                ordemDeServico,
                ServicoStatus.AGUARDANDO_INICIO,
                null,
                null
        );
    }

    // --- toServicoDTO ---

    @Test
    void testToServicoDTO_mapeiaCorretamente() {
        ServicoResponseDTO resultado = ServicoMapper.toServicoDTO(servico);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, resultado.getStatus());
        assertEquals(2L, resultado.getOrdemDeServicoId());
        assertNull(resultado.getDataInicio());
        assertNull(resultado.getDataFim());
    }

    @Test
    void testToServicoDTO_mapeioTipoServico() {
        ServicoResponseDTO resultado = ServicoMapper.toServicoDTO(servico);

        assertNotNull(resultado.getTipoServico());
        assertEquals(1L, resultado.getTipoServico().getId());
        assertEquals("Troca de Óleo", resultado.getTipoServico().getNome());
        assertEquals(new BigDecimal("150.00"), resultado.getTipoServico().getValor());
    }

    @Test
    void testToServicoDTO_preservaStatus_emExecucao() {
        Servico emExecucao = new Servico(
                1L, tipoServico, ordemDeServico,
                ServicoStatus.EM_EXECUCAO, LocalDateTime.now().minusHours(1), null
        );

        ServicoResponseDTO resultado = ServicoMapper.toServicoDTO(emExecucao);

        assertEquals(ServicoStatus.EM_EXECUCAO, resultado.getStatus());
        assertNotNull(resultado.getDataInicio());
        assertNull(resultado.getDataFim());
    }

    @Test
    void testToServicoDTO_preservaDatas_finalizado() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(3);
        LocalDateTime fim = LocalDateTime.now();

        Servico finalizado = new Servico(
                2L, tipoServico, ordemDeServico,
                ServicoStatus.FINALIZADO, inicio, fim
        );

        ServicoResponseDTO resultado = ServicoMapper.toServicoDTO(finalizado);

        assertEquals(ServicoStatus.FINALIZADO, resultado.getStatus());
        assertEquals(inicio, resultado.getDataInicio());
        assertEquals(fim, resultado.getDataFim());
    }

    // --- toCommandCreate ---

    @Test
    void testToCommandCreate_mapeiaCorretamente() {
        ServicoRequestCreateDTO dto = ServicoRequestCreateDTO.builder()
                .tipoServicoId(1L)
                .ordemDeServicoId(2L)
                .build();

        ServicoCreateCommand command = ServicoMapper.toCommandCreate(dto);

        assertNotNull(command);
        assertEquals(1L, command.getTipoServicoId());
        assertEquals(2L, command.getOrdemDeServicoId());
    }

    @Test
    void testToCommandCreate_preservaIdsTipoServico() {
        ServicoRequestCreateDTO dto = ServicoRequestCreateDTO.builder()
                .tipoServicoId(99L)
                .ordemDeServicoId(55L)
                .build();

        ServicoCreateCommand command = ServicoMapper.toCommandCreate(dto);

        assertEquals(99L, command.getTipoServicoId());
        assertEquals(55L, command.getOrdemDeServicoId());
    }

    // --- toCommandUpdate ---

    @Test
    void testToCommandUpdate_mapeiaCorretamente() {
        ServicoRequestUpdateDTO dto = ServicoRequestUpdateDTO.builder()
                .tipoServicoId(3L)
                .ordemDeServicoId(4L)
                .build();

        ServicoUpdateCommand command = ServicoMapper.toCommandUpdate(dto);

        assertNotNull(command);
        assertEquals(3L, command.getTipoServicoId());
        assertEquals(4L, command.getOrdemDeServicoId());
    }

    @Test
    void testToCommandUpdate_camposNulos_saoPreservados() {
        ServicoRequestUpdateDTO dto = ServicoRequestUpdateDTO.builder()
                .tipoServicoId(null)
                .ordemDeServicoId(null)
                .build();

        ServicoUpdateCommand command = ServicoMapper.toCommandUpdate(dto);

        assertNotNull(command);
        assertNull(command.getTipoServicoId());
        assertNull(command.getOrdemDeServicoId());
    }
}
