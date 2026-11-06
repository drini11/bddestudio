package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.NaoAutenticadoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.application.exception.RegraNegocioException;
import dev.jppb.bddestudio.application.service.DadosSugestao;
import dev.jppb.bddestudio.application.service.SugestaoService;
import dev.jppb.bddestudio.application.service.UsuarioService;
import dev.jppb.bddestudio.domain.model.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SugestaoServiceTest {

    @Mock
    SugestaoRepository sugestoes;
    @Mock
    HistoriaUsuarioRepository historias;
    @Mock
    UsuarioService usuarios;
    @InjectMocks
    SugestaoService service;

    private Usuario usuario(Long id) {
        Usuario u = new Usuario("Usuario " + id, "u" + id + "@exemplo.com");
        u.setId(id);
        return u;
    }

    private HistoriaUsuario historia() {
        Projeto p = new Projeto("Biblioteca", null, null);
        p.setId(1L);
        HistoriaUsuario h = new HistoriaUsuario(p, usuario(1L), "Reservar livro",
                "papel", "objetivo", "beneficio", Prioridade.ALTA);
        h.setId(10L);
        return h;
    }

    private Sugestao pendente() {
        return new Sugestao(historia(), usuario(1L), "ChatGPT", "prompt", "conteudo");
    }

    @Test
    void registrar_valida_comecaPendenteComQuemRegistrou() {
        Usuario ana = usuario(1L);
        when(usuarios.buscarAtual(1L)).thenReturn(ana);
        when(historias.findById(10L)).thenReturn(Optional.of(historia()));
        when(sugestoes.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sugestao salva = service.registrar(10L, 1L,
                new DadosSugestao("  ChatGPT ", "meu prompt", "o conteudo"));

        assertThat(salva.getDecisao()).isEqualTo(DecisaoSugestao.PENDENTE);
        assertThat(salva.getFerramenta()).isEqualTo("ChatGPT");
        assertThat(salva.getRegistradaPor()).isSameAs(ana);
        assertThat(salva.getDecididaPor()).isNull();
        assertThat(salva.getDecididaEm()).isNull();
    }

    @Test
    void registrar_comUsuarioInexistente_lancaNaoAutenticadoENaoSalva() {
        when(usuarios.buscarAtual(99L)).thenThrow(new NaoAutenticadoException(99L));

        assertThatThrownBy(() -> service.registrar(10L, 99L, new DadosSugestao("a", "b", "c")))
                .isInstanceOf(NaoAutenticadoException.class);
        verify(sugestoes, never()).save(any());
    }

    @Test
    void registrar_comHistoriaInexistente_lancaNaoEncontradoENaoSalva() {
        when(usuarios.buscarAtual(1L)).thenReturn(usuario(1L));
        when(historias.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(99L, 1L, new DadosSugestao("a", "b", "c")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(sugestoes, never()).save(any());
    }

    @Test
    void aceitar_pendente_marcaComoAceitaComQuemEQuando() {
        Usuario bruno = usuario(2L);
        Sugestao sugestao = pendente();
        when(usuarios.buscarAtual(2L)).thenReturn(bruno);
        when(sugestoes.findDetalhadoById(5L)).thenReturn(Optional.of(sugestao));

        service.aceitar(5L, 2L, "  faz sentido  ");

        assertThat(sugestao.getDecisao()).isEqualTo(DecisaoSugestao.ACEITA);
        assertThat(sugestao.getDecididaPor()).isSameAs(bruno);
        assertThat(sugestao.getDecididaEm()).isNotNull();
        assertThat(sugestao.getJustificativa()).isEqualTo("faz sentido");
        assertThat(sugestao.getMotivoRejeicao()).isNull();
    }

    @Test
    void rejeitar_comMotivo_marcaComoRejeitada() {
        Sugestao sugestao = pendente();
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(sugestoes.findDetalhadoById(5L)).thenReturn(Optional.of(sugestao));

        service.rejeitar(5L, 2L, MotivoRejeicao.GENERICA, "  muito genérica ");

        assertThat(sugestao.getDecisao()).isEqualTo(DecisaoSugestao.REJEITADA);
        assertThat(sugestao.getMotivoRejeicao()).isEqualTo(MotivoRejeicao.GENERICA);
        assertThat(sugestao.getJustificativa()).isEqualTo("muito genérica");
        assertThat(sugestao.getDecididaEm()).isNotNull();
    }

    @Test
    void rejeitar_semMotivo_lancaRegraNegocioEMantemPendente() {
        Sugestao sugestao = pendente();
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(sugestoes.findDetalhadoById(5L)).thenReturn(Optional.of(sugestao));

        assertThatThrownBy(() -> service.rejeitar(5L, 2L, null, "não gostei"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("motivo");
        assertThat(sugestao.getDecisao()).isEqualTo(DecisaoSugestao.PENDENTE);
        assertThat(sugestao.getDecididaPor()).isNull();
    }

    @Test
    void rejeitar_motivoOutroSemJustificativa_lancaRegraNegocio() {
        Sugestao sugestao = pendente();
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(sugestoes.findDetalhadoById(5L)).thenReturn(Optional.of(sugestao));

        assertThatThrownBy(() -> service.rejeitar(5L, 2L, MotivoRejeicao.OUTRO, "   "))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(sugestao.getDecisao()).isEqualTo(DecisaoSugestao.PENDENTE);
    }

    @Test
    void aceitar_jaDecidida_lancaConflitoESemAlterar() {
        Sugestao sugestao = pendente();
        sugestao.aceitar(usuario(1L), null);
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(sugestoes.findDetalhadoById(5L)).thenReturn(Optional.of(sugestao));

        assertThatThrownBy(() -> service.aceitar(5L, 2L, "outra vez"))
                .isInstanceOf(ConflitoException.class);
        assertThat(sugestao.getDecisao()).isEqualTo(DecisaoSugestao.ACEITA);
        assertThat(sugestao.getDecididaPor().getId()).isEqualTo(1L);
    }

    @Test
    void rejeitar_jaDecidida_lancaConflitoESemAlterar() {
        Sugestao sugestao = pendente();
        sugestao.rejeitar(usuario(1L), MotivoRejeicao.GENERICA, null);
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(sugestoes.findDetalhadoById(5L)).thenReturn(Optional.of(sugestao));

        assertThatThrownBy(() -> service.rejeitar(5L, 2L, MotivoRejeicao.INCORRETA, null))
                .isInstanceOf(ConflitoException.class);
        assertThat(sugestao.getMotivoRejeicao()).isEqualTo(MotivoRejeicao.GENERICA);
    }

    @Test
    void listar_semFiltro_usaConsultaDaHistoria() {
        when(historias.existsById(10L)).thenReturn(true);
        when(sugestoes.findByHistoriaIdOrderByIdAsc(10L)).thenReturn(List.of(pendente()));

        assertThat(service.listar(10L, null)).hasSize(1);
        verify(sugestoes, never()).findByHistoriaIdAndDecisaoOrderByIdAsc(any(), any());
    }

    @Test
    void listar_comFiltro_usaConsultaPorDecisao() {
        when(historias.existsById(10L)).thenReturn(true);
        when(sugestoes.findByHistoriaIdAndDecisaoOrderByIdAsc(10L, DecisaoSugestao.PENDENTE))
                .thenReturn(List.of(pendente()));

        assertThat(service.listar(10L, DecisaoSugestao.PENDENTE)).hasSize(1);
    }

    @Test
    void listar_historiaInexistente_lancaNaoEncontrado() {
        when(historias.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.listar(99L, null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
