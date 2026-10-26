package dev.jppb.bddestudio.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historia_usuario", uniqueConstraints = @UniqueConstraint (columnNames = {"projeto_id", "titulo"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String titulo;
    @Column(nullable = false)
    private String papel;
    @Column(nullable = false)
    private String objetivo;
    @Column(nullable = false)
    private String beneficio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id")
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Cenario> cenarios = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();
    private LocalDateTime atualizadaEm = LocalDateTime.now();

    public void adicionarCenario(Cenario c){
        c.setHistoria(this);
        cenarios.add(c);
    }
    public HistoriaUsuario(Projeto projeto, Usuario autor, String titulo,
                           String papel, String objetivo, String beneficio,
                           Prioridade prioridade) {
        this.projeto = projeto;
        this.autor = autor;
        this.titulo = titulo;
        this.papel = papel;
        this.objetivo = objetivo;
        this.beneficio = beneficio;
        this.prioridade = prioridade;
    }

    public void atualizar(String titulo, String papel, String objetivo,
                          String beneficio, Prioridade prioridade) {
        this.titulo = titulo;
        this.papel = papel;
        this.objetivo = objetivo;
        this.beneficio = beneficio;
        this.prioridade = prioridade;
        this.atualizadaEm = LocalDateTime.now();
    }

    public boolean foiEscritaPor(Long usuarioId) {
        return autor.getId().equals(usuarioId);
    }
}
