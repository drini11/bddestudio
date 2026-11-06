package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.AcessoNegadoException;
import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RegraNegocioException;
import dev.jppb.bddestudio.application.service.*;
import dev.jppb.bddestudio.domain.model.*;
import dev.jppb.bddestudio.domain.repository.CenarioRepository;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static dev.jppb.bddestudio.domain.model.TipoPasso.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenarioServiceTest {

    @Mock
    CenarioRepository cenarios;
    @Mock
    HistoriaUsuarioRepository historias;
    @Mock
    HistoriaService historiaService;
    @Mock
    UsuarioService usuarios;
    @InjectMocks
    CenarioService service;

    private Usuario usuario(Long id) {
        Usuario u = new Usuario("Usuario " + id, "u" + id + "@exemplo.com");
        u.setId(id);
        return u;
    }

    private HistoriaUsuario historia(Usuario autor) {
        Projeto p = new Projeto("Biblioteca", null, null);
        p.setId(1L);
        HistoriaUsuario h = new HistoriaUsuario(p, autor, "Reservar livro",
                "papel", "objetivo", "beneficio", Prioridade.ALTA);
        h.setId(10L);
        return h;
    }

    private Cenario cenarioExistente(HistoriaUsuario historia) {
        Cenario c = new Cenario(historia, "Antigo", 1, OrigemCenario.HUMANO);
        c.setId(5L);
        c.adicionarPasso(new Passo(DADO, "antigo dado", 1));
        c.adicionarPasso(new Passo(QUANDO, "antigo quando", 2));
        c.adicionarPasso(new Passo(ENTAO, "antigo entao", 3));
        return c;
    }

    private DadosPasso passo(TipoPasso tipo, String texto) {
        return new DadosPasso(tipo, texto);
    }

    private DadosCenario dados(String titulo, DadosPasso... passos) {
        return new DadosCenario(titulo, null, List.of(passos));
    }

    @Test
    void criar_valido_numeraPassosNaOrdemInformadaEPosicionaNoFim() {
        when(historiaService.buscarComoAutor(10L, 1L)).thenReturn(historia(usuario(1L)));
        when(cenarios.existsByHistoriaIdAndTitulo(10L, "Reserva ok")).thenReturn(false);
        when(cenarios.maiorPosicao(10L)).thenReturn(2);
        when(cenarios.save(any(Cenario.class))).thenAnswer(inv -> inv.getArgument(0));

        Cenario salvo = service.criar(10L, 1L, dados("Reserva ok",
                passo(DADO, "um"), passo(QUANDO, "dois"), passo(ENTAO, "tres"), passo(E, "quatro")));

        assertThat(salvo.getPosicao()).isEqualTo(3);
        assertThat(salvo.getOrigem()).isEqualTo(OrigemCenario.HUMANO);
        assertThat(salvo.getPassos()).extracting(Passo::getTexto)
                .containsExactly("um", "dois", "tres", "quatro");
        assertThat(salvo.getPassos()).extracting(Passo::getPosicao)
                .containsExactly(1, 2, 3, 4);
    }

    @Test
    void criar_semPassoEntao_lancaRegraNegocioENaoSalva() {
        when(historiaService.buscarComoAutor(10L, 1L)).thenReturn(historia(usuario(1L)));

        assertThatThrownBy(() -> service.criar(10L, 1L,
                dados("Sem entao", passo(DADO, "um"), passo(QUANDO, "dois"))))
                .isInstanceOf(RegraNegocioException.class);
        verify(cenarios, never()).save(any());
    }

    @Test
    void criar_comTituloRepetidoNaHistoria_lancaConflito() {
        when(historiaService.buscarComoAutor(10L, 1L)).thenReturn(historia(usuario(1L)));
        when(cenarios.existsByHistoriaIdAndTitulo(10L, "Repetido")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(10L, 1L,
                dados("Repetido", passo(QUANDO, "a"), passo(ENTAO, "b"))))
                .isInstanceOf(ConflitoException.class);
        verify(cenarios, never()).save(any());
    }

    @Test
    void criar_porQuemNaoEAutor_lancaAcessoNegado() {
        when(historiaService.buscarComoAutor(10L, 2L)).thenThrow(new AcessoNegadoException("negado"));

        assertThatThrownBy(() -> service.criar(10L, 2L,
                dados("X", passo(QUANDO, "a"), passo(ENTAO, "b"))))
                .isInstanceOf(AcessoNegadoException.class);
        verify(cenarios, never()).save(any());
    }

    @Test
    void atualizar_porAutor_substituiPassosNaNovaOrdem() {
        Usuario ana = usuario(1L);
        Cenario existente = cenarioExistente(historia(ana));
        when(usuarios.buscarAtual(1L)).thenReturn(ana);
        when(cenarios.findDetalhadoById(5L)).thenReturn(Optional.of(existente));
        when(cenarios.existsByHistoriaIdAndTituloAndIdNot(10L, "Novo titulo", 5L)).thenReturn(false);

        service.atualizar(5L, 1L, dados("Novo titulo",
                passo(QUANDO, "a"), passo(ENTAO, "b"), passo(E, "c")));

        assertThat(existente.getTitulo()).isEqualTo("Novo titulo");
        assertThat(existente.getPassos()).extracting(Passo::getTexto).containsExactly("a", "b", "c");
        assertThat(existente.getPassos()).extracting(Passo::getPosicao).containsExactly(1, 2, 3);
    }

    @Test
    void atualizar_porQuemNaoEAutor_lancaAcessoNegadoESemAlterar() {
        Cenario existente = cenarioExistente(historia(usuario(1L)));
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(cenarios.findDetalhadoById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(5L, 2L,
                dados("Invasor", passo(QUANDO, "a"), passo(ENTAO, "b"))))
                .isInstanceOf(AcessoNegadoException.class);
        assertThat(existente.getTitulo()).isEqualTo("Antigo");
        assertThat(existente.getPassos()).hasSize(3);
    }

    @Test
    void excluir_porQuemNaoEAutor_lancaAcessoNegadoENaoExclui() {
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(cenarios.findDetalhadoById(5L))
                .thenReturn(Optional.of(cenarioExistente(historia(usuario(1L)))));

        assertThatThrownBy(() -> service.excluir(5L, 2L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(cenarios, never()).delete(any());
    }

    @Test
    void excluir_porAutor_removeCenario() {
        Usuario ana = usuario(1L);
        Cenario existente = cenarioExistente(historia(ana));
        when(usuarios.buscarAtual(1L)).thenReturn(ana);
        when(cenarios.findDetalhadoById(5L)).thenReturn(Optional.of(existente));

        service.excluir(5L, 1L);

        verify(cenarios).delete(existente);
    }
}
