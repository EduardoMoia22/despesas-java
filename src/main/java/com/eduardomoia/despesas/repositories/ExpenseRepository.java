package com.eduardomoia.despesas.repositories;

import com.eduardomoia.despesas.entities.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByGroup_Id(Long groupId, Pageable pageable);

    Page<Expense> findByGroup_Owner_Email(String email, Pageable pageable);

    boolean existsByCreatedBy_Id(Long createdById);

    boolean existsByGroup_Id(Long groupId);
}
