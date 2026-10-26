package com.fiap.techchallenge.cliente.framework.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cliente")
public class ClienteEntity {

    public enum TipoPessoaEntity {
        PF, PJ;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 2)
    private TipoPessoaEntity tipoPessoa;

    @Column(nullable = false, length = 18, unique = true)
    private String documento;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 150)
    private String email;

    public ClienteEntity(Long id, String nome, TipoPessoaEntity tipoPessoa, String documento, LocalDate dataNascimento, String email) {
        this.id = id;
        this.nome = nome;
        this.tipoPessoa = tipoPessoa;
        this.documento = documento;
        this.dataNascimento = dataNascimento;
        this.email = email;
    }
}
