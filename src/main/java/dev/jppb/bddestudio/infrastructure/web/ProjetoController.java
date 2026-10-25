package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.ProjetoService;
import dev.jppb.bddestudio.domain.model.Projeto;
import dev.jppb.bddestudio.infrastructure.web.dto.ProjetoRequest;
import dev.jppb.bddestudio.infrastructure.web.dto.ProjetoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService pservice;

    @PostMapping
    public ResponseEntity<ProjetoResponse> criar(@Valid @RequestBody ProjetoRequest req){
        Projeto criado = pservice.criar(req.nome(), req.descricao(), req.contextoDominio());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(ProjetoResponse.from(criado));
    }

    @GetMapping
    public List<ProjetoResponse> listar(){
        return pservice.listar().stream().map(ProjetoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjetoResponse buscar(@PathVariable Long id){
        return ProjetoResponse.from(pservice.buscar(id));
    }

    @PutMapping("/{id}")
    public ProjetoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProjetoRequest req){
        return ProjetoResponse.from(pservice.atualizar(id, req.nome(), req.descricao(), req.contextoDominio()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id){
        pservice.excluir(id);
    }
}
