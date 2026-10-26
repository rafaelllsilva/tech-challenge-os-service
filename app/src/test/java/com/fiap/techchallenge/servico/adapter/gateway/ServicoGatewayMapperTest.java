package com.fiap.techchallenge.servico.adapter.gateway;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.servico.adapter.gateway.mapper.ServicoGatewayMapper;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicoGatewayMapperTest {

    private Servico servico;
    private ServicoEntity servicoEntity;
    private TipoServico tipoServico;
    private TipoServicoEntity tipoServicoEntity;
    private OrdemDeServico ordemDeServico;
    private OrdemDeServicoEntity ordemDeServicoEntity;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        tipoServicoEntity = new TipoServicoEntity();
        tipoServicoEntity.setId(1L);
        tipoServicoEntity.setNome("Troca de Óleo");
        tipoServicoEntity.setValor(new BigDecimal("150.00"));

        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(1L);
        ordemDeServicoEntity = new OrdemDeServicoEntity();
        ordemDeServicoEntity.setId(1L);

        servico = new Servico(
                1L,
                tipoServico,
                ordemDeServico,
                ServicoStatus.AGUARDANDO_INICIO,
                null,
                null
        );

        servicoEntity = new ServicoEntity();
        servicoEntity.setId(1L);
        servicoEntity.setTipoServico(tipoServicoEntity);
        servicoEntity.setOrdemDeServico(ordemDeServicoEntity);
        servicoEntity.setStatus(ServicoStatus.AGUARDANDO_INICIO);
        servicoEntity.setDataInicio(null);
        servicoEntity.setDataFim(null);
    }

    // --- toEntity ---

    @Test
    void testToEntity_mapeiaCorretamente() {
        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servico);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, resultado.getStatus());
        assertNull(resultado.getDataInicio());
        assertNull(resultado.getDataFim());
    }

    @Test
    void testToEntity_preservaId() {
        Servico servicoComId = new Servico(5L, tipoServico, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO, null, null);

        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servicoComId);

        assertEquals(5L, resultado.getId());
    }

    @Test
    void testToEntity_preservaStatus() {
        Servico servicoEmExecucao = new Servico(1L, tipoServico, ordemDeServico, ServicoStatus.EM_EXECUCAO, LocalDateTime.now(), null);

        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servicoEmExecucao);

        assertEquals(ServicoStatus.EM_EXECUCAO, resultado.getStatus());
    }

    @Test
    void testToEntity_preservaDatas() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(2);
        LocalDateTime fim = LocalDateTime.now();
        Servico servicoFinalizado = new Servico(1L, tipoServico, ordemDeServico, ServicoStatus.FINALIZADO, inicio, fim);

        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servicoFinalizado);

        assertEquals(inicio, resultado.getDataInicio());
        assertEquals(fim, resultado.getDataFim());
    }

    @Test
    void testToEntity_preservaTipoServico() {
        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servico);

        assertNotNull(resultado.getTipoServico());
        assertEquals(1L, resultado.getTipoServico().getId());
        assertEquals("Troca de Óleo", resultado.getTipoServico().getNome());
    }

    @Test
    void testToEntity_preservaOrdemDeServico() {
        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servico);

        assertNotNull(resultado.getOrdemDeServico());
        assertEquals(1L, resultado.getOrdemDeServico().getId());
    }

    // --- toDomain ---

    @Test
    void testToDomain_mapeiaCorretamente() {
        Servico resultado = ServicoGatewayMapper.toDomain(servicoEntity);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, resultado.getStatus());
        assertNull(resultado.getDataInicio());
        assertNull(resultado.getDataFim());
    }

    @Test
    void testToDomain_preservaId() {
        TipoServicoEntity tipoEntity = new TipoServicoEntity();
        tipoEntity.setId(1L);
        tipoEntity.setNome("Tipo");
        tipoEntity.setValor(BigDecimal.TEN);

        ServicoEntity entityComId = new ServicoEntity();
        entityComId.setId(10L);
        entityComId.setStatus(ServicoStatus.AGUARDANDO_INICIO);
        entityComId.setTipoServico(tipoEntity);

        Servico resultado = ServicoGatewayMapper.toDomain(entityComId);

        assertEquals(10L, resultado.getId());
    }

    @Test
    void testToDomain_preservaStatus() {
        TipoServicoEntity tipoEntity = new TipoServicoEntity();
        tipoEntity.setId(1L);
        tipoEntity.setNome("Tipo");
        tipoEntity.setValor(BigDecimal.TEN);

        ServicoEntity entityEmExecucao = new ServicoEntity();
        entityEmExecucao.setId(1L);
        entityEmExecucao.setStatus(ServicoStatus.EM_EXECUCAO);
        entityEmExecucao.setDataInicio(LocalDateTime.now());
        entityEmExecucao.setTipoServico(tipoEntity);

        Servico resultado = ServicoGatewayMapper.toDomain(entityEmExecucao);

        assertEquals(ServicoStatus.EM_EXECUCAO, resultado.getStatus());
        assertNotNull(resultado.getDataInicio());
    }

    @Test
    void testToDomain_preservaDatas() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(1);
        LocalDateTime fim = LocalDateTime.now();

        TipoServicoEntity tipoEntity = new TipoServicoEntity();
        tipoEntity.setId(1L);
        tipoEntity.setNome("Tipo");
        tipoEntity.setValor(BigDecimal.TEN);

        ServicoEntity entityFinalizado = new ServicoEntity();
        entityFinalizado.setId(1L);
        entityFinalizado.setStatus(ServicoStatus.FINALIZADO);
        entityFinalizado.setDataInicio(inicio);
        entityFinalizado.setDataFim(fim);
        entityFinalizado.setTipoServico(tipoEntity);

        Servico resultado = ServicoGatewayMapper.toDomain(entityFinalizado);

        assertEquals(inicio, resultado.getDataInicio());
        assertEquals(fim, resultado.getDataFim());
    }

    @Test
    void testToDomain_preservaTipoServico() {
        Servico resultado = ServicoGatewayMapper.toDomain(servicoEntity);

        assertNotNull(resultado.getTipoServico());
        assertEquals(1L, resultado.getTipoServico().getId());
        assertEquals("Troca de Óleo", resultado.getTipoServico().getNome());
    }

    @Test
    void testToDomain_preservaOrdemDeServico() {
        Servico resultado = ServicoGatewayMapper.toDomain(servicoEntity);

        assertNotNull(resultado.getOrdemDeServico());
        assertEquals(1L, resultado.getOrdemDeServico().getId());
    }

    // --- mapping cycle ---

    @Test
    void testMappingCycle_domainParaEntityPreservaValores() {
        ServicoEntity entidade = ServicoGatewayMapper.toEntity(servico);
        Servico recuperado = ServicoGatewayMapper.toDomain(entidade);

        assertEquals(servico.getId(), recuperado.getId());
        assertEquals(servico.getStatus(), recuperado.getStatus());
    }

    @Test
    void testMappingCycle_entityParaDomainPreservaValores() {
        Servico dominio = ServicoGatewayMapper.toDomain(servicoEntity);
        ServicoEntity entidade = ServicoGatewayMapper.toEntity(dominio);

        assertEquals(servicoEntity.getId(), entidade.getId());
        assertEquals(servicoEntity.getStatus(), entidade.getStatus());
    }

    @Test
    void testToEntity_comTodosCamposFilled() {
        LocalDateTime agora = LocalDateTime.now();
        Servico completo = new Servico(
                100L,
                tipoServico,
                ordemDeServico,
                ServicoStatus.FINALIZADO,
                agora.minusHours(2),
                agora
        );

        ServicoEntity resultado = ServicoGatewayMapper.toEntity(completo);

        assertEquals(100L, resultado.getId());
        assertEquals(ServicoStatus.FINALIZADO, resultado.getStatus());
        assertNotNull(resultado.getDataInicio());
        assertNotNull(resultado.getDataFim());
        assertNotNull(resultado.getTipoServico());
        assertNotNull(resultado.getOrdemDeServico());
    }

    @Test
    void testToDomain_comTodosCamposFilled() {
        LocalDateTime agora = LocalDateTime.now();
        ServicoEntity completo = new ServicoEntity();
        completo.setId(100L);
        completo.setStatus(ServicoStatus.FINALIZADO);
        completo.setDataInicio(agora.minusHours(2));
        completo.setDataFim(agora);
        completo.setTipoServico(tipoServicoEntity);
        completo.setOrdemDeServico(ordemDeServicoEntity);

        Servico resultado = ServicoGatewayMapper.toDomain(completo);

        assertEquals(100L, resultado.getId());
        assertEquals(ServicoStatus.FINALIZADO, resultado.getStatus());
        assertNotNull(resultado.getDataInicio());
        assertNotNull(resultado.getDataFim());
        assertNotNull(resultado.getTipoServico());
        assertNotNull(resultado.getOrdemDeServico());
    }

    @Test
    void testToEntity_comStatusDeletado() {
        Servico servico = new Servico(1L, tipoServico, ordemDeServico, ServicoStatus.DELETADO, null, null);

        ServicoEntity resultado = ServicoGatewayMapper.toEntity(servico);

        assertEquals(ServicoStatus.DELETADO, resultado.getStatus());
    }

    @Test
    void testToDomain_comStatusDeletado() {
        TipoServicoEntity tipoEntity = new TipoServicoEntity();
        tipoEntity.setId(1L);
        tipoEntity.setNome("Tipo");
        tipoEntity.setValor(BigDecimal.TEN);

        ServicoEntity entity = new ServicoEntity();
        entity.setId(1L);
        entity.setStatus(ServicoStatus.DELETADO);
        entity.setTipoServico(tipoEntity);

        Servico resultado = ServicoGatewayMapper.toDomain(entity);

        assertEquals(ServicoStatus.DELETADO, resultado.getStatus());
    }
}
