package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.application.service.DadosCenario;
import dev.jppb.bddestudio.application.service.DadosPasso;
import dev.jppb.bddestudio.domain.model.OrigemCenario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CenarioRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        OrigemCenario origem, // opcional: padrão HUMANO na criação, mantém o atual na edição

        @NotNull(message = "Os passos são obrigatórios")
        List<@NotNull @Valid PassoRequest> passos) {

    public DadosCenario toDados() {
        return new DadosCenario(titulo, origem,
                passos.stream().map(p -> new DadosPasso(p.tipo(), p.texto())).toList());
    }
}