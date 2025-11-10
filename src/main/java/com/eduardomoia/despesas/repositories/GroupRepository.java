package com.eduardomoia.despesas.repositories;

import com.eduardomoia.despesas.entities.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {

    boolean existsByName(String name);

    Page<Group> findByOwner_Email(String email, Pageable pageable);

    boolean existsByOwner_Id(Long ownerId);
}
