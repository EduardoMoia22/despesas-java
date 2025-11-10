package com.eduardomoia.despesas.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        @Size(max = 120)
        String email,

        @NotBlank
        @Size(min = 6, max = 60)
        String password

) {}
