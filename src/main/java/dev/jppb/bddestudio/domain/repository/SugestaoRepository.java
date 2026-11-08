package dev.jppb.bddestudio.domain.repository;

import dev.jppb.bddestudio.domain.model.DecisaoSugestao;
import dev.jppb.bddestudio.domain.model.MotivoRejeicao;
import dev.jppb.bddestudio.domain.model.Sugestao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {

    @EntityGraph(attributePaths = {"historia", "registradaPor", "decididaPor"})
    Optional<Sugestao> findDetalhadoById(Long id);

    @EntityGraph(attributePaths = {"historia", "registradaPor", "decididaPor"})
    List<Sugestao> findByHistoriaIdOrderByIdAsc(Long historiaId);

    @EntityGraph(attributePaths = {"historia", "registradaPor", "decididaPor"})
    List<Sugestao> findByHistoriaIdAndDecisaoOrderByIdAsc(Long historiaId, DecisaoSugestao decisao);

    boolean existsByHistoriaIdAndDecisao(Long historiaId, DecisaoSugestao decisao);

    @Query("select s.historia.id as historiaId, count(s) as total from Sugestao s "
            + "where s.historia.projeto.id = :projetoId and s.decisao = :decisao "
            + "group by s.historia.id")
    List<ContagemPorHistoria> contarPorHistoria(@Param("projetoId") Long projetoId,
                                                @Param("decisao") DecisaoSugestao decisao);

    @Query("select s.decisao as decisao, count(s) as total from Sugestao s "
            + "where s.historia.projeto.id = :projetoId group by s.decisao")
    List<ContagemPorDecisao> contarPorDecisao(@Param("projetoId") Long projetoId);

    @Query("select s.motivoRejeicao as motivo, count(s) as total from Sugestao s "
            + "where s.historia.projeto.id = :projetoId and s.motivoRejeicao is not null "
            + "group by s.motivoRejeicao")
    List<ContagemPorMotivo> contarPorMotivo(@Param("projetoId") Long projetoId);

    interface ContagemPorDecisao {
        DecisaoSugestao getDecisao();
        Long getTotal();
    }

    interface ContagemPorMotivo {
        MotivoRejeicao getMotivo();
        Long getTotal();
    }
}
