package dev.jppb.bddestudio.domain.repository;

import dev.jppb.bddestudio.domain.model.DecisaoSugestao;
import dev.jppb.bddestudio.domain.model.Sugestao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {

    @EntityGraph(attributePaths = {"historia", "registradaPor", "decididaPor"})
    Optional<Sugestao> findDetalhadoById(Long id);

    @EntityGraph(attributePaths = {"historia", "registradaPor", "decididaPor"})
    List<Sugestao> findByHistoriaIdOrderByIdAsc(Long historiaId);

    @EntityGraph(attributePaths = {"historia", "registradaPor", "decididaPor"})
    List<Sugestao> findByHistoriaIdAndDecisaoOrderByIdAsc(Long historiaId, DecisaoSugestao decisao);
}
