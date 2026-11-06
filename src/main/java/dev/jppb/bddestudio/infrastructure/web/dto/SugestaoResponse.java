package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.domain.model.DecisaoSugestao;
import dev.jppb.bddestudio.domain.model.MotivoRejeicao;
import dev.jppb.bddestudio.domain.model.Sugestao;
import dev.jppb.bddestudio.domain.model.Usuario;

import java.time.LocalDateTime;

public record SugestaoResponse(
        Long id, Long historiaId, String ferramenta, String prompt, String conteudo,
        DecisaoSugestao decisao, MotivoRejeicao motivoRejeicao, String justificativa,
        LocalDateTime registradaEm, LocalDateTime decididaEm,
        Long registradaPorId, String registradaPorNome,
        Long decididaPorId, String decididaPorNome) {

    public static SugestaoResponse from(Sugestao s) {
        Usuario decidiu = s.getDecididaPor();
        return new SugestaoResponse(s.getId(), s.getHistoria().getId(), s.getFerramenta(),
                s.getPrompt(), s.getConteudo(), s.getDecisao(), s.getMotivoRejeicao(),
                s.getJustificativa(), s.getRegistradaEm(), s.getDecididaEm(),
                s.getRegistradaPor().getId(), s.getRegistradaPor().getNome(),
                decidiu == null ? null : decidiu.getId(),
                decidiu == null ? null : decidiu.getNome());
    }
}
