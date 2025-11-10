package com.eduardomoia.despesas.dtos.expense;

import com.eduardomoia.despesas.entities.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        LocalDate date,
        ExpenseCategory category,
        Long groupId,
        String groupName,
        Long createdById,
        String createdByName
) {}
