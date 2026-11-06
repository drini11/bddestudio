package dev.jppb.bddestudio.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sugestao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sugestao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id")
    private HistoriaUsuario historia;

    @Column(nullable = false) private String ferramenta;
    @Column(nullable = false, columnDefinition = "TEXT") private String prompt;
    @Column(nullable = false, columnDefinition = "TEXT") private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisaoSugestao decisao = DecisaoSugestao.PENDENTE;

    @Enumerated(EnumType.STRING)
    private MotivoRejeicao motivoRejeicao;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradaEm = LocalDateTime.now();
    private LocalDateTime decididaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_id")
    private Usuario registradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decidida_por_id")
    private Usuario decididaPor;

    public Sugestao(HistoriaUsuario historia, Usuario registradaPor,
                    String ferramenta, String prompt, String conteudo) {
        this.historia = historia;
        this.registradaPor = registradaPor;
        this.ferramenta = ferramenta;
        this.prompt = prompt;
        this.conteudo = conteudo;
    }

    public boolean estaPendente() {
        return decisao == DecisaoSugestao.PENDENTE;
    }

    public void aceitar(Usuario quem, String justificativa) {
        this.decisao = DecisaoSugestao.ACEITA;
        this.decididaPor = quem;
        this.decididaEm = LocalDateTime.now();
        this.justificativa = justificativa;
    }

    public void rejeitar(Usuario quem, MotivoRejeicao motivo, String justificativa) {
        this.decisao = DecisaoSugestao.REJEITADA;
        this.motivoRejeicao = motivo;
        this.decididaPor = quem;
        this.decididaEm = LocalDateTime.now();
        this.justificativa = justificativa;
    }
}