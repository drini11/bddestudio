package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.Projeto;

import java.time.LocalDateTime;

public record ProjetoResponse(Long id, String nome, String descricao, String contextoDominio, LocalDateTime criadoEm) {
    public static ProjetoResponse from(Projeto projeto){
        return new ProjetoResponse(projeto.getId(), projeto.getNome(), projeto.getDescricao(), projeto.getContextoDominio(), projeto.getCriadoEm());
    }

}
