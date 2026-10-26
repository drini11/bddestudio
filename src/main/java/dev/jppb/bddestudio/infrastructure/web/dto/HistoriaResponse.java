package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Prioridade;

import java.time.LocalDateTime;

public record HistoriaResponse(
        Long id, Long projetoId, String titulo, String papel, String objetivo,
        String beneficio, Prioridade prioridade, Long autorId, String autorNome,
        LocalDateTime criadaEm, LocalDateTime atualizadaEm) {

    public static HistoriaResponse from(HistoriaUsuario h) {
        return new HistoriaResponse(h.getId(), h.getProjeto().getId(), h.getTitulo(),
                h.getPapel(), h.getObjetivo(), h.getBeneficio(), h.getPrioridade(),
                h.getAutor().getId(), h.getAutor().getNome(),
                h.getCriadaEm(), h.getAtualizadaEm());
    }
}
