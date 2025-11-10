package com.eduardomoia.despesas.dtos.user;

import com.eduardomoia.despesas.entities.enums.Role;

import java.util.Set;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Set<Role> roles
) {}
