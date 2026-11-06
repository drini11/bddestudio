package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.application.exception.RegraNegocioException;
import dev.jppb.bddestudio.domain.model.*;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.SugestaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SugestaoService {

    private final SugestaoRepository sugestoes;
    private final HistoriaUsuarioRepository historias;
    private final UsuarioService usuarios;

    @Transactional
    public Sugestao registrar(Long historiaId, Long usuarioId, DadosSugestao d) {
        Usuario usuario = usuarios.buscarAtual(usuarioId);
        HistoriaUsuario historia = historias.findById(historiaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("História", historiaId));
        return sugestoes.save(new Sugestao(historia, usuario,
                d.ferramenta().strip(), d.prompt().strip(), d.conteudo().strip()));
    }

    public List<Sugestao> listar(Long historiaId, DecisaoSugestao decisao) {
        if (!historias.existsById(historiaId)) {
            throw new RecursoNaoEncontradoException("História", historiaId);
        }
        return decisao == null
                ? sugestoes.findByHistoriaIdOrderByIdAsc(historiaId)
                : sugestoes.findByHistoriaIdAndDecisaoOrderByIdAsc(historiaId, decisao);
    }

    public Sugestao buscar(Long id) {
        return sugestoes.findDetalhadoById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão", id));
    }

    @Transactional
    public Sugestao aceitar(Long id, Long usuarioId, String justificativa) {
        Usuario usuario = usuarios.buscarAtual(usuarioId);
        Sugestao sugestao = buscar(id);
        exigirPendente(sugestao);
        sugestao.aceitar(usuario, limpar(justificativa));
        return sugestao;
    }

    @Transactional
    public Sugestao rejeitar(Long id, Long usuarioId, MotivoRejeicao motivo, String justificativa) {
        Usuario usuario = usuarios.buscarAtual(usuarioId);
        Sugestao sugestao = buscar(id);
        String texto = limpar(justificativa);
        if (motivo == null) {
            throw new RegraNegocioException("A rejeição exige um motivo: INCORRETA, GENERICA, "
                    + "DUPLICADA, FORA_DO_ESCOPO ou OUTRO");
        }
        if (motivo == MotivoRejeicao.OUTRO && texto == null) {
            throw new RegraNegocioException("O motivo OUTRO exige uma justificativa");
        }
        exigirPendente(sugestao);
        sugestao.rejeitar(usuario, motivo, texto);
        return sugestao;
    }

    private void exigirPendente(Sugestao sugestao) {
        if (!sugestao.estaPendente()) {
            throw new ConflitoException("A sugestão já foi decidida (" + sugestao.getDecisao()
                    + ") e não pode ser alterada");
        }
    }

    private String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
