package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectExpenseRepository extends JpaRepository<ProjectExpense, Long> {

    List<ProjectExpense> findAllByProjectIdOrderByExpenseDateDescIdDesc(Long projectId);

    List<ProjectExpense> findAllByExpenseDateBetween(LocalDate from, LocalDate to);

    boolean existsByProjectId(Long projectId);

}
