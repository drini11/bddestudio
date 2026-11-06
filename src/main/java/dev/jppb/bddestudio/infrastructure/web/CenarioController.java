package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.CenarioService;
import dev.jppb.bddestudio.domain.model.Cenario;
import dev.jppb.bddestudio.infrastructure.web.dto.CenarioRequest;
import dev.jppb.bddestudio.infrastructure.web.dto.CenarioResponse;
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
public class CenarioController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";
    private final CenarioService service;

    @PostMapping("/historias/{historiaId}/cenarios")
    public ResponseEntity<CenarioResponse> criar(@PathVariable Long historiaId,
                                                 @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                                 @Valid @RequestBody CenarioRequest req) {
        Cenario criado = service.criar(historiaId, usuarioId, req.toDados());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/cenarios/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(CenarioResponse.from(criado));
    }

    @GetMapping("/historias/{historiaId}/cenarios")
    public List<CenarioResponse> listar(@PathVariable Long historiaId) {
        return service.listar(historiaId).stream().map(CenarioResponse::from).toList();
    }

    @GetMapping("/cenarios/{id}")
    public CenarioResponse buscar(@PathVariable Long id) {
        return CenarioResponse.from(service.buscar(id));
    }

    @PutMapping("/cenarios/{id}")
    public CenarioResponse atualizar(@PathVariable Long id,
                                     @RequestHeader(HEADER_USUARIO) Long usuarioId,
                                     @Valid @RequestBody CenarioRequest req) {
        return CenarioResponse.from(service.atualizar(id, usuarioId, req.toDados()));
    }

    @DeleteMapping("/cenarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @RequestHeader(HEADER_USUARIO) Long usuarioId) {
        service.excluir(id, usuarioId);
    }
}
