package com.eduardomoia.despesas.services;

import com.eduardomoia.despesas.dtos.group.GroupCreateDTO;
import com.eduardomoia.despesas.dtos.group.GroupResponseDTO;
import com.eduardomoia.despesas.dtos.group.GroupUpdateDTO;
import com.eduardomoia.despesas.entities.Group;
import com.eduardomoia.despesas.entities.User;
import com.eduardomoia.despesas.exceptions.BusinessException;
import com.eduardomoia.despesas.exceptions.ResourceNotFoundException;
import com.eduardomoia.despesas.repositories.ExpenseRepository;
import com.eduardomoia.despesas.repositories.GroupRepository;
import com.eduardomoia.despesas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public GroupResponseDTO create(GroupCreateDTO dto, String authenticatedEmail) {
        log.debug("Criando novo grupo name={} authenticatedUser={}", dto.name(), authenticatedEmail);

        if (groupRepository.existsByName(dto.name())) {
            throw new BusinessException("Já existe um grupo cadastrado com esse nome.");
        }

        User owner = findUserByEmail(authenticatedEmail);

        Group group = new Group();
        group.setName(dto.name());
        group.setDescription(dto.description());
        group.setOwner(owner);

        Group saved = groupRepository.save(group);

        log.info("Grupo criado com sucesso id={} name={} owner={}", saved.getId(), saved.getName(), owner.getEmail());
        return toResponseDTO(saved);
    }

    public Page<GroupResponseDTO> findAll(Pageable pageable, String authenticatedEmail) {
        log.debug("Buscando grupos paginados para user={} pageable={}", authenticatedEmail, pageable);
        findUserByEmail(authenticatedEmail); // garante que o usuário exista

        return groupRepository.findByOwner_Email(authenticatedEmail, pageable)
                .map(this::toResponseDTO);
    }

    public GroupResponseDTO findById(Long id, String authenticatedEmail) {
        log.debug("Buscando grupo por id={} user={}", id, authenticatedEmail);
        User owner = findUserByEmail(authenticatedEmail);
        Group group = findOwnedGroup(id, owner);
        return toResponseDTO(group);
    }

    @Transactional
    public GroupResponseDTO update(Long id, GroupUpdateDTO dto, String authenticatedEmail) {
        log.debug("Atualizando grupo id={} user={}", id, authenticatedEmail);

        User owner = findUserByEmail(authenticatedEmail);
        Group group = findOwnedGroup(id, owner);

        boolean nomeAlterado = !group.getName().equals(dto.name());
        if (nomeAlterado && groupRepository.existsByName(dto.name())) {
            throw new BusinessException("Já existe outro grupo cadastrado com esse nome.");
        }

        group.setName(dto.name());
        group.setDescription(dto.description());

        Group updated = groupRepository.save(group);
        log.info("Grupo atualizado id={} por user={}", updated.getId(), owner.getEmail());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id, String authenticatedEmail) {
        log.debug("Removendo grupo id={} user={}", id, authenticatedEmail);
        User owner = findUserByEmail(authenticatedEmail);
        Group group = findOwnedGroup(id, owner);

        if (expenseRepository.existsByGroup_Id(id)) {
            throw new BusinessException("Não é possível remover o grupo pois existem despesas associadas.");
        }

        groupRepository.delete(group);
        log.info("Grupo removido id={} por user={}", id, owner.getEmail());
    }

    private Group findOwnedGroup(Long id, User owner) {
        Group group = findEntityById(id);
        validateOwnership(group, owner);
        return group;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
    }

    private void validateOwnership(Group group, User owner) {
        if (group.getOwner() == null || !group.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("Usuário não possui acesso ao grupo solicitado.");
        }
    }

    private Group findEntityById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grupo não encontrado para o id: " + id));
    }

    private GroupResponseDTO toResponseDTO(Group group) {
        return new GroupResponseDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getOwner() != null ? group.getOwner().getId() : null,
                group.getOwner() != null ? group.getOwner().getName() : null
        );
    }
}
