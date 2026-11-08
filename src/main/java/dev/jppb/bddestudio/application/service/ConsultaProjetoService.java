package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.domain.model.*;
import dev.jppb.bddestudio.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultaProjetoService {

    public record HistoriaComContagens(HistoriaUsuario historia,
                                       long quantidadeCenarios, long sugestoesPendentes) {
    }

    public record MotivoFrequente(MotivoRejeicao motivo, long quantidade) {
    }

    public record ResumoProjeto(Long projetoId, String projetoNome, long totalHistorias,
                                Map<OrigemCenario, Long> cenariosPorOrigem,
                                Map<DecisaoSugestao, Long> sugestoesPorDecisao,
                                List<MotivoFrequente> motivosDeRejeicao) {
    }

    private final ProjetoRepository projetos;
    private final HistoriaUsuarioRepository historias;
    private final CenarioRepository cenarios;
    private final SugestaoRepository sugestoes;

    public List<HistoriaComContagens> consultar(Long projetoId, Prioridade prioridade) {
        if (!projetos.existsById(projetoId)) {
            throw new RecursoNaoEncontradoException("Projeto", projetoId);
        }
        List<HistoriaUsuario> lista = prioridade == null
                ? historias.findByProjetoIdOrderByIdAsc(projetoId)
                : historias.findByProjetoIdAndPrioridadeOrderByIdAsc(projetoId, prioridade);

        Map<Long, Long> qtdCenarios = porHistoria(cenarios.contarPorHistoria(projetoId));
        Map<Long, Long> pendentes = porHistoria(
                sugestoes.contarPorHistoria(projetoId, DecisaoSugestao.PENDENTE));

        return lista.stream()
                .map(h -> new HistoriaComContagens(h,
                        qtdCenarios.getOrDefault(h.getId(), 0L),
                        pendentes.getOrDefault(h.getId(), 0L)))
                .toList();
    }

    public ResumoProjeto resumir(Long projetoId) {
        Projeto projeto = projetos.findById(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto", projetoId));

        Map<OrigemCenario, Long> porOrigem = new EnumMap<>(OrigemCenario.class);
        for (OrigemCenario origem : OrigemCenario.values()) {
            porOrigem.put(origem, 0L);
        }
        cenarios.contarPorOrigem(projetoId).forEach(c -> porOrigem.put(c.getOrigem(), c.getTotal()));

        Map<DecisaoSugestao, Long> porDecisao = new EnumMap<>(DecisaoSugestao.class);
        for (DecisaoSugestao decisao : DecisaoSugestao.values()) {
            porDecisao.put(decisao, 0L);
        }
        sugestoes.contarPorDecisao(projetoId).forEach(c -> porDecisao.put(c.getDecisao(), c.getTotal()));

        List<MotivoFrequente> motivos = sugestoes.contarPorMotivo(projetoId).stream()
                .map(m -> new MotivoFrequente(m.getMotivo(), m.getTotal()))
                .sorted(Comparator.comparingLong(MotivoFrequente::quantidade).reversed()
                        .thenComparing(MotivoFrequente::motivo))
                .toList();

        return new ResumoProjeto(projeto.getId(), projeto.getNome(),
                historias.countByProjetoId(projetoId), porOrigem, porDecisao, motivos);
    }

    private Map<Long, Long> porHistoria(List<ContagemPorHistoria> contagens) {
        return contagens.stream().collect(
                Collectors.toMap(ContagemPorHistoria::getHistoriaId, ContagemPorHistoria::getTotal));
    }
}
