package com.eduardomoia.despesas.dtos.auth;

public record AuthResponseDTO(
        String token,
        Long userId,
        String name,
        String email
) {}
