package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.domain.model.OrigemCenario;

import java.util.List;

public record DadosCenario(String titulo, OrigemCenario origem, List<DadosPasso> passos) {
}
