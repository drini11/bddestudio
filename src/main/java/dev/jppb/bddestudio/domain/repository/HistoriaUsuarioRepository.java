package dev.jppb.bddestudio.domain.repository;

import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriaUsuarioRepository extends JpaRepository<HistoriaUsuario, Long> {
    boolean existsByProjetoId(Long projetoId);
}
