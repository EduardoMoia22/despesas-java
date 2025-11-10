package com.eduardomoia.despesas.services;

import com.eduardomoia.despesas.dtos.expense.ExpenseCreateDTO;
import com.eduardomoia.despesas.dtos.expense.ExpenseResponseDTO;
import com.eduardomoia.despesas.dtos.expense.ExpenseUpdateDTO;
import com.eduardomoia.despesas.entities.Expense;
import com.eduardomoia.despesas.entities.Group;
import com.eduardomoia.despesas.entities.User;
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
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponseDTO create(ExpenseCreateDTO dto, String authenticatedEmail) {
        log.debug("Criando despesa para groupId={} user={}", dto.groupId(), authenticatedEmail);

        User currentUser = findUserByEmail(authenticatedEmail);
        Group group = findGroup(dto.groupId());
        validateGroupOwnership(group, currentUser);

        Expense expense = new Expense();
        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setDate(dto.date());
        expense.setCategory(dto.category());
        expense.setGroup(group);
        expense.setCreatedBy(currentUser);

        Expense saved = expenseRepository.save(expense);

        log.info("Despesa criada id={} groupId={} por user={}", saved.getId(), group.getId(), currentUser.getEmail());
        return toResponseDTO(saved);
    }

    public Page<ExpenseResponseDTO> findAll(Pageable pageable, String authenticatedEmail) {
        log.debug("Listando despesas user={} pageable={}", authenticatedEmail, pageable);
        findUserByEmail(authenticatedEmail); // garante existência
        return expenseRepository.findByGroup_Owner_Email(authenticatedEmail, pageable)
                .map(this::toResponseDTO);
    }

    public Page<ExpenseResponseDTO> findByGroup(Long groupId, Pageable pageable, String authenticatedEmail) {
        log.debug("Listando despesas do grupo groupId={} user={} pageable={}", groupId, authenticatedEmail, pageable);
        User currentUser = findUserByEmail(authenticatedEmail);
        Group group = findOwnedGroup(groupId, currentUser);
        return expenseRepository.findByGroup_Id(group.getId(), pageable)
                .map(this::toResponseDTO);
    }

    public ExpenseResponseDTO findById(Long id, String authenticatedEmail) {
        log.debug("Buscando despesa id={} user={}", id, authenticatedEmail);
        User currentUser = findUserByEmail(authenticatedEmail);
        Expense expense = findOwnedExpense(id, currentUser);
        return toResponseDTO(expense);
    }

    @Transactional
    public ExpenseResponseDTO update(Long id, ExpenseUpdateDTO dto, String authenticatedEmail) {
        log.debug("Atualizando despesa id={} user={}", id, authenticatedEmail);

        User currentUser = findUserByEmail(authenticatedEmail);
        Expense expense = findOwnedExpense(id, currentUser);

        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setDate(dto.date());
        expense.setCategory(dto.category());

        Expense updated = expenseRepository.save(expense);
        log.info("Despesa atualizada id={} por user={}", updated.getId(), currentUser.getEmail());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id, String authenticatedEmail) {
        log.debug("Removendo despesa id={} user={}", id, authenticatedEmail);
        User currentUser = findUserByEmail(authenticatedEmail);
        Expense expense = findOwnedExpense(id, currentUser);
        expenseRepository.delete(expense);
        log.info("Despesa removida id={} por user={}", id, currentUser.getEmail());
    }

    private Expense findOwnedExpense(Long id, User owner) {
        Expense expense = findEntityById(id);
        validateGroupOwnership(expense.getGroup(), owner);
        return expense;
    }

    private Group findOwnedGroup(Long groupId, User owner) {
        Group group = findGroup(groupId);
        validateGroupOwnership(group, owner);
        return group;
    }

    private Group findGroup(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado para o id: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
    }

    private void validateGroupOwnership(Group group, User owner) {
        if (group.getOwner() == null || !group.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("Usuário não possui acesso às despesas do grupo solicitado.");
        }
    }

    private Expense findEntityById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Despesa não encontrada para o id: " + id));
    }

    private ExpenseResponseDTO toResponseDTO(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getDate(),
                expense.getCategory(),
                expense.getGroup() != null ? expense.getGroup().getId() : null,
                expense.getGroup() != null ? expense.getGroup().getName() : null,
                expense.getCreatedBy() != null ? expense.getCreatedBy().getId() : null,
                expense.getCreatedBy() != null ? expense.getCreatedBy().getName() : null
        );
    }
}
