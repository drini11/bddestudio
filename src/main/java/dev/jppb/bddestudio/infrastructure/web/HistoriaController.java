package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.HistoriaService;
import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Prioridade;
import dev.jppb.bddestudio.infrastructure.web.dto.HistoriaRequest;
import dev.jppb.bddestudio.infrastructure.web.dto.HistoriaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoriaController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";
    private final HistoriaService service;

    @PostMapping("/projetos/{projetoId}/historias")
    public ResponseEntity<HistoriaResponse> criar(@PathVariable Long projetoId,
                                                  @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                                  @Valid @RequestBody HistoriaRequest req) {
        HistoriaUsuario criada = service.criar(projetoId, usuarioId, req.toDados());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/historias/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(HistoriaResponse.from(criada));
    }

    @GetMapping("/projetos/{projetoId}/historias")
    public List<HistoriaResponse> listar(@PathVariable Long projetoId,
                                         @RequestParam(required = false) Prioridade prioridade) {
        return service.listar(projetoId, prioridade).stream().map(HistoriaResponse::from).toList();
    }

    @GetMapping("/historias/{id}")
    public HistoriaResponse buscar(@PathVariable Long id) {
        return HistoriaResponse.from(service.buscar(id));
    }

    @PutMapping("/historias/{id}")
    public HistoriaResponse atualizar(@PathVariable Long id,
                                      @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                      @Valid @RequestBody HistoriaRequest req) {
        return HistoriaResponse.from(service.atualizar(id, usuarioId, req.toDados()));
    }

    @DeleteMapping("/historias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @RequestHeader(HEADER_USUARIO) Long usuarioId) {
        service.excluir(id, usuarioId);
    }
}