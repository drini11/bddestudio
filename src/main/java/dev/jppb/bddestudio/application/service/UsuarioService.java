package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.NaoAutenticadoException;
import dev.jppb.bddestudio.domain.model.Usuario;
import dev.jppb.bddestudio.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public List<Usuario> listar() {
        return usuarios.findAll();
    }

    public Usuario buscarAtual(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .orElseThrow(() -> new NaoAutenticadoException(usuarioId));
    }
}
