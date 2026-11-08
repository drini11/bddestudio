package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.domain.model.*;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class GeradorFeature {

    private GeradorFeature() {
    }

    public static String gerar(HistoriaUsuario historia, List<Cenario> cenarios) {
        StringBuilder gherkin = new StringBuilder();
        gherkin.append("# language: pt\n");
        gherkin.append("@prioridade-")
                .append(historia.getPrioridade().name().toLowerCase(Locale.ROOT)).append('\n');
        gherkin.append("Funcionalidade: ").append(linha(historia.getTitulo())).append('\n');
        gherkin.append("  Como ").append(linha(historia.getPapel())).append('\n');
        gherkin.append("  Quero ").append(linha(historia.getObjetivo())).append('\n');
        gherkin.append("  Para ").append(linha(historia.getBeneficio())).append('\n');

        cenarios.stream().sorted(Comparator.comparingInt(Cenario::getPosicao)).forEach(c -> {
            gherkin.append('\n');
            if (c.getOrigem() == OrigemCenario.SUGESTAO_IA) {
                gherkin.append("  @sugestao-ia\n");
            }
            gherkin.append("  Cenário: ").append(linha(c.getTitulo())).append('\n');
            c.getPassos().stream().sorted(Comparator.comparingInt(Passo::getPosicao)).forEach(p ->
                    gherkin.append("    ").append(palavra(p.getTipo())).append(' ')
                            .append(linha(p.getTexto())).append('\n'));
        });
        return gherkin.toString();
    }

    /** Nome do arquivo a partir do título, sem acento nem símbolo (ex.: "Reservar livro" -> reservar-livro.feature). */
    public static String nomeDoArquivo(HistoriaUsuario historia) {
        String slug = Normalizer.normalize(historia.getTitulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return (slug.isEmpty() ? "historia-" + historia.getId() : slug) + ".feature";
    }

    /** Uma linha só: quebras de linha viram espaço, para o texto não criar linhas de Gherkin. */
    private static String linha(String texto) {
        return texto.replaceAll("\\s*\\R\\s*", " ").strip();
    }

    private static String palavra(TipoPasso tipo) {
        return switch (tipo) {
            case DADO -> "Dado";
            case QUANDO -> "Quando";
            case ENTAO -> "Então";
            case E -> "E";
            case MAS -> "Mas";
        };
    }
}
