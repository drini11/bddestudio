package dev.jppb.bddestudio.domain.repository;

import dev.jppb.bddestudio.domain.model.Cenario;
import dev.jppb.bddestudio.domain.model.OrigemCenario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CenarioRepository extends JpaRepository<Cenario, Integer> {
    boolean existsByHistoriaIdAndTitulo(Long historiaId, String titulo);
    boolean existsByHistoriaIdAndTituloAndIdNot(Long historiaId, String titulo, Long id);

    @EntityGraph(attributePaths = {"passos", "historia", "historia.autor"})
    Optional<Cenario> findDetalhadoById(Long id);

    @EntityGraph(attributePaths = {"passos", "historia"})
    List<Cenario> findByHistoriaIdOrderByPosicaoAsc(Long historiaId);

    @Query("select coalesce(max(c.posicao), 0) from Cenario c where c.historia.id = :historiaId")
    int maiorPosicao(@Param("historiaId") Long historiaId);

    @Query("select c.historia.id as historiaId, count(c) as total from Cenario c "
            + "where c.historia.projeto.id = :projetoId group by c.historia.id")
    List<ContagemPorHistoria> contarPorHistoria(@Param("projetoId") Long projetoId);

    @Query("select c.origem as origem, count(c) as total from Cenario c "
            + "where c.historia.projeto.id = :projetoId group by c.origem")
    List<ContagemPorOrigem> contarPorOrigem(@Param("projetoId") Long projetoId);

    interface ContagemPorOrigem {
        OrigemCenario getOrigem();
        Long getTotal();
    }

}
