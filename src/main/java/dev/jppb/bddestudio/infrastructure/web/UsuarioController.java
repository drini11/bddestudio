package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.UsuarioService;
import dev.jppb.bddestudio.infrastructure.web.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar().stream().map(UsuarioResponse::from).toList();
    }
}
