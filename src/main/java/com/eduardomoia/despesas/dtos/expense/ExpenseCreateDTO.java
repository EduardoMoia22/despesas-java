package com.eduardomoia.despesas.dtos.expense;

import com.eduardomoia.despesas.entities.enums.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateDTO(

        @NotBlank
        @Size(max = 200)
        String description,

        @NotNull
        @Digits(integer = 10, fraction = 2)
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull
        LocalDate date,

        @NotNull
        ExpenseCategory category,

        @NotNull
        Long groupId

) {}
