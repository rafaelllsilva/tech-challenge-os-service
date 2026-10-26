package com.fiap.techchallenge.cliente.usecase;

import com.fiap.techchallenge.cliente.adapter.controller.dto.validator.DocumentoValidator;
import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.usuario.usecase.UsuarioUseCase;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ClienteInteractor implements ClienteUseCase {

    private final ClienteGateway gateway;
    private final UsuarioUseCase usuarioUseCase;

    public ClienteInteractor(ClienteGateway gateway,
            @Lazy UsuarioUseCase usuarioUseCase) {
        this.gateway = gateway;
        this.usuarioUseCase = usuarioUseCase;
    }

    @Override
    @Transactional
    public Cliente create(String nome, TipoPessoa tipoPessoa, String documento, LocalDate dataNascimento,
            String email) {
        // Valida o documento (CPF ou CNPJ) antes de salvar
        DocumentoValidator.validarDocumento(tipoPessoa, documento);

        String documentoNormalizado = DocumentoValidator.normalizarDocumento(documento);
        Cliente cliente = new Cliente(null, nome, tipoPessoa, documentoNormalizado, dataNascimento, email);
        Cliente clienteSalvo = gateway.save(cliente);

        // Cria automaticamente um usuário com role CLIENTE vinculado a este Cliente
        // Login = documento; senha = documento;
        usuarioUseCase.criarUsuarioParaCliente(clienteSalvo);
        log.info("Usuário CLIENTE criado automaticamente para documento {}", clienteSalvo.getDocumento());

        return clienteSalvo;
    }

    @Override
    public List<Cliente> getAll() {
        return gateway.findAll();
    }

    @Override
    public Optional<Cliente> getById(Long id) {
        return gateway.findById(id);
    }

    @Override
    public Optional<Cliente> getByDocumento(String documento) {
        return gateway.findByDocumento(DocumentoValidator.normalizarDocumento(documento));
    }

    @Override
    public Cliente updateById(Long id, String nome, TipoPessoa tipoPessoa, String documento, LocalDate dataNascimento,
            String email) {
        // Valida o documento (CPF ou CNPJ) se informado no update
        DocumentoValidator.validarDocumento(tipoPessoa, documento);

        Cliente cliente = gateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cliente com id {} não encontrado", id);
                    return new NotFoundException("Cliente com id " + id + " não encontrado");
                });

        if (nome != null)
            cliente.setNome(nome);
        if (tipoPessoa != null)
            cliente.setTipoPessoa(tipoPessoa);
        if (documento != null)
            cliente.setDocumento(DocumentoValidator.normalizarDocumento(documento));
        if (dataNascimento != null)
            cliente.setDataNascimento(dataNascimento);
        if (email != null)
            cliente.setEmail(email);

        return gateway.save(cliente);
    }

    @Override
    @Transactional
    public Cliente deleteById(Long id) {
        Cliente cliente = gateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cliente com id {} não encontrado", id);
                    return new NotFoundException("Cliente com id " + id + " não encontrado");
                });
        usuarioUseCase.removerUsuarioPorCliente(id);
        gateway.delete(id);
        return cliente;
    }
}
