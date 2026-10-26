package com.fiap.techchallenge.usuario.usecase;

import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.usuario.inputdata.CreateUsuarioInputData;
import com.fiap.techchallenge.usuario.domain.Usuario;

public interface UsuarioUseCase {

    Usuario criarUsuario(CreateUsuarioInputData request);

    Usuario criarUsuarioParaCliente(Cliente cliente);

    void removerUsuarioPorCliente(Long clienteId);
}
