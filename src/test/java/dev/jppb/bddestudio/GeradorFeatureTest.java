package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.service.GeradorFeature;
import dev.jppb.bddestudio.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorFeatureTest {

    private HistoriaUsuario historia(String titulo, Prioridade prioridade) {
        Projeto p = new Projeto("Biblioteca", null, null);
        p.setId(1L);
        Usuario autor = new Usuario("Ana", "ana@exemplo.com");
        autor.setId(1L);
        HistoriaUsuario h = new HistoriaUsuario(p, autor, titulo, "estudante",
                "reservar um livro indisponível", "garantir o acesso", prioridade);
        h.setId(10L);
        return h;
    }

    private Cenario cenario(HistoriaUsuario h, String titulo, int posicao,
                            OrigemCenario origem, Passo... passos) {
        Cenario c = new Cenario(h, titulo, posicao, origem);
        for (Passo passo : passos) {
            c.adicionarPasso(passo);
        }
        return c;
    }

    @Test
    void gera_featureCompleta() {
        HistoriaUsuario h = historia("Reservar livro", Prioridade.ALTA);
        Cenario c = cenario(h, "Limite de reservas", 1, OrigemCenario.HUMANO,
                new Passo(TipoPasso.DADO, "que o estudante já possui 3 reservas", 1),
                new Passo(TipoPasso.QUANDO, "ele tenta reservar outro livro", 2),
                new Passo(TipoPasso.ENTAO, "o sistema recusa a reserva", 3),
                new Passo(TipoPasso.E, "informa o limite", 4));

        String esperado = String.join("\n",
                "# language: pt",
                "@prioridade-alta",
                "Funcionalidade: Reservar livro",
                "  Como estudante",
                "  Quero reservar um livro indisponível",
                "  Para garantir o acesso",
                "",
                "  Cenário: Limite de reservas",
                "    Dado que o estudante já possui 3 reservas",
                "    Quando ele tenta reservar outro livro",
                "    Então o sistema recusa a reserva",
                "    E informa o limite",
                "");

        assertThat(GeradorFeature.gerar(h, List.of(c))).isEqualTo(esperado);
    }

    @Test
    void gera_marcaCenarioDeSugestaoComTag() {
        HistoriaUsuario h = historia("Reservar livro", Prioridade.MEDIA);
        Cenario c = cenario(h, "Fila de espera", 1, OrigemCenario.SUGESTAO_IA,
                new Passo(TipoPasso.QUANDO, "a", 1), new Passo(TipoPasso.ENTAO, "b", 2));

        assertThat(GeradorFeature.gerar(h, List.of(c)))
                .contains("@prioridade-media")
                .contains("  @sugestao-ia\n  Cenário: Fila de espera\n");
    }

    @Test
    void gera_ordenaCenariosEPassosPelaPosicao() {
        HistoriaUsuario h = historia("Reservar livro", Prioridade.ALTA);
        Cenario segundo = cenario(h, "A", 2, OrigemCenario.HUMANO,
                new Passo(TipoPasso.ENTAO, "c", 3),
                new Passo(TipoPasso.DADO, "a", 1),
                new Passo(TipoPasso.QUANDO, "b", 2));
        Cenario primeiro = cenario(h, "B", 1, OrigemCenario.HUMANO,
                new Passo(TipoPasso.QUANDO, "x", 1), new Passo(TipoPasso.ENTAO, "y", 2));

        String feature = GeradorFeature.gerar(h, List.of(segundo, primeiro));

        assertThat(feature.indexOf("Cenário: B")).isLessThan(feature.indexOf("Cenário: A"));
        assertThat(feature).containsSubsequence("Dado a", "Quando b", "Então c");
    }

    @Test
    void gera_quebraDeLinhaNoTextoNaoCriaLinhaDeGherkin() {
        HistoriaUsuario h = historia("Reservar livro", Prioridade.ALTA);
        Cenario c = cenario(h, "Com quebra", 1, OrigemCenario.HUMANO,
                new Passo(TipoPasso.QUANDO, "ele reserva", 1),
                new Passo(TipoPasso.ENTAO, "o sistema responde\nCenário: falso", 2));

        String feature = GeradorFeature.gerar(h, List.of(c));

        assertThat(feature).contains("    Então o sistema responde Cenário: falso\n");
        assertThat(feature).doesNotContain("\nCenário: falso");
    }

    @Test
    void nomeDoArquivo_semAcentoNemSimbolo_comFallbackPeloId() {
        assertThat(GeradorFeature.nomeDoArquivo(historia("Reservar livro", Prioridade.ALTA)))
                .isEqualTo("reservar-livro.feature");
        assertThat(GeradorFeature.nomeDoArquivo(historia("Ação rápida!", Prioridade.ALTA)))
                .isEqualTo("acao-rapida.feature");
        assertThat(GeradorFeature.nomeDoArquivo(historia("???", Prioridade.ALTA)))
                .isEqualTo("historia-10.feature");
    }
}
