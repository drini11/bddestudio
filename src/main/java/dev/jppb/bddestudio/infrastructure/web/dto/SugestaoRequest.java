package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.application.service.DadosSugestao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SugestaoRequest(
        @NotBlank(message = "A ferramenta é obrigatória")
        @Size(max = 100, message = "A ferramenta deve ter no máximo 100 caracteres")
        String ferramenta,

        @NotBlank(message = "O prompt é obrigatório")
        @Size(max = 20000, message = "O prompt deve ter no máximo 20000 caracteres")
        String prompt,

        @NotBlank(message = "O conteúdo é obrigatório")
        @Size(max = 20000, message = "O conteúdo deve ter no máximo 20000 caracteres")
        String conteudo) {

    public DadosSugestao toDados() {
        return new DadosSugestao(ferramenta, prompt, conteudo);
    }
}
