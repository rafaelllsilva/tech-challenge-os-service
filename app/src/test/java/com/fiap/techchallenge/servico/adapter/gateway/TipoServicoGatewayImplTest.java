package com.fiap.techchallenge.servico.adapter.gateway;

import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.repository.TipoServicoRepository;
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
class TipoServicoGatewayImplTest {

    @Mock
    private TipoServicoRepository tipoServicoRepository;

    @InjectMocks
    private TipoServicoGatewayImpl tipoServicoGateway;

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

    // --- findTempoMedioPorTipoServico ---

    @Test
    void testFindTempoMedioPorTipoServico_deveRetornarTodosTemposMedios() {
        TempoMedioServicoDTO dto1 = new TempoMedioServicoDTO("Troca de Óleo", new BigDecimal("30"));
        TempoMedioServicoDTO dto2 = new TempoMedioServicoDTO("Alinhamento", new BigDecimal("45"));

        when(tipoServicoRepository.findTempoMedioPorTipoServico()).thenReturn(List.of(dto1, dto2));

        List<TempoMedioServicoDTO> resultado = tipoServicoGateway.findTempoMedioPorTipoServico();

        assertEquals(2, resultado.size());
        assertEquals("Troca de Óleo", resultado.get(0).getNome());
        assertEquals(new BigDecimal("30"), resultado.get(0).getTempoMedioMinutos());
        verify(tipoServicoRepository, times(1)).findTempoMedioPorTipoServico();
    }

    @Test
    void testFindTempoMedioPorTipoServico_deveRetornarListaVaziaQuandoNaoHaServicos() {
        when(tipoServicoRepository.findTempoMedioPorTipoServico()).thenReturn(List.of());

        List<TempoMedioServicoDTO> resultado = tipoServicoGateway.findTempoMedioPorTipoServico();

        assertTrue(resultado.isEmpty());
        verify(tipoServicoRepository, times(1)).findTempoMedioPorTipoServico();
    }

    // --- findTempoMedioPorTipoServicoId ---

    @Test
    void testFindTempoMedioPorTipoServicoId_deveRetornarTempoMedioDoTipo() {
        TempoMedioServicoDTO dto = new TempoMedioServicoDTO("Troca de Óleo", new BigDecimal("30"));

        when(tipoServicoRepository.findTempoMedioPorTipoServicoId(1L)).thenReturn(Optional.of(dto));

        Optional<TempoMedioServicoDTO> resultado = tipoServicoGateway.findTempoMedioPorTipoServicoId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Troca de Óleo", resultado.get().getNome());
        assertEquals(new BigDecimal("30"), resultado.get().getTempoMedioMinutos());
        verify(tipoServicoRepository, times(1)).findTempoMedioPorTipoServicoId(1L);
    }

    @Test
    void testFindTempoMedioPorTipoServicoId_deveRetornarVazioQuandoNaoEncontrado() {
        when(tipoServicoRepository.findTempoMedioPorTipoServicoId(999L)).thenReturn(Optional.empty());

        Optional<TempoMedioServicoDTO> resultado = tipoServicoGateway.findTempoMedioPorTipoServicoId(999L);

        assertFalse(resultado.isPresent());
        verify(tipoServicoRepository, times(1)).findTempoMedioPorTipoServicoId(999L);
    }

    // --- save ---

    @Test
    void testSave_deveSalvarTipoServicoERetornarDominio() {
        when(tipoServicoRepository.save(any(TipoServicoEntity.class))).thenReturn(tipoServicoEntity);

        TipoServico resultado = tipoServicoGateway.save(tipoServico);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Troca de Óleo", resultado.getNome());
        assertEquals(new BigDecimal("150.00"), resultado.getValor());
        verify(tipoServicoRepository, times(1)).save(any(TipoServicoEntity.class));
    }

    // --- findAll ---

    @Test
    void testFindAll_deveRetornarTodosTiposServico() {
        TipoServicoEntity tipoServicoEntity2 = new TipoServicoEntity();
        tipoServicoEntity2.setId(2L);
        tipoServicoEntity2.setNome("Alinhamento");
        tipoServicoEntity2.setValor(new BigDecimal("200.00"));

        when(tipoServicoRepository.findAll()).thenReturn(List.of(tipoServicoEntity, tipoServicoEntity2));

        List<TipoServico> resultado = tipoServicoGateway.findAll();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        verify(tipoServicoRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_deveRetornarListaVaziaQuandoNaoHaTiposServico() {
        when(tipoServicoRepository.findAll()).thenReturn(List.of());

        List<TipoServico> resultado = tipoServicoGateway.findAll();

        assertTrue(resultado.isEmpty());
        verify(tipoServicoRepository, times(1)).findAll();
    }

    // --- findById ---

    @Test
    void testFindById_deveRetornarTipoServicoQuandoEncontrado() {
        when(tipoServicoRepository.findById(1L)).thenReturn(Optional.of(tipoServicoEntity));

        Optional<TipoServico> resultado = tipoServicoGateway.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals("Troca de Óleo", resultado.get().getNome());
        verify(tipoServicoRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_deveRetornarVazioQuandoNaoEncontrado() {
        when(tipoServicoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<TipoServico> resultado = tipoServicoGateway.findById(999L);

        assertFalse(resultado.isPresent());
        verify(tipoServicoRepository, times(1)).findById(999L);
    }

    // --- delete ---

    @Test
    void testDelete_deveChamarDeleteByIdDoRepositorio() {
        tipoServicoGateway.delete(1L);

        verify(tipoServicoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_comIdValido_deveDeletarComSucesso() {
        tipoServicoGateway.delete(5L);

        verify(tipoServicoRepository, times(1)).deleteById(5L);
    }

    // --- integration scenarios ---

    @Test
    void testSaveEFindById_integracaoCompleta() {
        when(tipoServicoRepository.save(any(TipoServicoEntity.class))).thenReturn(tipoServicoEntity);
        when(tipoServicoRepository.findById(1L)).thenReturn(Optional.of(tipoServicoEntity));

        TipoServico salvo = tipoServicoGateway.save(tipoServico);
        Optional<TipoServico> recuperado = tipoServicoGateway.findById(salvo.getId());

        assertTrue(recuperado.isPresent());
        assertEquals(salvo.getId(), recuperado.get().getId());
        verify(tipoServicoRepository, times(1)).save(any(TipoServicoEntity.class));
        verify(tipoServicoRepository, times(1)).findById(1L);
    }

    @Test
    void testFindTempoMedioEFindAll_comDadosCompletos() {
        TipoServicoEntity tipoServicoEntity2 = new TipoServicoEntity();
        tipoServicoEntity2.setId(2L);
        tipoServicoEntity2.setNome("Alinhamento");

        when(tipoServicoRepository.findAll()).thenReturn(List.of(tipoServicoEntity, tipoServicoEntity2));
        when(tipoServicoRepository.findTempoMedioPorTipoServico()).thenReturn(
                List.of(
                        new TempoMedioServicoDTO("Troca de Óleo", new BigDecimal("30")),
                        new TempoMedioServicoDTO("Alinhamento", new BigDecimal("45"))
                )
        );

        List<TipoServico> todos = tipoServicoGateway.findAll();
        List<TempoMedioServicoDTO> tempos = tipoServicoGateway.findTempoMedioPorTipoServico();

        assertEquals(2, todos.size());
        assertEquals(2, tempos.size());
        verify(tipoServicoRepository, times(1)).findAll();
        verify(tipoServicoRepository, times(1)).findTempoMedioPorTipoServico();
    }
}
