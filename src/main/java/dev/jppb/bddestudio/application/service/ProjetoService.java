package dev.jppb.bddestudio.application.service;

import dev.jppb.bddestudio.application.exception.ConflitoException;
import dev.jppb.bddestudio.application.exception.RecursoNaoEncontradoException;
import dev.jppb.bddestudio.domain.model.Projeto;
import dev.jppb.bddestudio.domain.repository.HistoriaUsuarioRepository;
import dev.jppb.bddestudio.domain.repository.ProjetoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjetoService {
    private final ProjetoRepository projetoRepository;
    private final HistoriaUsuarioRepository historiaUsuarioRepository;

    public Projeto criar(String nome, String descricao, String contextoDominio){
        String nomeLimpo = nome.strip();
        if(projetoRepository.existsByNome(nomeLimpo)){
            throw new ConflitoException("Já existe um projeto com o nome: '"+ nomeLimpo + "'");
        }
        return projetoRepository.save(new Projeto(nome, descricao, contextoDominio));
    }

    @Transactional
    public List<Projeto> listar(){
        return projetoRepository.findAll();
    }

    @Transactional
    public Projeto buscar(Long id){
        return projetoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Projeto", id));
    }

    public Projeto atualizar(Long id, String nome, String descricao, String contextoDominio){
        Projeto projeto = buscar(id);
        String nomeLimpo = nome.strip();
        if(projetoRepository.existsByNomeAndIdNot(nomeLimpo, id)){
            throw new ConflitoException("Já existe um projeto com o nome: '" + nomeLimpo + "'");
        }
        projeto.atualizar(nome, descricao, contextoDominio);
        return projeto;
    }

    public void excluir(Long id){
        Projeto projeto = buscar(id);
        if (historiaUsuarioRepository.existsByProjetoId(id)){
            throw new ConflitoException("O projeto possui histórias e não pode ser excluído");
        }
        projetoRepository.delete(projeto);
    }


}
