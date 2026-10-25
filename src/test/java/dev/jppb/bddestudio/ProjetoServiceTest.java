package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.application.service.ProjetoService;
import dev.jppb.bddestudio.domain.model.Projeto;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.ProjetoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    ProjetoRepository projetos;
    @Mock
    HistoriaUsuarioRepository historias;
    @InjectMocks
    ProjetoService service;

    @Test
    void criar_comNomeNovo_salvaProjeto() {
        when(projetos.existsByNome("Estoque")).thenReturn(false);
        when(projetos.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        Projeto salvo = service.criar("Estoque", "desc", "ctx");

        assertThat(salvo.getNome()).isEqualTo("Estoque");
        verify(projetos).save(any(Projeto.class));
    }

    @Test
    void criar_comNomeRepetido_lancaConflitoENaoSalva() {
        when(projetos.existsByNome("Estoque")).thenReturn(true);

        assertThatThrownBy(() -> service.criar("Estoque", null, null))
                .isInstanceOf(ConflitoException.class);
        verify(projetos, never()).save(any());
    }

    @Test
    void buscar_inexistente_lancaNaoEncontrado() {
        when(projetos.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizar_comNomeDeOutroProjeto_lancaConflito() {
        when(projetos.findById(1L)).thenReturn(Optional.of(new Projeto("A", null, null)));
        when(projetos.existsByNomeAndIdNot("B", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, "B", null, null))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void excluir_comHistorias_lancaConflitoENaoExclui() {
        when(projetos.findById(1L)).thenReturn(Optional.of(new Projeto("A", null, null)));
        when(historias.existsByProjetoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ConflitoException.class);
        verify(projetos, never()).delete(any());
    }

    @Test
    void excluir_semHistorias_removeProjeto() {
        Projeto projeto = new Projeto("A", null, null);
        when(projetos.findById(1L)).thenReturn(Optional.of(projeto));
        when(historias.existsByProjetoId(1L)).thenReturn(false);

        service.excluir(1L);

        verify(projetos).delete(projeto);
    }
}