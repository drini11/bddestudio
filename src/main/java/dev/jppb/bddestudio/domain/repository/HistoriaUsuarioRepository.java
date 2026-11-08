package dev.jppb.bddestudio.domain.repository;

import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Prioridade;
import dev.jppb.bddestudio.domain.model.Projeto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoriaUsuarioRepository extends JpaRepository<HistoriaUsuario, Long> {

    boolean existsByProjetoId(Long projetoId);
    boolean existsByProjetoIdAndTitulo(Long projetoId, String titulo);
    boolean existsByProjetoIdAndTituloAndIdNot(Long projetoId, String titulo, Long id);
    long countByProjetoId(Long projetoId);

    @EntityGraph(attributePaths = {"autor", "projeto"})
    Optional<HistoriaUsuario> findDetalhadoById(Long id);

    @EntityGraph(attributePaths = {"autor", "projeto"})
    List<HistoriaUsuario> findByProjetoIdOrderByIdAsc(Long projetoId);

    @EntityGraph(attributePaths = {"autor", "projeto"})
    List<HistoriaUsuario> findByProjetoIdAndPrioridadeOrderByIdAsc(Long projetoId, Prioridade prioridade);
}
