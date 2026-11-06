package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.MotivoRejeicao;
import jakarta.validation.constraints.Size;

public record RejeicaoRequest(
        MotivoRejeicao motivo,

        @Size(max = 2000, message = "A justificativa deve ter no máximo 2000 caracteres")
        String justificativa) {
}
