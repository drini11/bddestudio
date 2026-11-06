package dev.jppb.bddestudio.domain.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cenario", uniqueConstraints = @UniqueConstraint(columnNames = {"historia_id", "titulo"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private int posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemCenario origem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id")
    private HistoriaUsuario historia;

    @OneToMany(mappedBy = "cenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Passo> passos = new ArrayList<>();

    public void adicionarPasso(Passo p) {
        p.setCenario(this);
        passos.add(p);
    }

    public Cenario(String titulo, int posicao, OrigemCenario origem) {
        this.titulo = titulo;
        this.posicao = posicao;
        this.origem = origem;
    }

    public Cenario(HistoriaUsuario historia, String titulo, int posicao, OrigemCenario origem) {
        this(titulo, posicao, origem);
        this.historia = historia;
    }


    public void atualizar(String titulo, OrigemCenario origem) {
        this.titulo = titulo;
        this.origem = origem;
    }

    public void substituirPassos(List<Passo> novos) {
        passos.clear();
        novos.forEach(this::adicionarPasso);
    }
}
