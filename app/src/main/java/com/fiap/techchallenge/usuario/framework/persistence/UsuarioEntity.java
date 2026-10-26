package com.fiap.techchallenge.usuario.framework.persistence;

import com.fiap.techchallenge.cliente.framework.persistence.ClienteEntity;
import com.fiap.techchallenge.usuario.domain.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuario", schema = "oficina")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private ClienteEntity cliente;

    public UsuarioEntity(String login, String senhaHash, Role role) {
        this.login = login;
        this.senhaHash = senhaHash;
        this.role = role;
    }

    public UsuarioEntity(String login, String senhaHash, Role role, ClienteEntity cliente) {
        this(login, senhaHash, role);
        this.cliente = cliente;
    }
}
