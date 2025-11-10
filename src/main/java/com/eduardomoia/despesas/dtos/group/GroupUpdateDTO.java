package com.eduardomoia.despesas.dtos.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupUpdateDTO(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String description

) {}
