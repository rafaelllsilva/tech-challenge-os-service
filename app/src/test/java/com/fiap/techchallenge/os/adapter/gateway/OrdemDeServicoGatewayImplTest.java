package com.fiap.techchallenge.os.adapter.gateway;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdemDeServicoGatewayImplTest {

    @Mock
    private OrdemDeServicoRepository repository;

    @InjectMocks
    private OrdemDeServicoGatewayImpl gateway;

    private OrdemDeServico criarOrdemDeServico(Long id, StatusOrdemDeServico status) {
        OrdemDeServico os = new OrdemDeServico();
        os.setId(id);
        os.setStatus(status);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(100));
        return os;
    }

    // #################### findAllOrdensDeServico() ####################

    @Test
    public void given_existemOrdens_when_buscaTodasAsOrdens_then_retornaListaDeOrdens() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findAll()).thenReturn(List.of(osEntity));

        var resultado = gateway.findAllOrdensDeServico();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Problema nos freios.", resultado.get(0).getSolicitacao());
    }

    @Test
    public void given_nenhumaOrdem_when_buscaTodasAsOrdens_then_retornaListaVazia() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        var resultado = gateway.findAllOrdensDeServico();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void given_multiplaOrdensExistem_when_buscaTodasAsOrdens_then_retornaTodasAOrdens() {
        var osEntity1 = new OrdemDeServicoEntity();
        osEntity1.setId(1L);
        osEntity1.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity1.setSolicitacao("Problema nos freios.");
        osEntity1.setOrcamento(BigDecimal.valueOf(100));

        var osEntity2 = new OrdemDeServicoEntity();
        osEntity2.setId(2L);
        osEntity2.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntity2.setSolicitacao("Problema nos freios.");
        osEntity2.setOrcamento(BigDecimal.valueOf(100));

        var osEntity3 = new OrdemDeServicoEntity();
        osEntity3.setId(3L);
        osEntity3.setStatus(StatusOrdemDeServico.APROVADA);
        osEntity3.setSolicitacao("Problema nos freios.");
        osEntity3.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findAll()).thenReturn(List.of(osEntity1, osEntity2, osEntity3));

        var resultado = gateway.findAllOrdensDeServico();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        assertEquals(3L, resultado.get(2).getId());
    }

    // #################### findOrdemDeServicoById(Long id) ####################

    @Test
    public void given_ordemExiste_when_buscaPorId_then_retornaOrdem() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findById(1L)).thenReturn(Optional.of(osEntity));

        var resultado = gateway.findOrdemDeServicoById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, resultado.get().getStatus());
    }

    @Test
    public void given_ordemNaoExiste_when_buscaPorId_then_retornaVazio() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        var resultado = gateway.findOrdemDeServicoById(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void given_ordemComPecasEServicos_when_buscaPorId_then_retornaOrdemCompleta() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));
        osEntity.setPecas(null);
        osEntity.setServicos(null);

        when(repository.findById(1L)).thenReturn(Optional.of(osEntity));

        var resultado = gateway.findOrdemDeServicoById(1L);

        assertTrue(resultado.isPresent());
        assertNull(resultado.get().getPecas());
        assertNull(resultado.get().getServicos());
    }

    // #################### findAllOrdensDeServicoClienteId(Long clienteId) ####################

    @Test
    public void given_ordemExiste_when_buscaPorClienteId_then_retornaOrdem() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findAllByClienteId(456L)).thenReturn(List.of(osEntity));

        var resultado = gateway.findAllOrdensDeServicoClienteId(456L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void given_nenhuma_ordem_para_cliente_when_buscaPorClienteId_then_retornaListaVazia() {
        when(repository.findAllByClienteId(999L)).thenReturn(Collections.emptyList());

        var resultado = gateway.findAllOrdensDeServicoClienteId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void given_multiplas_ordensParaCliente_when_buscaPorClienteId_then_retornaTodasAsOrdensDoCliente() {
        var osEntity1 = new OrdemDeServicoEntity();
        osEntity1.setId(1L);
        osEntity1.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity1.setSolicitacao("Problema nos freios.");
        osEntity1.setOrcamento(BigDecimal.valueOf(100));

        var osEntity2 = new OrdemDeServicoEntity();
        osEntity2.setId(2L);
        osEntity2.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntity2.setSolicitacao("Problema nos freios.");
        osEntity2.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findAllByClienteId(456L)).thenReturn(List.of(osEntity1, osEntity2));

        var resultado = gateway.findAllOrdensDeServicoClienteId(456L);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
    }

    // #################### findAllOrdensDeServicoVeiculoId(Long veiculoId) ####################

    @Test
    public void given_ordemExiste_when_buscaPorVeiculoId_then_retornaOrdem() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findAllByVeiculoId(123L)).thenReturn(List.of(osEntity));

        var resultado = gateway.findAllOrdensDeServicoVeiculoId(123L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void given_nenhuma_ordem_para_veiculo_when_buscaPorVeiculoId_then_retornaListaVazia() {
        when(repository.findAllByVeiculoId(999L)).thenReturn(Collections.emptyList());

        var resultado = gateway.findAllOrdensDeServicoVeiculoId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void given_multiplas_ordensParaVeiculo_when_buscaPorVeiculoId_then_retornaTodasAsOrdensDoVeiculo() {
        var osEntity1 = new OrdemDeServicoEntity();
        osEntity1.setId(1L);
        osEntity1.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity1.setSolicitacao("Problema nos freios.");
        osEntity1.setOrcamento(BigDecimal.valueOf(100));

        var osEntity2 = new OrdemDeServicoEntity();
        osEntity2.setId(2L);
        osEntity2.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntity2.setSolicitacao("Problema nos freios.");
        osEntity2.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findAllByVeiculoId(123L)).thenReturn(List.of(osEntity1, osEntity2));

        var resultado = gateway.findAllOrdensDeServicoVeiculoId(123L);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
    }

    // #################### saveOrdemDeServico(OrdemDeServico os) ####################

    @Test
    public void given_ordemValida_when_salvaOrdem_then_retornaOrdemSalva() {
        var os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.RECEBIDA);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(100));

        var osEntitySalva = new OrdemDeServicoEntity();
        osEntitySalva.setId(1L);
        osEntitySalva.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntitySalva.setSolicitacao("Problema nos freios.");
        osEntitySalva.setOrcamento(BigDecimal.valueOf(100));

        when(repository.save(any(OrdemDeServicoEntity.class))).thenReturn(osEntitySalva);

        var resultado = gateway.saveOrdemDeServico(os);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, resultado.getStatus());
        assertEquals("Problema nos freios.", resultado.getSolicitacao());
        verify(repository).save(any(OrdemDeServicoEntity.class));
    }

    @Test
    public void given_ordemComValoresCompletos_when_salvaOrdem_then_preservaTodosOsDados() {
        var os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.RECEBIDA);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(500));

        var osEntitySalva = new OrdemDeServicoEntity();
        osEntitySalva.setId(1L);
        osEntitySalva.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntitySalva.setSolicitacao("Problema nos freios.");
        osEntitySalva.setOrcamento(BigDecimal.valueOf(500));

        when(repository.save(any(OrdemDeServicoEntity.class))).thenReturn(osEntitySalva);

        var resultado = gateway.saveOrdemDeServico(os);

        assertEquals(BigDecimal.valueOf(500), resultado.getOrcamento());
    }

    @Test
    public void given_ordemNula_when_salvaOrdem_then_retornaNulo() {
        var resultado = gateway.saveOrdemDeServico(null);

        assertNull(resultado);
    }

    // #################### updateOrdemDeServico(OrdemDeServico os) ####################

    @Test
    public void given_ordemExisteValida_when_atualizaOrdem_then_retornaOrdemAtualizada() {
        var os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(250));

        var osEntityExistente = new OrdemDeServicoEntity();
        osEntityExistente.setId(1L);
        osEntityExistente.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntityExistente.setSolicitacao("Problema nos freios.");
        osEntityExistente.setOrcamento(BigDecimal.valueOf(100));

        var osEntityAtualizada = new OrdemDeServicoEntity();
        osEntityAtualizada.setId(1L);
        osEntityAtualizada.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntityAtualizada.setSolicitacao("Problema nos freios.");
        osEntityAtualizada.setOrcamento(BigDecimal.valueOf(250));

        when(repository.findById(1L)).thenReturn(Optional.of(osEntityExistente));
        when(repository.save(any(OrdemDeServicoEntity.class))).thenReturn(osEntityAtualizada);

        var resultado = gateway.updateOrdemDeServico(os);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, resultado.getStatus());
        assertEquals(BigDecimal.valueOf(250), resultado.getOrcamento());
        verify(repository).save(any(OrdemDeServicoEntity.class));
    }

    @Test
    public void given_ordemComPecasEServicos_when_atualizaOrdem_then_atualizaTambemPecasEServicos() {
        var os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(100));
        os.setPecas(new ArrayList<>());
        os.setServicos(new ArrayList<>());

        var osEntityExistente = new OrdemDeServicoEntity();
        osEntityExistente.setId(1L);
        osEntityExistente.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntityExistente.setSolicitacao("Problema nos freios.");
        osEntityExistente.setOrcamento(BigDecimal.valueOf(100));

        var osEntityAtualizada = new OrdemDeServicoEntity();
        osEntityAtualizada.setId(1L);
        osEntityAtualizada.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntityAtualizada.setSolicitacao("Problema nos freios.");
        osEntityAtualizada.setOrcamento(BigDecimal.valueOf(100));
        osEntityAtualizada.setPecas(new ArrayList<>());
        osEntityAtualizada.setServicos(new ArrayList<>());

        when(repository.findById(1L)).thenReturn(Optional.of(osEntityExistente));
        when(repository.save(any(OrdemDeServicoEntity.class))).thenReturn(osEntityAtualizada);

        var resultado = gateway.updateOrdemDeServico(os);

        assertNotNull(resultado);
        verify(repository).save(any(OrdemDeServicoEntity.class));
    }

    @Test
    public void given_ordemNaoExiste_when_atualizaOrdem_then_lancaExcecao() {
        var os = new OrdemDeServico();
        os.setId(999L);
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> gateway.updateOrdemDeServico(os));
        verify(repository, never()).save(any());
    }

    @Test
    public void given_ordemNula_when_atualizaOrdem_then_lancaExcecao() {
        assertThrows(NullPointerException.class, () -> gateway.updateOrdemDeServico(null));
    }

    @Test
    public void given_pecasVaziasdaOrdem_when_atualizaOrdem_then_limpaAsPecas() {
        var os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(100));
        os.setPecas(new ArrayList<>());

        var osEntityExistente = new OrdemDeServicoEntity();
        osEntityExistente.setId(1L);
        osEntityExistente.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntityExistente.setSolicitacao("Problema nos freios.");
        osEntityExistente.setOrcamento(BigDecimal.valueOf(100));

        var osEntityAtualizada = new OrdemDeServicoEntity();
        osEntityAtualizada.setId(1L);
        osEntityAtualizada.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntityAtualizada.setSolicitacao("Problema nos freios.");
        osEntityAtualizada.setOrcamento(BigDecimal.valueOf(100));
        osEntityAtualizada.setPecas(new ArrayList<>());

        when(repository.findById(1L)).thenReturn(Optional.of(osEntityExistente));
        when(repository.save(any(OrdemDeServicoEntity.class))).thenReturn(osEntityAtualizada);

        var resultado = gateway.updateOrdemDeServico(os);

        assertNotNull(resultado);
        verify(repository).save(any(OrdemDeServicoEntity.class));
    }

    // #################### deleteOrdemDeServico(OrdemDeServico os) ####################

    @Test
    public void given_ordemExiste_when_deletaOrdem_then_retornaOrdemDeletada() {
        var os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemDeServico.RECEBIDA);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(100));

        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));

        when(repository.findById(1L)).thenReturn(Optional.of(osEntity));

        var resultado = gateway.deleteOrdemDeServico(os);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(repository).delete(osEntity);
    }

    @Test
    public void given_ordemNaoExiste_when_deletaOrdem_then_retornaNulo() {
        var os = new OrdemDeServico();
        os.setId(999L);
        os.setStatus(StatusOrdemDeServico.RECEBIDA);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        var resultado = gateway.deleteOrdemDeServico(os);

        assertNull(resultado);
        verify(repository, never()).delete(any());
    }

    @Test
    public void given_ordemNula_when_deletaOrdem_then_lancaExcecao() {
        assertThrows(NullPointerException.class, () -> gateway.deleteOrdemDeServico(null));
    }

    // #################### toDomain(OrdemDeServicoEntity entity) ####################

    @Test
    public void given_entityValida_when_converteToDomain_then_retornaOrdemComTodosCampos() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));

        var resultado = OrdemDeServicoGatewayImpl.toDomain(osEntity);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, resultado.getStatus());
        assertEquals(BigDecimal.valueOf(100), resultado.getOrcamento());
        assertEquals("Problema nos freios.", resultado.getSolicitacao());
    }

    @Test
    public void given_entityNula_when_converteToDomain_then_retornaNulo() {
        var resultado = OrdemDeServicoGatewayImpl.toDomain(null);

        assertNull(resultado);
    }

    @Test
    public void given_entityComPecasEServicos_when_converteToDomain_then_mapeiaTodasAsColecoes() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));
        osEntity.setPecas(null);
        osEntity.setServicos(null);

        var resultado = OrdemDeServicoGatewayImpl.toDomain(osEntity);

        assertNotNull(resultado);
        assertNull(resultado.getPecas());
        assertNull(resultado.getServicos());
    }

    @Test
    public void given_entitySemPecas_when_converteToDomain_then_pecasRetornaNulo() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));
        osEntity.setPecas(null);

        var resultado = OrdemDeServicoGatewayImpl.toDomain(osEntity);

        assertNotNull(resultado);
        assertNull(resultado.getPecas());
    }

    @Test
    public void given_entitySemServicos_when_converteToDomain_then_servicosRetornaNulo() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));
        osEntity.setServicos(null);

        var resultado = OrdemDeServicoGatewayImpl.toDomain(osEntity);

        assertNotNull(resultado);
        assertNull(resultado.getServicos());
    }

    @Test
    public void given_entityComClienteNulo_when_converteToDomain_then_clienteRetornaNulo() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));
        osEntity.setCliente(null);

        var resultado = OrdemDeServicoGatewayImpl.toDomain(osEntity);

        assertNotNull(resultado);
        assertNull(resultado.getCliente());
    }

    @Test
    public void given_entityComVeiculoNulo_when_converteToDomain_then_veiculoRetornaNulo() {
        var osEntity = new OrdemDeServicoEntity();
        osEntity.setId(1L);
        osEntity.setStatus(StatusOrdemDeServico.RECEBIDA);
        osEntity.setSolicitacao("Problema nos freios.");
        osEntity.setOrcamento(BigDecimal.valueOf(100));
        osEntity.setVeiculo(null);

        var resultado = OrdemDeServicoGatewayImpl.toDomain(osEntity);

        assertNotNull(resultado);
        assertNull(resultado.getVeiculo());
    }

    // #################### toEntity(OrdemDeServico domain) ####################

    @Test
    public void given_domainValido_when_converteToEntity_then_retornaEntityComTodosCampos() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);

        var resultado = OrdemDeServicoGatewayImpl.toEntity(os);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, resultado.getStatus());
        assertEquals(BigDecimal.valueOf(100), resultado.getOrcamento());
        assertEquals("Problema nos freios.", resultado.getSolicitacao());
    }

    @Test
    public void given_domainNulo_when_converteToEntity_then_retornaNulo() {
        var resultado = OrdemDeServicoGatewayImpl.toEntity(null);

        assertNull(resultado);
    }

    @Test
    public void given_domainComPecasEServicos_when_converteToEntity_then_mapeiaTodasAsColecoes() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setPecas(null);
        os.setServicos(null);

        var resultado = OrdemDeServicoGatewayImpl.toEntity(os);

        assertNotNull(resultado);
        assertNull(resultado.getPecas());
        assertNull(resultado.getServicos());
    }

    @Test
    public void given_domainSemPecas_when_converteToEntity_then_pecasRetornaNulo() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        os.setPecas(null);

        var resultado = OrdemDeServicoGatewayImpl.toEntity(os);

        assertNotNull(resultado);
        assertNull(resultado.getPecas());
    }

    @Test
    public void given_domainSemServicos_when_converteToEntity_then_servicosRetornaNulo() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        os.setServicos(null);

        var resultado = OrdemDeServicoGatewayImpl.toEntity(os);

        assertNotNull(resultado);
        assertNull(resultado.getServicos());
    }

    @Test
    public void given_domainComClienteNulo_when_converteToEntity_then_clienteRetornaNulo() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        os.setCliente(null);

        var resultado = OrdemDeServicoGatewayImpl.toEntity(os);

        assertNotNull(resultado);
        assertNull(resultado.getCliente());
    }

    @Test
    public void given_domainComVeiculoNulo_when_converteToEntity_then_veiculoRetornaNulo() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        os.setVeiculo(null);

        var resultado = OrdemDeServicoGatewayImpl.toEntity(os);

        assertNotNull(resultado);
        assertNull(resultado.getVeiculo());
    }
}
