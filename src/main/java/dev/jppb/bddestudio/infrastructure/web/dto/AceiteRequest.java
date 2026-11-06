package dev.jppb.bddestudio.infrastructure.web.dto;

import jakarta.validation.constraints.Size;

public record AceiteRequest(
        @Size(max = 2000, message = "A justificativa deve ter no máximo 2000 caracteres")
        String justificativa) {
}
