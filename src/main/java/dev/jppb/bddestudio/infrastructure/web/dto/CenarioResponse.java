package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.Cenario;
import dev.jppb.bddestudio.domain.model.OrigemCenario;

import java.util.List;

public record CenarioResponse(Long id, Long historiaId, String titulo, int posicao,
                              OrigemCenario origem, List<PassoResponse> passos) {
    public static CenarioResponse from(Cenario c) {
        return new CenarioResponse(c.getId(), c.getHistoria().getId(), c.getTitulo(),
                c.getPosicao(), c.getOrigem(),
                c.getPassos().stream().map(PassoResponse::from).toList());
    }
}
