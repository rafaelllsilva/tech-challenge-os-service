package com.fiap.techchallenge.cliente.usecase;

import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import com.fiap.techchallenge.cliente.framework.persistence.ClienteEntity;
import com.fiap.techchallenge.cliente.framework.persistence.ClienteJpaRepository;
import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.usuario.usecase.UsuarioUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteInteractorTest {

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private UsuarioUseCase usuarioUseCase;

    @InjectMocks
    private ClienteInteractor clienteInteractor;

    private Cliente exemploCliente() {
        return new Cliente(1L, "Maria Silva", TipoPessoa.PF, "52998224725", LocalDate.of(1990, 5, 15), null);
    }

    @Test
    void create_salvaERetornaCliente() {
        Cliente clienteMock = exemploCliente();
        when(clienteGateway.save(any(Cliente.class))).thenReturn(clienteMock);
        
        var result = clienteInteractor.create("Maria Silva", TipoPessoa.PF, "52998224725", LocalDate.of(1990, 5, 15), null);

        verify(clienteGateway).save(any(Cliente.class));
        verify(usuarioUseCase).criarUsuarioParaCliente(any(Cliente.class));
        assertThat(result.getNome()).isEqualTo("Maria Silva");
        assertThat(result.getTipoPessoa()).isEqualTo(TipoPessoa.PF);
        assertThat(result.getDocumento()).isEqualTo("52998224725");
    }

    @Test
    void create_cpfInvalido_lancaExcecao() {
        assertThatThrownBy(() -> clienteInteractor.create("Maria Silva", TipoPessoa.PF, "00000000000", LocalDate.of(1990, 5, 15), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF inválido");

        verify(clienteGateway, never()).save(any());
    }

    @Test
    void getAll_retornaTodosMapeados() {
        when(clienteGateway.findAll()).thenReturn(List.of(exemploCliente()));

        var result = clienteInteractor.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Maria Silva");
    }

    @Test
    void getById_encontrado_retornaCliente() {
        when(clienteGateway.findById(1L)).thenReturn(Optional.of(exemploCliente()));

        var result = clienteInteractor.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getByDocumento_encontrado_retornaCliente() {
        when(clienteGateway.findByDocumento("52998224725")).thenReturn(Optional.of(exemploCliente()));

        var result = clienteInteractor.getByDocumento("52998224725");

        assertThat(result).isPresent();
        assertThat(result.get().getDocumento()).isEqualTo("52998224725");
    }

    @Test
    void updateById_encontrado_atualizaTodosOsCamposERetornaCliente() {
        Cliente cliente = exemploCliente();
        when(clienteGateway.findById(1L)).thenReturn(Optional.of(cliente));
        
        Cliente atualizado = new Cliente(1L, "Maria Souza", TipoPessoa.PJ, "11222333000181", LocalDate.of(2000, 1, 1), "souza@empresa.com");
        when(clienteGateway.save(any(Cliente.class))).thenReturn(atualizado);

        var result = clienteInteractor.updateById(1L, "Maria Souza", TipoPessoa.PJ, "11222333000181", LocalDate.of(2000, 1, 1), "souza@empresa.com");

        verify(clienteGateway).save(cliente);
        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("Maria Souza");
        assertThat(result.getTipoPessoa()).isEqualTo(TipoPessoa.PJ);
        assertThat(result.getDocumento()).isEqualTo("11222333000181");
        assertThat(result.getEmail()).isEqualTo("souza@empresa.com");
    }

    @Test
    void updateById_naoEncontrado_lancaExcecao() {
        when(clienteGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteInteractor.updateById(99L, "Nome", TipoPessoa.PF, "52998224725", null, null))
                .isInstanceOf(NotFoundException.class);

        verify(clienteGateway, never()).save(any());
    }

    @Test
    void deleteById_encontrado_deletaERetornaCliente() {
        Cliente cliente = exemploCliente();
        when(clienteGateway.findById(1L)).thenReturn(Optional.of(cliente));

        var result = clienteInteractor.deleteById(1L);

        verify(usuarioUseCase).removerUsuarioPorCliente(1L);
        verify(clienteGateway).delete(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
}
