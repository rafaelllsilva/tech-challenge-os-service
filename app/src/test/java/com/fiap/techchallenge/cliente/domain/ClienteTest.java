package com.fiap.techchallenge.cliente.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    @Test
    public void given_construtorPadrao_when_criaCliente_then_camposSaoNulos() {
        Cliente cliente = new Cliente();
        assertNull(cliente.getId());
        assertNull(cliente.getNome());
    }

    @Test
    public void given_parametrosValidos_when_criaClientePF_then_camposPreenchidosCorretamente() {
        LocalDate dataNascimento = LocalDate.of(1990, 5, 15);
        Cliente cliente = new Cliente(1L, "Maria Silva", TipoPessoa.PF, "52998224725", dataNascimento, null);

        assertEquals("Maria Silva", cliente.getNome());
        assertEquals(TipoPessoa.PF, cliente.getTipoPessoa());
        assertEquals("52998224725", cliente.getDocumento());
    }
}
