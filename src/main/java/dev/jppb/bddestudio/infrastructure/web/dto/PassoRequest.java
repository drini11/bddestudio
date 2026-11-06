package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.TipoPasso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PassoRequest(
        @NotNull(message = "O tipo do passo é obrigatório")
        TipoPasso tipo,

        @NotBlank(message = "O texto do passo é obrigatório")
        @Size(max = 255, message = "O texto do passo deve ter no máximo 255 caracteres")
        String texto) {
}