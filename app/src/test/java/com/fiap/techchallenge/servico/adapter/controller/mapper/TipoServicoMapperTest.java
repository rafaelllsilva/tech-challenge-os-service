package com.fiap.techchallenge.servico.adapter.controller.mapper;

import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoRequestDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.command.TipoServicoRequestsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TipoServicoMapperTest {

    private TipoServico tipoServico;
    private TipoServicoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));
        requestDTO = TipoServicoRequestDTO.builder()
                .nome("Alinhamento")
                .valor(new BigDecimal("200.00"))
                .build();
    }

    // --- toDto ---

    @Test
    void testToDto_mapeiaCorretamente() {
        TipoServicoDTO resultado = TipoServicoMapper.toDto(tipoServico);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Troca de Óleo", resultado.getNome());
        assertEquals(new BigDecimal("150.00"), resultado.getValor());
    }

    @Test
    void testToDto_preservaId() {
        TipoServico tipoComId = new TipoServico(5L, "Teste", new BigDecimal("100.00"));

        TipoServicoDTO resultado = TipoServicoMapper.toDto(tipoComId);

        assertEquals(5L, resultado.getId());
    }

    @Test
    void testToDto_preservaNome() {
        TipoServico tipoComNome = new TipoServico(1L, "Nome Específico", new BigDecimal("100.00"));

        TipoServicoDTO resultado = TipoServicoMapper.toDto(tipoComNome);

        assertEquals("Nome Específico", resultado.getNome());
    }

    @Test
    void testToDto_preservaValor() {
        TipoServico tipoComValor = new TipoServico(1L, "Teste", new BigDecimal("999.99"));

        TipoServicoDTO resultado = TipoServicoMapper.toDto(tipoComValor);

        assertEquals(new BigDecimal("999.99"), resultado.getValor());
    }

    @Test
    void testToDto_comValoresNulos() {
        TipoServico tipoNulo = new TipoServico();

        TipoServicoDTO resultado = TipoServicoMapper.toDto(tipoNulo);

        assertNotNull(resultado);
        assertNull(resultado.getId());
        assertNull(resultado.getNome());
        assertNull(resultado.getValor());
    }

    // --- toCommand ---

    @Test
    void testToCommand_mapeiaCorretamente() {
        TipoServicoRequestsCommand resultado = TipoServicoMapper.toCommand(requestDTO);

        assertNotNull(resultado);
        assertEquals("Alinhamento", resultado.getNome());
        assertEquals(new BigDecimal("200.00"), resultado.getValor());
    }

    @Test
    void testToCommand_preservaNomeEValor() {
        TipoServicoRequestDTO dto = TipoServicoRequestDTO.builder()
                .nome("Reparo de Freio")
                .valor(new BigDecimal("350.50"))
                .build();

        TipoServicoRequestsCommand resultado = TipoServicoMapper.toCommand(dto);

        assertEquals("Reparo de Freio", resultado.getNome());
        assertEquals(new BigDecimal("350.50"), resultado.getValor());
    }

    @Test
    void testToCommand_preservaNomeComEspacos() {
        TipoServicoRequestDTO dto = TipoServicoRequestDTO.builder()
                .nome("Serviço Com Espaços")
                .valor(new BigDecimal("100.00"))
                .build();

        TipoServicoRequestsCommand resultado = TipoServicoMapper.toCommand(dto);

        assertEquals("Serviço Com Espaços", resultado.getNome());
    }

    @Test
    void testToCommand_preservaValorDecimal() {
        TipoServicoRequestDTO dto = TipoServicoRequestDTO.builder()
                .nome("Teste")
                .valor(new BigDecimal("150.99"))
                .build();

        TipoServicoRequestsCommand resultado = TipoServicoMapper.toCommand(dto);

        assertEquals(new BigDecimal("150.99"), resultado.getValor());
    }

    @Test
    void testToCommand_comValorZero() {
        TipoServicoRequestDTO dto = TipoServicoRequestDTO.builder()
                .nome("Gratuito")
                .valor(BigDecimal.ZERO)
                .build();

        TipoServicoRequestsCommand resultado = TipoServicoMapper.toCommand(dto);

        assertEquals(BigDecimal.ZERO, resultado.getValor());
    }

    @Test
    void testToCommand_comValoresNulos() {
        TipoServicoRequestDTO dto = TipoServicoRequestDTO.builder()
                .nome(null)
                .valor(null)
                .build();

        TipoServicoRequestsCommand resultado = TipoServicoMapper.toCommand(dto);

        assertNotNull(resultado);
        assertNull(resultado.getNome());
        assertNull(resultado.getValor());
    }

    // --- mapping cycle ---

    @Test
    void testMappingCycle_domainToDtoPreservaValores() {
        TipoServico original = new TipoServico(3L, "Original", new BigDecimal("300.00"));

        TipoServicoDTO dto = TipoServicoMapper.toDto(original);

        assertEquals(original.getId(), dto.getId());
        assertEquals(original.getNome(), dto.getNome());
        assertEquals(original.getValor(), dto.getValor());
    }

    @Test
    void testMappingCycle_requestToDtoViaCommand() {
        TipoServicoRequestDTO request = TipoServicoRequestDTO.builder()
                .nome("Nome Teste")
                .valor(new BigDecimal("250.00"))
                .build();

        TipoServicoRequestsCommand command = TipoServicoMapper.toCommand(request);

        assertEquals(request.getNome(), command.getNome());
        assertEquals(request.getValor(), command.getValor());
    }

    @Test
    void testToDto_comValorAltissimo() {
        TipoServico tipoComValorAlto = new TipoServico(1L, "Restauração Completa", new BigDecimal("99999.99"));

        TipoServicoDTO resultado = TipoServicoMapper.toDto(tipoComValorAlto);

        assertEquals(new BigDecimal("99999.99"), resultado.getValor());
    }

    @Test
    void testToCommand_preservaEstruturaDados() {
        TipoServicoRequestDTO dto = TipoServicoRequestDTO.builder()
                .nome("Teste Mapper")
                .valor(new BigDecimal("123.45"))
                .build();

        TipoServicoRequestsCommand comando = TipoServicoMapper.toCommand(dto);

        assertTrue(comando.getNome().equals(dto.getNome()));
        assertTrue(comando.getValor().compareTo(dto.getValor()) == 0);
    }
}
