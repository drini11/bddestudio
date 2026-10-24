package dev.jppb.bddestudio.domain.repository;

import dev.jppb.bddestudio.domain.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    boolean existsByNome(String nome);
}
