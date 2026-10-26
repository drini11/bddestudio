package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.domain.model.Prioridade;

public record DadosHistoria(String titulo, String papel, String objetivo,
                            String beneficio, Prioridade prioridade) {
}
