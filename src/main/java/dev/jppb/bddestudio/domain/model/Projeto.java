package dev.jppb.bddestudio.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "projeto")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String contextoDominio;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Projeto(String nome, String descricao, String contextoDominio) {
        this.nome = nome;
        this.descricao = descricao;
        this.contextoDominio = contextoDominio;
    }

    public void atualizar(String nome, String descricao, String contextoDominio) {
        this.nome = nome;
        this.descricao = descricao;
        this.contextoDominio = contextoDominio;
    }
}
