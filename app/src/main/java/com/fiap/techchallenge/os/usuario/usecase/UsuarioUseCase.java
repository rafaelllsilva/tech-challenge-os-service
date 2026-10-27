package com.fiap.techchallenge.os.usuario.usecase;

import com.fiap.techchallenge.os.cliente.domain.Cliente;
import com.fiap.techchallenge.os.usuario.inputdata.CreateUsuarioInputData;
import com.fiap.techchallenge.os.usuario.domain.Usuario;

public interface UsuarioUseCase {

    Usuario criarUsuario(CreateUsuarioInputData request);

    Usuario criarUsuarioParaCliente(Cliente cliente);

    void removerUsuarioPorCliente(Long clienteId);
}
