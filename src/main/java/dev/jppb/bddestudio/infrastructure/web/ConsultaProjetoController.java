package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.ConsultaProjetoService;
import dev.jppb.bddestudio.domain.model.Prioridade;
import dev.jppb.bddestudio.infrastructure.web.dto.HistoriaConsultaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos/{projetoId}")
@RequiredArgsConstructor
public class ConsultaProjetoController {

    private final ConsultaProjetoService service;

    @GetMapping("/consulta")
    public List<HistoriaConsultaResponse> consultar(@PathVariable Long projetoId,
                                                    @RequestParam(required = false) Prioridade prioridade) {
        return service.consultar(projetoId, prioridade).stream()
                .map(HistoriaConsultaResponse::from).toList();
    }

    @GetMapping("/resumo")
    public ConsultaProjetoService.ResumoProjeto resumir(@PathVariable Long projetoId) {
        return service.resumir(projetoId);
    }
}
