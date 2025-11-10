package com.eduardomoia.despesas.controllers;

import com.eduardomoia.despesas.dtos.expense.ExpenseCreateDTO;
import com.eduardomoia.despesas.dtos.expense.ExpenseResponseDTO;
import com.eduardomoia.despesas.dtos.expense.ExpenseUpdateDTO;
import com.eduardomoia.despesas.services.ExpenseService;
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

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ExpenseCreateDTO dto) {
        ExpenseResponseDTO created = expenseService.create(dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Lista todas as despesas, com opção de filtrar por grupo (?groupId=)
    @GetMapping
    public ResponseEntity<Page<ExpenseResponseDTO>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Long groupId,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ExpenseResponseDTO> page;

        if (groupId != null) {
            page = expenseService.findByGroup(groupId, pageable, principal.getUsername());
        } else {
            page = expenseService.findAll(pageable, principal.getUsername());
        }

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getById(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        ExpenseResponseDTO expense = expenseService.findById(id, principal.getUsername());
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateDTO dto) {

        ExpenseResponseDTO updated = expenseService.update(id, dto, principal.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        expenseService.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
