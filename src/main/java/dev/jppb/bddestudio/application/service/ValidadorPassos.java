package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.RegraNegocioException;
import dev.jppb.bddestudio.domain.model.TipoPasso;
import java.util.List;

/** Regra 2: ordem Dado -> Quando -> Então; E/Mas continuam o tipo anterior; Quando e Então obrigatórios. */
public final class ValidadorPassos {

    private ValidadorPassos() {
    }

    public static void validar(List<DadosPasso> passos) {
        TipoPasso efetivo = null;
        boolean temQuando = false;
        boolean temEntao = false;

        for (int i = 0; i < passos.size(); i++) {
            TipoPasso declarado = passos.get(i).tipo();
            boolean continuacao = declarado == TipoPasso.E || declarado == TipoPasso.MAS;

            if (continuacao && efetivo == null) {
                throw new RegraNegocioException(
                        "O cenário não pode começar com um passo " + rotulo(declarado));
            }
            TipoPasso atual = continuacao ? efetivo : declarado;

            if (efetivo != null && fase(atual) < fase(efetivo)) {
                throw new RegraNegocioException("Passo " + (i + 1) + ": " + rotulo(atual)
                        + " não pode vir depois de " + rotulo(efetivo)
                        + " (a ordem é Dado, Quando, Então)");
            }
            efetivo = atual;
            temQuando |= atual == TipoPasso.QUANDO;
            temEntao |= atual == TipoPasso.ENTAO;
        }

        if (!temQuando) {
            throw new RegraNegocioException("O cenário precisa ter ao menos um passo Quando");
        }
        if (!temEntao) {
            throw new RegraNegocioException("O cenário precisa ter ao menos um passo Então");
        }
    }

    private static int fase(TipoPasso tipo) {
        return switch (tipo) {
            case DADO -> 0;
            case QUANDO -> 1;
            case ENTAO -> 2;
            default -> throw new IllegalArgumentException("Tipo sem fase própria: " + tipo);
        };
    }

    private static String rotulo(TipoPasso tipo) {
        return switch (tipo) {
            case DADO -> "Dado";
            case QUANDO -> "Quando";
            case ENTAO -> "Então";
            case E -> "E";
            case MAS -> "Mas";
        };
    }
}