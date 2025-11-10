package com.eduardomoia.despesas.controllers;

import com.eduardomoia.despesas.dtos.expense.ExpenseResponseDTO;
import com.eduardomoia.despesas.dtos.group.GroupCreateDTO;
import com.eduardomoia.despesas.dtos.group.GroupResponseDTO;
import com.eduardomoia.despesas.dtos.group.GroupUpdateDTO;
import com.eduardomoia.despesas.services.ExpenseService;
import com.eduardomoia.despesas.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<GroupResponseDTO> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody GroupCreateDTO dto) {
        GroupResponseDTO created = groupService.create(dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<GroupResponseDTO>> list(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<GroupResponseDTO> page = groupService.findAll(pageable, principal.getUsername());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> getById(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        GroupResponseDTO group = groupService.findById(id, principal.getUsername());
        return ResponseEntity.ok(group);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody GroupUpdateDTO dto) {

        GroupResponseDTO updated = groupService.update(id, dto, principal.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        groupService.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<Page<ExpenseResponseDTO>> listExpensesByGroup(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ExpenseResponseDTO> page = expenseService.findByGroup(groupId, pageable, principal.getUsername());
        return ResponseEntity.ok(page);
    }
}
