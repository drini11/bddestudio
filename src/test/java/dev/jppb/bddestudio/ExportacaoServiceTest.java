package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.application.service.ArquivoFeature;
import dev.jppb.bddestudio.application.service.ExportacaoService;
import dev.jppb.bddestudio.domain.model.*;
import dev.jppb.bddestudio.domain.repository.CenarioRepository;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.SugestaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportacaoServiceTest {

    @Mock
    HistoriaUsuarioRepository historias;
    @Mock
    CenarioRepository cenarios;
    @Mock
    SugestaoRepository sugestoes;
    @InjectMocks
    ExportacaoService service;

    private HistoriaUsuario historia() {
        Projeto p = new Projeto("Biblioteca", null, null);
        p.setId(1L);
        Usuario autor = new Usuario("Ana", "ana@exemplo.com");
        autor.setId(1L);
        HistoriaUsuario h = new HistoriaUsuario(p, autor, "Reservar livro",
                "estudante", "reservar um livro", "garantir o acesso", Prioridade.ALTA);
        h.setId(10L);
        return h;
    }

    private Cenario cenario(HistoriaUsuario h) {
        Cenario c = new Cenario(h, "Reserva ok", 1, OrigemCenario.HUMANO);
        c.adicionarPasso(new Passo(TipoPasso.QUANDO, "o estudante reserva", 1));
        c.adicionarPasso(new Passo(TipoPasso.ENTAO, "a reserva é confirmada", 2));
        return c;
    }

    @Test
    void exportar_valida_devolveGherkinComNomeDoArquivo() {
        HistoriaUsuario h = historia();
        when(historias.findById(10L)).thenReturn(Optional.of(h));
        when(cenarios.findByHistoriaIdOrderByPosicaoAsc(10L)).thenReturn(List.of(cenario(h)));
        when(sugestoes.existsByHistoriaIdAndDecisao(10L, DecisaoSugestao.PENDENTE)).thenReturn(false);

        ArquivoFeature arquivo = service.exportarFeature(10L);

        assertThat(arquivo.nome()).isEqualTo("reservar-livro.feature");
        assertThat(arquivo.conteudo()).startsWith("# language: pt\n")
                .contains("Funcionalidade: Reservar livro", "Cenário: Reserva ok",
                        "Então a reserva é confirmada");
    }

    @Test
    void exportar_comSugestaoPendente_lancaConflito() {
        HistoriaUsuario h = historia();
        when(historias.findById(10L)).thenReturn(Optional.of(h));
        when(cenarios.findByHistoriaIdOrderByPosicaoAsc(10L)).thenReturn(List.of(cenario(h)));
        when(sugestoes.existsByHistoriaIdAndDecisao(10L, DecisaoSugestao.PENDENTE)).thenReturn(true);

        assertThatThrownBy(() -> service.exportarFeature(10L))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("pendentes");
    }

    @Test
    void exportar_semCenarios_lancaConflito() {
        when(historias.findById(10L)).thenReturn(Optional.of(historia()));
        when(cenarios.findByHistoriaIdOrderByPosicaoAsc(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.exportarFeature(10L))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("cenários");
    }

    @Test
    void exportar_historiaInexistente_lancaNaoEncontrado() {
        when(historias.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exportarFeature(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
