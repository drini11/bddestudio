package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.application.service.ConsultaProjetoService;
import dev.jppb.bddestudio.domain.model.*;
import dev.jppb.bddestudio.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaProjetoServiceTest {

    @Mock
    ProjetoRepository projetos;
    @Mock
    HistoriaUsuarioRepository historias;
    @Mock
    CenarioRepository cenarios;
    @Mock
    SugestaoRepository sugestoes;
    @InjectMocks
    ConsultaProjetoService service;

    private HistoriaUsuario historia(Long id, Prioridade prioridade) {
        Projeto p = new Projeto("Biblioteca", null, null);
        p.setId(1L);
        Usuario autor = new Usuario("Ana", "ana@exemplo.com");
        autor.setId(1L);
        HistoriaUsuario h = new HistoriaUsuario(p, autor, "Historia " + id,
                "papel", "objetivo", "beneficio", prioridade);
        h.setId(id);
        return h;
    }

    private ContagemPorHistoria porHistoria(Long historiaId, long total) {
        return new ContagemPorHistoria() {
            public Long getHistoriaId() { return historiaId; }
            public Long getTotal() { return total; }
        };
    }

    private CenarioRepository.ContagemPorOrigem porOrigem(OrigemCenario origem, long total) {
        return new CenarioRepository.ContagemPorOrigem() {
            public OrigemCenario getOrigem() { return origem; }
            public Long getTotal() { return total; }
        };
    }

    private SugestaoRepository.ContagemPorDecisao porDecisao(DecisaoSugestao decisao, long total) {
        return new SugestaoRepository.ContagemPorDecisao() {
            public DecisaoSugestao getDecisao() { return decisao; }
            public Long getTotal() { return total; }
        };
    }

    private SugestaoRepository.ContagemPorMotivo porMotivo(MotivoRejeicao motivo, long total) {
        return new SugestaoRepository.ContagemPorMotivo() {
            public MotivoRejeicao getMotivo() { return motivo; }
            public Long getTotal() { return total; }
        };
    }

    @Test
    void consultar_juntaContagensDeCenariosEPendentesPorHistoria() {
        when(projetos.existsById(1L)).thenReturn(true);
        when(historias.findByProjetoIdOrderByIdAsc(1L))
                .thenReturn(List.of(historia(10L, Prioridade.ALTA), historia(11L, Prioridade.BAIXA)));
        when(cenarios.contarPorHistoria(1L)).thenReturn(List.of(porHistoria(10L, 3)));
        when(sugestoes.contarPorHistoria(1L, DecisaoSugestao.PENDENTE))
                .thenReturn(List.of(porHistoria(10L, 2), porHistoria(11L, 1)));

        List<ConsultaProjetoService.HistoriaComContagens> resultado = service.consultar(1L, null);

        assertThat(resultado).extracting(r -> r.historia().getId()).containsExactly(10L, 11L);
        assertThat(resultado).extracting(ConsultaProjetoService.HistoriaComContagens::quantidadeCenarios)
                .containsExactly(3L, 0L);
        assertThat(resultado).extracting(ConsultaProjetoService.HistoriaComContagens::sugestoesPendentes)
                .containsExactly(2L, 1L);
    }

    @Test
    void consultar_comFiltroDePrioridade_usaConsultaFiltrada() {
        when(projetos.existsById(1L)).thenReturn(true);
        when(historias.findByProjetoIdAndPrioridadeOrderByIdAsc(1L, Prioridade.ALTA))
                .thenReturn(List.of(historia(10L, Prioridade.ALTA)));

        assertThat(service.consultar(1L, Prioridade.ALTA)).hasSize(1);
        verify(historias, never()).findByProjetoIdOrderByIdAsc(any());
    }

    @Test
    void consultar_projetoInexistente_lancaNaoEncontrado() {
        when(projetos.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.consultar(99L, null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(historias, never()).findByProjetoIdOrderByIdAsc(any());
    }

    @Test
    void resumir_preencheZerosEOrdenaMotivosDoMaisFrequente() {
        Projeto projeto = new Projeto("Biblioteca", null, null);
        projeto.setId(1L);
        when(projetos.findById(1L)).thenReturn(Optional.of(projeto));
        when(historias.countByProjetoId(1L)).thenReturn(3L);
        when(cenarios.contarPorOrigem(1L)).thenReturn(List.of(porOrigem(OrigemCenario.HUMANO, 2)));
        when(sugestoes.contarPorDecisao(1L)).thenReturn(List.of(
                porDecisao(DecisaoSugestao.ACEITA, 1), porDecisao(DecisaoSugestao.REJEITADA, 5)));
        when(sugestoes.contarPorMotivo(1L)).thenReturn(List.of(
                porMotivo(MotivoRejeicao.INCORRETA, 1),
                porMotivo(MotivoRejeicao.DUPLICADA, 2),
                porMotivo(MotivoRejeicao.GENERICA, 2)));

        ConsultaProjetoService.ResumoProjeto resumo = service.resumir(1L);

        assertThat(resumo.totalHistorias()).isEqualTo(3L);
        assertThat(resumo.cenariosPorOrigem())
                .containsEntry(OrigemCenario.HUMANO, 2L)
                .containsEntry(OrigemCenario.SUGESTAO_IA, 0L);
        assertThat(resumo.sugestoesPorDecisao())
                .containsEntry(DecisaoSugestao.PENDENTE, 0L)
                .containsEntry(DecisaoSugestao.ACEITA, 1L)
                .containsEntry(DecisaoSugestao.REJEITADA, 5L);
        assertThat(resumo.motivosDeRejeicao())
                .extracting(ConsultaProjetoService.MotivoFrequente::motivo)
                .containsExactly(MotivoRejeicao.GENERICA, MotivoRejeicao.DUPLICADA, MotivoRejeicao.INCORRETA);
        assertThat(resumo.motivosDeRejeicao().get(0).quantidade()).isEqualTo(2L);
    }

    @Test
    void resumir_projetoInexistente_lancaNaoEncontrado() {
        when(projetos.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resumir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
