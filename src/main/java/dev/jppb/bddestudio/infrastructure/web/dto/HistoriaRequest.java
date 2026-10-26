package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.application.service.DadosHistoria;
import dev.jppb.bddestudio.domain.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistoriaRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "O papel (Como...) é obrigatório")
        @Size(max = 255, message = "O papel deve ter no máximo 255 caracteres")
        String papel,

        @NotBlank(message = "O objetivo (quero...) é obrigatório")
        @Size(max = 255, message = "O objetivo deve ter no máximo 255 caracteres")
        String objetivo,

        @NotBlank(message = "O benefício (para...) é obrigatório")
        @Size(max = 255, message = "O benefício deve ter no máximo 255 caracteres")
        String beneficio,

        @NotNull(message = "A prioridade é obrigatória")
        Prioridade prioridade) {

    public DadosHistoria toDados() {
        return new DadosHistoria(titulo, papel, objetivo, beneficio, prioridade);
    }
}
