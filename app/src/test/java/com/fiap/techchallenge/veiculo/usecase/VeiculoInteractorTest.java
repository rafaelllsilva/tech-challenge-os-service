package com.fiap.techchallenge.veiculo.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoInteractorTest {

    @Mock
    private VeiculoGateway gateway;

    @InjectMocks
    private VeiculoInteractor useCase;

    private Veiculo sampleVeiculo() {
        return new Veiculo(1L, "ABC1234", "Toyota", "Corolla", 2020);
    }

    @Test
    void create_savesAndReturnsVeiculo() {
        Veiculo saved = sampleVeiculo();
        when(gateway.save(any())).thenReturn(saved);

        Veiculo result = useCase.create("ABC1234", "Toyota", "Corolla", 2020);

        verify(gateway).save(any(Veiculo.class));
        assertThat(result.getPlaca()).isEqualTo("ABC1234");
        assertThat(result.getMarca()).isEqualTo("Toyota");
        assertThat(result.getModelo()).isEqualTo("Corolla");
        assertThat(result.getAno()).isEqualTo(2020);
    }

    @Test
    void getAll_returnsAllVeiculos() {
        when(gateway.findAll()).thenReturn(List.of(sampleVeiculo()));

        List<Veiculo> result = useCase.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void getAll_empty_returnsEmptyList() {
        when(gateway.findAll()).thenReturn(List.of());

        assertThat(useCase.getAll()).isEmpty();
    }

    @Test
    void getById_found_returnsMapped() {
        when(gateway.findById(1L)).thenReturn(Optional.of(sampleVeiculo()));

        Optional<Veiculo> result = useCase.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getById_notFound_returnsEmpty() {
        when(gateway.findById(99L)).thenReturn(Optional.empty());

        assertThat(useCase.getById(99L)).isEmpty();
    }

    @Test
    void getByPlaca_found_returnsMapped() {
        when(gateway.findByPlaca("ABC1234")).thenReturn(Optional.of(sampleVeiculo()));

        Optional<Veiculo> result = useCase.getByPlaca("ABC1234");

        assertThat(result).isPresent();
        assertThat(result.get().getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void getByPlaca_notFound_returnsEmpty() {
        when(gateway.findByPlaca("ZZZ9999")).thenReturn(Optional.empty());

        assertThat(useCase.getByPlaca("ZZZ9999")).isEmpty();
    }

    @Test
    void updateById_found_updatesAllFieldsAndReturns() {
        Veiculo existing = sampleVeiculo();
        when(gateway.findById(1L)).thenReturn(Optional.of(existing));
        when(gateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Veiculo result = useCase.updateById(1L, "XYZ1A34", "Honda", "Civic", 2022);

        verify(gateway).save(existing);
        assertThat(result.getMarca()).isEqualTo("Honda");
        assertThat(result.getModelo()).isEqualTo("Civic");
        assertThat(result.getPlaca()).isEqualTo("XYZ1A34");
        assertThat(result.getAno()).isEqualTo(2022);
    }

    @Test
    void updateById_nullFields_doesNotOverwrite() {
        Veiculo existing = sampleVeiculo();
        when(gateway.findById(1L)).thenReturn(Optional.of(existing));
        when(gateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Veiculo result = useCase.updateById(1L, null, null, null, null);

        assertThat(result.getMarca()).isEqualTo("Toyota");
        assertThat(result.getModelo()).isEqualTo("Corolla");
    }

    @Test
    void updateById_notFound_throwsNotFoundException() {
        when(gateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateById(99L, null, null, null, null))
                .isInstanceOf(NotFoundException.class);
        verify(gateway, never()).save(any());
    }

    @Test
    void updateByPlaca_found_updatesAndReturns() {
        Veiculo existing = sampleVeiculo();
        when(gateway.findByPlaca("ABC1234")).thenReturn(Optional.of(existing));
        when(gateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Veiculo result = useCase.updateByPlaca("ABC1234", "Ford", "Fiesta", 2021);

        verify(gateway).save(existing);
        assertThat(result.getMarca()).isEqualTo("Ford");
        assertThat(result.getAno()).isEqualTo(2021);
    }

    @Test
    void updateByPlaca_notFound_throwsNotFoundException() {
        when(gateway.findByPlaca("ZZZ9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateByPlaca("ZZZ9999", null, null, null))
                .isInstanceOf(NotFoundException.class);
        verify(gateway, never()).save(any());
    }

    @Test
    void deleteById_found_deletesAndReturns() {
        Veiculo existing = sampleVeiculo();
        when(gateway.findById(1L)).thenReturn(Optional.of(existing));

        Veiculo result = useCase.deleteById(1L);

        verify(gateway).delete(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void deleteById_notFound_throwsNotFoundException() {
        when(gateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deleteById(99L))
                .isInstanceOf(NotFoundException.class);
        verify(gateway, never()).delete(any());
    }

    @Test
    void deleteByPlaca_found_deletesAndReturns() {
        Veiculo existing = sampleVeiculo();
        when(gateway.findByPlaca("ABC1234")).thenReturn(Optional.of(existing));

        Veiculo result = useCase.deleteByPlaca("ABC1234");

        verify(gateway).delete(1L);
        assertThat(result.getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void deleteByPlaca_notFound_throwsNotFoundException() {
        when(gateway.findByPlaca("ZZZ9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deleteByPlaca("ZZZ9999"))
                .isInstanceOf(NotFoundException.class);
        verify(gateway, never()).delete(any());
    }
}
