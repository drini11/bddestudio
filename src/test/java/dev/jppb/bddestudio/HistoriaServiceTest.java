package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.AcessoNegadoException;
import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.NaoAutenticadoException;
import dev.jppb.bddestudio.application.service.DadosHistoria;
import dev.jppb.bddestudio.application.service.HistoriaService;
import dev.jppb.bddestudio.application.service.UsuarioService;
import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Prioridade;
import dev.jppb.bddestudio.domain.model.Projeto;
import dev.jppb.bddestudio.domain.model.Usuario;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.ProjetoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoriaServiceTest {

    @Mock
    HistoriaUsuarioRepository historias;
    @Mock
    ProjetoRepository projetos;
    @Mock
    UsuarioService usuarios;
    @InjectMocks
    HistoriaService service;

    private final DadosHistoria dados = new DadosHistoria(
            "Reservar livro", "estudante", "reservar um livro", "garantir acesso", Prioridade.ALTA);

    private Usuario usuario(Long id) {
        Usuario u = new Usuario("Usuario " + id, "u" + id + "@exemplo.com");
        u.setId(id);
        return u;
    }

    private Projeto projeto() {
        Projeto p = new Projeto("Biblioteca", null, null);
        p.setId(1L);
        return p;
    }

    private HistoriaUsuario historiaDe(Usuario autor) {
        HistoriaUsuario h = new HistoriaUsuario(projeto(), autor, "Titulo antigo",
                "papel", "objetivo", "beneficio", Prioridade.MEDIA);
        h.setId(10L);
        return h;
    }

    @Test
    void criar_valida_salvaComAutorDoCabecalho() {
        Usuario ana = usuario(1L);
        when(usuarios.buscarAtual(1L)).thenReturn(ana);
        when(projetos.findById(1L)).thenReturn(Optional.of(projeto()));
        when(historias.existsByProjetoIdAndTitulo(1L, "Reservar livro")).thenReturn(false);
        when(historias.save(any(HistoriaUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

        HistoriaUsuario salva = service.criar(1L, 1L, dados);

        assertThat(salva.getAutor()).isSameAs(ana);
        assertThat(salva.getTitulo()).isEqualTo("Reservar livro");
    }

    @Test
    void criar_comTituloRepetidoNoProjeto_lancaConflitoENaoSalva() {
        when(usuarios.buscarAtual(1L)).thenReturn(usuario(1L));
        when(projetos.findById(1L)).thenReturn(Optional.of(projeto()));
        when(historias.existsByProjetoIdAndTitulo(1L, "Reservar livro")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(1L, 1L, dados))
                .isInstanceOf(ConflitoException.class);
        verify(historias, never()).save(any());
    }

    @Test
    void criar_comUsuarioInexistente_lancaNaoAutenticado() {
        when(usuarios.buscarAtual(99L)).thenThrow(new NaoAutenticadoException(99L));

        assertThatThrownBy(() -> service.criar(1L, 99L, dados))
                .isInstanceOf(NaoAutenticadoException.class);
        verify(historias, never()).save(any());
    }

    @Test
    void atualizar_porQuemNaoEAutor_lancaAcessoNegadoESemAlterar() {
        HistoriaUsuario historia = historiaDe(usuario(1L));
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(historias.findDetalhadoById(10L)).thenReturn(Optional.of(historia));

        assertThatThrownBy(() -> service.atualizar(10L, 2L, dados))
                .isInstanceOf(AcessoNegadoException.class);
        assertThat(historia.getTitulo()).isEqualTo("Titulo antigo");
    }

    @Test
    void atualizar_porAutor_alteraOsCampos() {
        HistoriaUsuario historia = historiaDe(usuario(1L));
        when(usuarios.buscarAtual(1L)).thenReturn(usuario(1L));
        when(historias.findDetalhadoById(10L)).thenReturn(Optional.of(historia));
        when(historias.existsByProjetoIdAndTituloAndIdNot(1L, "Reservar livro", 10L)).thenReturn(false);

        service.atualizar(10L, 1L, dados);

        assertThat(historia.getTitulo()).isEqualTo("Reservar livro");
        assertThat(historia.getPrioridade()).isEqualTo(Prioridade.ALTA);
    }

    @Test
    void excluir_porQuemNaoEAutor_lancaAcessoNegadoENaoExclui() {
        when(usuarios.buscarAtual(2L)).thenReturn(usuario(2L));
        when(historias.findDetalhadoById(10L)).thenReturn(Optional.of(historiaDe(usuario(1L))));

        assertThatThrownBy(() -> service.excluir(10L, 2L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(historias, never()).delete(any());
    }

    @Test
    void excluir_porAutor_removeHistoria() {
        HistoriaUsuario historia = historiaDe(usuario(1L));
        when(usuarios.buscarAtual(1L)).thenReturn(usuario(1L));
        when(historias.findDetalhadoById(10L)).thenReturn(Optional.of(historia));

        service.excluir(10L, 1L);

        verify(historias).delete(historia);
    }
}