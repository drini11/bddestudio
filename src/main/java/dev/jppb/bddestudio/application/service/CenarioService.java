package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.AcessoNegadoException;
import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.domain.model.Cenario;
import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.OrigemCenario;
import dev.jppb.bddestudio.domain.model.Passo;
import dev.jppb.bddestudio.domain.repository.CenarioRepository;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CenarioService {

    private final CenarioRepository cenarios;
    private final HistoriaUsuarioRepository historias;
    private final HistoriaService historiaService;
    private final UsuarioService usuarios;

    @Transactional
    public Cenario criar(Long historiaId, Long usuarioId, DadosCenario d) {
        HistoriaUsuario historia = historiaService.buscarComoAutor(historiaId, usuarioId);
        ValidadorPassos.validar(d.passos());
        String titulo = d.titulo().strip();
        if (cenarios.existsByHistoriaIdAndTitulo(historiaId, titulo)) {
            throw new ConflitoException("Já existe um cenário com o título '" + titulo + "' nesta história");
        }
        OrigemCenario origem = d.origem() == null ? OrigemCenario.HUMANO : d.origem();
        Cenario cenario = new Cenario(historia, titulo, cenarios.maiorPosicao(historiaId) + 1, origem);
        montarPassos(d.passos()).forEach(cenario::adicionarPasso);
        return cenarios.save(cenario);
    }

    public List<Cenario> listar(Long historiaId) {
        if (!historias.existsById(historiaId)) {
            throw new RecursoNaoEncontradoException("História", historiaId);
        }
        return cenarios.findByHistoriaIdOrderByPosicaoAsc(historiaId);
    }

    public Cenario buscar(Long id) {
        return cenarios.findDetalhadoById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cenário", id));
    }

    @Transactional
    public Cenario atualizar(Long cenarioId, Long usuarioId, DadosCenario d) {
        Cenario cenario = buscarComoAutor(cenarioId, usuarioId);
        ValidadorPassos.validar(d.passos());
        String titulo = d.titulo().strip();
        if (cenarios.existsByHistoriaIdAndTituloAndIdNot(cenario.getHistoria().getId(), titulo, cenarioId)) {
            throw new ConflitoException("Já existe um cenário com o título '" + titulo + "' nesta história");
        }
        cenario.atualizar(titulo, d.origem() == null ? cenario.getOrigem() : d.origem());
        cenario.substituirPassos(montarPassos(d.passos()));
        return cenario;
    }
    @Transactional
    public void excluir(Long cenarioId, Long usuarioId) {
        cenarios.delete(buscarComoAutor(cenarioId, usuarioId));
    }
    private Cenario buscarComoAutor(Long cenarioId, Long usuarioId) {
        usuarios.buscarAtual(usuarioId);
        Cenario cenario = buscar(cenarioId);
        if (!cenario.getHistoria().foiEscritaPor(usuarioId)) {
            throw new AcessoNegadoException("Somente o autor da história pode alterar ou excluir seus cenários");
        }
        return cenario;
    }

    private List<Passo> montarPassos(List<DadosPasso> dados) {
        List<Passo> passos = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            passos.add(new Passo(dados.get(i).tipo(), dados.get(i).texto().strip(), i + 1));
        }
        return passos;
    }
}
