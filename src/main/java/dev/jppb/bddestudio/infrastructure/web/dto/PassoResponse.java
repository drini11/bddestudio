package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.Passo;
import dev.jppb.bddestudio.domain.model.TipoPasso;

public record PassoResponse(Long id, TipoPasso tipo, String texto, int posicao) {
    public static PassoResponse from(Passo p) {
        return new PassoResponse(p.getId(), p.getTipo(), p.getTexto(), p.getPosicao());
    }
}
