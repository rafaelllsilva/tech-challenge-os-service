package com.fiap.techchallenge.servico.adapter.gateway;

import com.fiap.techchallenge.servico.adapter.gateway.mapper.TipoServicoGatewayMapper;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TipoServicoGatewayMapperTest {

    private TipoServico tipoServico;
    private TipoServicoEntity tipoServicoEntity;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        tipoServicoEntity = new TipoServicoEntity();
        tipoServicoEntity.setId(1L);
        tipoServicoEntity.setNome("Troca de Óleo");
        tipoServicoEntity.setValor(new BigDecimal("150.00"));
    }

    // --- toEntity ---

    @Test
    void testToEntity_mapeiaCorretamente() {
        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoServico);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Troca de Óleo", resultado.getNome());
        assertEquals(new BigDecimal("150.00"), resultado.getValor());
    }

    @Test
    void testToEntity_preservaId() {
        TipoServico tipoComId = new TipoServico(5L, "Alinhamento", new BigDecimal("200.00"));

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoComId);

        assertEquals(5L, resultado.getId());
    }

    @Test
    void testToEntity_preservaNome() {
        TipoServico tipoComNome = new TipoServico(1L, "Nome Específico", new BigDecimal("100.00"));

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoComNome);

        assertEquals("Nome Específico", resultado.getNome());
    }

    @Test
    void testToEntity_preservaValor() {
        TipoServico tipoComValor = new TipoServico(1L, "Teste", new BigDecimal("999.99"));

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoComValor);

        assertEquals(new BigDecimal("999.99"), resultado.getValor());
    }

    @Test
    void testToEntity_comValoresNulos() {
        TipoServico tipoNulo = new TipoServico();

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoNulo);

        assertNotNull(resultado);
        assertNull(resultado.getId());
        assertNull(resultado.getNome());
        assertNull(resultado.getValor());
    }

    @Test
    void testToEntity_comIdNulo() {
        TipoServico tipoSemId = new TipoServico(null, "Sem ID", new BigDecimal("50.00"));

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoSemId);

        assertNull(resultado.getId());
        assertEquals("Sem ID", resultado.getNome());
        assertEquals(new BigDecimal("50.00"), resultado.getValor());
    }

    // --- toDomain ---

    @Test
    void testToDomain_mapeiaCorretamente() {
        TipoServico resultado = TipoServicoGatewayMapper.toDomain(tipoServicoEntity);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Troca de Óleo", resultado.getNome());
        assertEquals(new BigDecimal("150.00"), resultado.getValor());
    }

    @Test
    void testToDomain_preservaId() {
        TipoServicoEntity entityComId = new TipoServicoEntity();
        entityComId.setId(10L);
        entityComId.setNome("Teste");
        entityComId.setValor(new BigDecimal("100.00"));

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityComId);

        assertEquals(10L, resultado.getId());
    }

    @Test
    void testToDomain_preservaNome() {
        TipoServicoEntity entityComNome = new TipoServicoEntity();
        entityComNome.setId(1L);
        entityComNome.setNome("Nome Específico");
        entityComNome.setValor(new BigDecimal("100.00"));

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityComNome);

        assertEquals("Nome Específico", resultado.getNome());
    }

    @Test
    void testToDomain_preservaValor() {
        TipoServicoEntity entityComValor = new TipoServicoEntity();
        entityComValor.setId(1L);
        entityComValor.setNome("Teste");
        entityComValor.setValor(new BigDecimal("999.99"));

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityComValor);

        assertEquals(new BigDecimal("999.99"), resultado.getValor());
    }

    @Test
    void testToDomain_comValoresNulos() {
        TipoServicoEntity entityNula = new TipoServicoEntity();

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityNula);

        assertNotNull(resultado);
        assertNull(resultado.getId());
        assertNull(resultado.getNome());
        assertNull(resultado.getValor());
    }

    // --- mapping cycle ---

    @Test
    void testMappingCycle_domainParaEntityPreservaValores() {
        TipoServicoEntity entidade = TipoServicoGatewayMapper.toEntity(tipoServico);
        TipoServico recuperado = TipoServicoGatewayMapper.toDomain(entidade);

        assertEquals(tipoServico.getId(), recuperado.getId());
        assertEquals(tipoServico.getNome(), recuperado.getNome());
        assertEquals(tipoServico.getValor(), recuperado.getValor());
    }

    @Test
    void testMappingCycle_entityParaDomainPreservaValores() {
        TipoServico dominio = TipoServicoGatewayMapper.toDomain(tipoServicoEntity);
        TipoServicoEntity entidade = TipoServicoGatewayMapper.toEntity(dominio);

        assertEquals(tipoServicoEntity.getId(), entidade.getId());
        assertEquals(tipoServicoEntity.getNome(), entidade.getNome());
        assertEquals(tipoServicoEntity.getValor(), entidade.getValor());
    }

    @Test
    void testToEntity_comValorDecimal() {
        TipoServico tipoComValorDecimal = new TipoServico(1L, "Teste", new BigDecimal("123.45"));

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoComValorDecimal);

        assertEquals(new BigDecimal("123.45"), resultado.getValor());
    }

    @Test
    void testToDomain_comValorDecimal() {
        TipoServicoEntity entityComValorDecimal = new TipoServicoEntity();
        entityComValorDecimal.setId(1L);
        entityComValorDecimal.setNome("Teste");
        entityComValorDecimal.setValor(new BigDecimal("123.45"));

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityComValorDecimal);

        assertEquals(new BigDecimal("123.45"), resultado.getValor());
    }

    @Test
    void testToEntity_comNomeComEspacos() {
        TipoServico tipoComEspacos = new TipoServico(1L, "Nome Com Espaços", new BigDecimal("100.00"));

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoComEspacos);

        assertEquals("Nome Com Espaços", resultado.getNome());
    }

    @Test
    void testToDomain_comNomeComEspacos() {
        TipoServicoEntity entityComEspacos = new TipoServicoEntity();
        entityComEspacos.setId(1L);
        entityComEspacos.setNome("Nome Com Espaços");
        entityComEspacos.setValor(new BigDecimal("100.00"));

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityComEspacos);

        assertEquals("Nome Com Espaços", resultado.getNome());
    }

    @Test
    void testToEntity_comValorZero() {
        TipoServico tipoGratuito = new TipoServico(1L, "Gratuito", BigDecimal.ZERO);

        TipoServicoEntity resultado = TipoServicoGatewayMapper.toEntity(tipoGratuito);

        assertEquals(BigDecimal.ZERO, resultado.getValor());
    }

    @Test
    void testToDomain_comValorZero() {
        TipoServicoEntity entityGratuita = new TipoServicoEntity();
        entityGratuita.setId(1L);
        entityGratuita.setNome("Gratuito");
        entityGratuita.setValor(BigDecimal.ZERO);

        TipoServico resultado = TipoServicoGatewayMapper.toDomain(entityGratuita);

        assertEquals(BigDecimal.ZERO, resultado.getValor());
    }

    @Test
    void testMappingCycle_multiplosCiclos() {
        TipoServico original = new TipoServico(5L, "Teste Ciclo", new BigDecimal("250.00"));

        TipoServicoEntity entity1 = TipoServicoGatewayMapper.toEntity(original);
        TipoServico domain1 = TipoServicoGatewayMapper.toDomain(entity1);
        TipoServicoEntity entity2 = TipoServicoGatewayMapper.toEntity(domain1);

        assertEquals(original.getId(), entity2.getId());
        assertEquals(original.getNome(), entity2.getNome());
        assertEquals(original.getValor(), entity2.getValor());
    }
}
