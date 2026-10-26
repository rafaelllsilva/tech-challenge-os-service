package com.fiap.techchallenge.servico.adapter.gateway;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoGatewayImplTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoGatewayImpl servicoGateway;

    private Servico servico;
    private ServicoEntity servicoEntity;
    private TipoServicoEntity tipoServicoEntity;
    private OrdemDeServicoEntity ordemDeServicoEntity;

    @BeforeEach
    void setUp() {
        TipoServico tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));
        tipoServicoEntity = new TipoServicoEntity();
        tipoServicoEntity.setId(1L);
        tipoServicoEntity.setNome("Troca de Óleo");
        tipoServicoEntity.setValor(new BigDecimal("150.00"));

        OrdemDeServico ordemDeServico = new OrdemDeServico();
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
        servicoEntity.setStatus(ServicoStatus.AGUARDANDO_INICIO);
        servicoEntity.setTipoServico(tipoServicoEntity);
        servicoEntity.setOrdemDeServico(ordemDeServicoEntity);
    }

    // --- save ---

    @Test
    void testSave_deveSalvarServicoERetornarDominio() {
        when(servicoRepository.save(any(ServicoEntity.class))).thenReturn(servicoEntity);

        Servico resultado = servicoGateway.save(servico);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, resultado.getStatus());
        verify(servicoRepository, times(1)).save(any(ServicoEntity.class));
    }

    @Test
    void testSave_deveMapearvoDomainParaEntity() {
        when(servicoRepository.save(any(ServicoEntity.class))).thenReturn(servicoEntity);

        servicoGateway.save(servico);

        verify(servicoRepository, times(1)).save(any(ServicoEntity.class));
    }

    // --- findById ---

    @Test
    void testFindById_deveRetornarServicoQuandoEncontrado() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoEntity));

        Optional<Servico> resultado = servicoGateway.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(servicoRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_deveRetornarVazioQuandoNaoEncontrado() {
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Servico> resultado = servicoGateway.findById(999L);

        assertFalse(resultado.isPresent());
        verify(servicoRepository, times(1)).findById(999L);
    }

    // --- findAll ---

    @Test
    void testFindAll_deveRetornarTodosServicos() {
        ServicoEntity servicoEntity2 = new ServicoEntity();
        servicoEntity2.setId(2L);
        servicoEntity2.setStatus(ServicoStatus.EM_EXECUCAO);
        servicoEntity2.setTipoServico(tipoServicoEntity);
        servicoEntity2.setOrdemDeServico(ordemDeServicoEntity);

        when(servicoRepository.findAll()).thenReturn(List.of(servicoEntity, servicoEntity2));

        List<Servico> resultado = servicoGateway.findAll();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        verify(servicoRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_deveRetornarListaVaziaQuandoNaoHaServicos() {
        when(servicoRepository.findAll()).thenReturn(List.of());

        List<Servico> resultado = servicoGateway.findAll();

        assertTrue(resultado.isEmpty());
        verify(servicoRepository, times(1)).findAll();
    }

    // --- findAllByOsId ---

    @Test
    void testFindAllByOsId_deveRetornarServicosFiltraPorOsId() {
        ServicoEntity servicoEntity2 = new ServicoEntity();
        servicoEntity2.setId(2L);
        servicoEntity2.setTipoServico(tipoServicoEntity);
        servicoEntity2.setOrdemDeServico(ordemDeServicoEntity);

        when(servicoRepository.findAllByOsId(1L)).thenReturn(List.of(servicoEntity, servicoEntity2));

        Optional<List<Servico>> resultado = servicoGateway.findAllByOsId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(2, resultado.get().size());
        verify(servicoRepository, times(1)).findAllByOsId(1L);
    }

    @Test
    void testFindAllByOsId_deveRetornarListaVaziaQuandoNaoExistemServicosParaOs() {
        when(servicoRepository.findAllByOsId(999L)).thenReturn(List.of());

        Optional<List<Servico>> resultado = servicoGateway.findAllByOsId(999L);

        assertTrue(resultado.isEmpty());
        verify(servicoRepository, times(1)).findAllByOsId(999L);
    }

    // --- existsByTipoServicoId ---

    @Test
    void testExistsByTipoServicoId_deveRetornarTrueQuandoExiste() {
        when(servicoRepository.existsByTipoServicoId(1L)).thenReturn(true);

        boolean resultado = servicoGateway.existsByTipoServicoId(1L);

        assertTrue(resultado);
        verify(servicoRepository, times(1)).existsByTipoServicoId(1L);
    }

    @Test
    void testExistsByTipoServicoId_deveRetornarFalseQuandoNaoExiste() {
        when(servicoRepository.existsByTipoServicoId(999L)).thenReturn(false);

        boolean resultado = servicoGateway.existsByTipoServicoId(999L);

        assertFalse(resultado);
        verify(servicoRepository, times(1)).existsByTipoServicoId(999L);
    }

    // --- delete ---

    @Test
    void testDelete_deveChamarDeleteByIdDoRepositorio() {
        servicoGateway.delete(1L);

        verify(servicoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_comIdValido_deveDeletarComSucesso() {
        servicoGateway.delete(5L);

        verify(servicoRepository, times(1)).deleteById(5L);
    }

    // --- integration scenarios ---

    @Test
    void testSaveEFindById_integracaoCompleta() {
        when(servicoRepository.save(any(ServicoEntity.class))).thenReturn(servicoEntity);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoEntity));

        Servico salvo = servicoGateway.save(servico);
        Optional<Servico> recuperado = servicoGateway.findById(salvo.getId());

        assertTrue(recuperado.isPresent());
        assertEquals(salvo.getId(), recuperado.get().getId());
        verify(servicoRepository, times(1)).save(any(ServicoEntity.class));
        verify(servicoRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAllEFiltroOsId_deveRetornarApenasDoOs() {
        when(servicoRepository.findAll()).thenReturn(List.of(servicoEntity));
        when(servicoRepository.findAllByOsId(1L)).thenReturn(List.of(servicoEntity));

        List<Servico> todos = servicoGateway.findAll();
        Optional<List<Servico>> filtrados = servicoGateway.findAllByOsId(1L);

        assertEquals(1, todos.size());
        assertTrue(filtrados.isPresent());
        assertEquals(1, filtrados.get().size());
        verify(servicoRepository, times(1)).findAll();
        verify(servicoRepository, times(1)).findAllByOsId(1L);
    }
}
