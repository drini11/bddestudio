package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.SugestaoService;
import dev.jppb.bddestudio.domain.model.DecisaoSugestao;
import dev.jppb.bddestudio.domain.model.Sugestao;
import dev.jppb.bddestudio.infrastructure.web.dto.AceiteRequest;
import dev.jppb.bddestudio.infrastructure.web.dto.RejeicaoRequest;
import dev.jppb.bddestudio.infrastructure.web.dto.SugestaoRequest;
import dev.jppb.bddestudio.infrastructure.web.dto.SugestaoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SugestaoController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";
    private final SugestaoService service;

    @PostMapping("/historias/{historiaId}/sugestoes")
    public ResponseEntity<SugestaoResponse> registrar(@PathVariable Long historiaId,
                                                      @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                                      @Valid @RequestBody SugestaoRequest req) {
        Sugestao criada = service.registrar(historiaId, usuarioId, req.toDados());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/sugestoes/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(SugestaoResponse.from(criada));
    }

    @GetMapping("/historias/{historiaId}/sugestoes")
    public List<SugestaoResponse> listar(@PathVariable Long historiaId,
                                         @RequestParam(required = false) DecisaoSugestao decisao) {
        return service.listar(historiaId, decisao).stream().map(SugestaoResponse::from).toList();
    }

    @GetMapping("/sugestoes/{id}")
    public SugestaoResponse buscar(@PathVariable Long id) {
        return SugestaoResponse.from(service.buscar(id));
    }

    @PostMapping("/sugestoes/{id}/aceitar")
    public SugestaoResponse aceitar(@PathVariable Long id,
                                    @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                    @Valid @RequestBody(required = false) AceiteRequest req) {
        return SugestaoResponse.from(
                service.aceitar(id, usuarioId, req == null ? null : req.justificativa()));
    }

    @PostMapping("/sugestoes/{id}/rejeitar")
    public SugestaoResponse rejeitar(@PathVariable Long id,
                                     @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                     @Valid @RequestBody RejeicaoRequest req) {
        return SugestaoResponse.from(
                service.rejeitar(id, usuarioId, req.motivo(), req.justificativa()));
    }
}
