package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.AcessoNegadoException;
import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Prioridade;
import dev.jppb.bddestudio.domain.model.Projeto;
import dev.jppb.bddestudio.domain.model.Usuario;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.ProjetoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoriaService {

    private final HistoriaUsuarioRepository historias;
    private final ProjetoRepository projetos;
    private final UsuarioService usuarios;

    public HistoriaUsuario criar(Long projetoId, Long usuarioId, DadosHistoria d) {
        Usuario autor = usuarios.buscarAtual(usuarioId);
        Projeto projeto = projetos.findById(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto", projetoId));
        String titulo = d.titulo().strip();
        if (historias.existsByProjetoIdAndTitulo(projetoId, titulo)) {
            throw new ConflitoException("Já existe uma história com o título '" + titulo + "' neste projeto");
        }
        return historias.save(new HistoriaUsuario(projeto, autor, titulo,
                d.papel().strip(), d.objetivo().strip(), d.beneficio().strip(), d.prioridade()));
    }

    @Transactional
    public List<HistoriaUsuario> listar(Long projetoId, Prioridade prioridade) {
        if (!projetos.existsById(projetoId)) {
            throw new RecursoNaoEncontradoException("Projeto", projetoId);
        }
        return prioridade == null
                ? historias.findByProjetoIdOrderByIdAsc(projetoId)
                : historias.findByProjetoIdAndPrioridadeOrderByIdAsc(projetoId, prioridade);
    }

    @Transactional
    public HistoriaUsuario buscar(Long id) {
        return historias.findDetalhadoById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("História", id));
    }

    public HistoriaUsuario atualizar(Long historiaId, Long usuarioId, DadosHistoria d) {
        HistoriaUsuario historia = buscarComoAutor(historiaId, usuarioId);
        String titulo = d.titulo().strip();
        if (historias.existsByProjetoIdAndTituloAndIdNot(historia.getProjeto().getId(), titulo, historiaId)) {
            throw new ConflitoException("Já existe uma história com o título '" + titulo + "' neste projeto");
        }
        historia.atualizar(titulo, d.papel().strip(), d.objetivo().strip(),
                d.beneficio().strip(), d.prioridade());
        return historia;
    }

    public void excluir(Long historiaId, Long usuarioId) {
        historias.delete(buscarComoAutor(historiaId, usuarioId));
    }

    public HistoriaUsuario buscarComoAutor(Long historiaId, Long usuarioId) {
        usuarios.buscarAtual(usuarioId);
        HistoriaUsuario historia = buscar(historiaId);
        if (!historia.foiEscritaPor(usuarioId)) {
            throw new AcessoNegadoException("Somente o autor pode alterar ou excluir esta história");
        }
        return historia;
    }
}
