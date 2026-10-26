package com.fiap.techchallenge.usuario.usecase;

import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.usecase.ClienteGateway;
import com.fiap.techchallenge.usuario.inputdata.CreateUsuarioInputData;
import com.fiap.techchallenge.usuario.domain.Role;
import com.fiap.techchallenge.usuario.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioInteractor implements UsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(UsuarioInteractor.class);

    private final UsuarioGateway usuarioGateway;
    private final ClienteGateway clienteGateway;
    private final PasswordEncoder passwordEncoder;

    public UsuarioInteractor(UsuarioGateway usuarioGateway,
                             ClienteGateway clienteGateway,
                             PasswordEncoder passwordEncoder) {
        this.usuarioGateway = usuarioGateway;
        this.clienteGateway = clienteGateway;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario criarUsuario(CreateUsuarioInputData request) {
        if (usuarioGateway.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException("Login já está em uso: " + request.getLogin());
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());
        String hash = passwordEncoder.encode(request.getSenha());

        Usuario usuario = new Usuario(request.getLogin(), hash, role);

        if (request.getClienteId() != null) {
            Optional<Cliente> clienteOpt = clienteGateway.findById(request.getClienteId());
            if (clienteOpt.isEmpty()) {
                throw new IllegalArgumentException("Cliente não encontrado: " + request.getClienteId());
            }
            usuario.setCliente(clienteOpt.get());
        }

        return usuarioGateway.save(usuario);
    }

    @Override
    public Usuario criarUsuarioParaCliente(Cliente cliente) {
        String login = cliente.getDocumento();
        if (usuarioGateway.existsByLogin(login)) {
            log.warn("Usuário para o cliente {} já existe, ignorando criação.", login);
            return usuarioGateway.findByLogin(login).orElseThrow();
        }

        String hash = passwordEncoder.encode(login); // senha temporária = documento
        Usuario usuario = new Usuario(login, hash, Role.CLIENTE, cliente);
        return usuarioGateway.save(usuario);
    }

    @Override
    public void removerUsuarioPorCliente(Long clienteId) {
        usuarioGateway.deleteByClienteId(clienteId);
        log.info("Usuário associado ao cliente {} removido com sucesso.", clienteId);
    }
}
