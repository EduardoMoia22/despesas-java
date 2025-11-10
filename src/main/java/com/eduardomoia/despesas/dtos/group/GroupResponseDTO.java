package com.eduardomoia.despesas.dtos.group;

public record GroupResponseDTO(
        Long id,
        String name,
        String description,
        Long ownerId,
        String ownerName
) {}

