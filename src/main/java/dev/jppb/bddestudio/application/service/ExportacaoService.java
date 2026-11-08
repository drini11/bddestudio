package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.domain.model.Cenario;
import dev.jppb.bddestudio.domain.model.DecisaoSugestao;
import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.repository.CenarioRepository;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.SugestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportacaoService {

    private final HistoriaUsuarioRepository historias;
    private final CenarioRepository cenarios;
    private final SugestaoRepository sugestoes;

    public ArquivoFeature exportarFeature(Long historiaId) {
        HistoriaUsuario historia = historias.findById(historiaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("História", historiaId));
        List<Cenario> lista = cenarios.findByHistoriaIdOrderByPosicaoAsc(historiaId);
        if (lista.isEmpty()) {
            throw new ConflitoException("A história não tem cenários e não pode ser exportada");
        }
        if (sugestoes.existsByHistoriaIdAndDecisao(historiaId, DecisaoSugestao.PENDENTE)) {
            throw new ConflitoException(
                    "A história tem sugestões pendentes: aceite ou rejeite todas antes de exportar");
        }
        return new ArquivoFeature(GeradorFeature.nomeDoArquivo(historia),
                GeradorFeature.gerar(historia, lista));
    }
}
