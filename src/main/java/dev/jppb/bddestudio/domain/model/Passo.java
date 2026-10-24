package dev.jppb.bddestudio.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "passo")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Passo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPasso tipo;

    @Column(nullable = false) private String texto;
    @Column(nullable = false) private int posicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cenario_id")
    private Cenario cenario;

    public Passo(TipoPasso tipo, String texto, int posicao) {
        this.tipo = tipo;
        this.texto = texto;
        this.posicao = posicao;
    }

}