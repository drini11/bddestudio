package dev.jppb.bddestudio.infrastructure.config;

import dev.jppb.bddestudio.domain.model.*;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.ProjetoRepository;
import dev.jppb.bddestudio.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final ProjetoRepository projetos;
    private final HistoriaUsuarioRepository historias;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }

        Usuario ana = usuarios.save(new Usuario("Ana Souza", "ana@exemplo.com"));
        usuarios.save(new Usuario("Bruno Lima", "bruno@exemplo.com"));

        Projeto biblioteca = projetos.save(new Projeto(
                "Biblioteca Universitária",
                "Sistema de empréstimo e reserva de livros",
                "Estudantes podem reservar até 3 livros por vez. Cada empréstimo dura 14 dias."));

        historias.save(historiaReservar(biblioteca, ana));
        historias.save(historiaRenovar(biblioteca, ana));
    }

    private HistoriaUsuario historiaReservar(Projeto projeto, Usuario autor) {
        HistoriaUsuario h = new HistoriaUsuario(projeto, autor, "Reservar livro",
                "estudante", "reservar um livro indisponível",
                "garantir o acesso quando ele for devolvido", Prioridade.ALTA);

        Cenario c1 = new Cenario("Reserva de livro disponível", 1, OrigemCenario.HUMANO);
        c1.adicionarPasso(new Passo(TipoPasso.DADO, "que o livro \"Clean Code\" está disponível", 1));
        c1.adicionarPasso(new Passo(TipoPasso.QUANDO, "o estudante solicita a reserva", 2));
        c1.adicionarPasso(new Passo(TipoPasso.ENTAO, "a reserva é confirmada", 3));
        h.adicionarCenario(c1);

        Cenario c2 = new Cenario("Limite de reservas atingido", 2, OrigemCenario.HUMANO);
        c2.adicionarPasso(new Passo(TipoPasso.DADO, "que o estudante já possui 3 reservas", 1));
        c2.adicionarPasso(new Passo(TipoPasso.QUANDO, "ele tenta reservar outro livro", 2));
        c2.adicionarPasso(new Passo(TipoPasso.ENTAO, "o sistema recusa a reserva", 3));
        c2.adicionarPasso(new Passo(TipoPasso.E, "informa o limite de 3 reservas", 4));
        h.adicionarCenario(c2);

        return h;
    }

    private HistoriaUsuario historiaRenovar(Projeto projeto, Usuario autor) {
        HistoriaUsuario h = new HistoriaUsuario(projeto, autor, "Renovar empréstimo",
                "estudante", "renovar um empréstimo ativo",
                "continuar com o livro sem ir à biblioteca", Prioridade.MEDIA);

        Cenario c = new Cenario("Renovação sem reservas pendentes", 1, OrigemCenario.HUMANO);
        c.adicionarPasso(new Passo(TipoPasso.DADO, "que o empréstimo está ativo e sem reservas de outros", 1));
        c.adicionarPasso(new Passo(TipoPasso.QUANDO, "o estudante solicita a renovação", 2));
        c.adicionarPasso(new Passo(TipoPasso.ENTAO, "o prazo é estendido em 14 dias", 3));
        h.adicionarCenario(c);

        return h;
    }
}