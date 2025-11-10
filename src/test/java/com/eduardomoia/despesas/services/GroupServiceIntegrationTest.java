package com.eduardomoia.despesas.services;

import com.eduardomoia.despesas.dtos.group.GroupCreateDTO;
import com.eduardomoia.despesas.entities.Expense;
import com.eduardomoia.despesas.entities.Group;
import com.eduardomoia.despesas.entities.User;
import com.eduardomoia.despesas.entities.enums.ExpenseCategory;
import com.eduardomoia.despesas.entities.enums.Role;
import com.eduardomoia.despesas.exceptions.BusinessException;
import com.eduardomoia.despesas.repositories.ExpenseRepository;
import com.eduardomoia.despesas.repositories.GroupRepository;
import com.eduardomoia.despesas.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret",
        "security.jwt.expiration-ms=3600000"
})
@Transactional
class GroupServiceIntegrationTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deleteShouldFailWhenGroupHasExpenses() {
        User owner = persistUser("group-owner@example.com");
        Group group = createGroup(owner, "Grupo despesas");

        Expense expense = Expense.builder()
                .description("Cinema")
                .amount(new BigDecimal("25.00"))
                .date(LocalDate.now())
                .category(ExpenseCategory.ENTERTAINMENT)
                .group(group)
                .createdBy(owner)
                .build();
        expenseRepository.save(expense);

        assertThatThrownBy(() -> groupService.delete(group.getId(), owner.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("despesas");
    }

    @Test
    void deleteShouldFailForNonOwner() {
        User owner = persistUser("real-owner@example.com");
        User intruder = persistUser("intruder@example.com");
        Group group = createGroup(owner, "Grupo secreto");

        assertThatThrownBy(() -> groupService.delete(group.getId(), intruder.getEmail()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createShouldAssociateAuthenticatedUserAsOwner() {
        User owner = persistUser("creator@example.com");
        GroupCreateDTO dto = new GroupCreateDTO("Novo Grupo", "Descricao");

        var response = groupService.create(dto, owner.getEmail());

        assertThat(response.ownerId()).isEqualTo(owner.getId());
    }

    private Group createGroup(User owner, String name) {
        Group group = Group.builder()
                .name(name)
                .description("Descricao")
                .owner(owner)
                .build();
        return groupRepository.save(group);
    }

    private User persistUser(String email) {
        User user = User.builder()
                .name("User")
                .email(email)
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(Role.ROLE_USER))
                .build();
        return userRepository.save(user);
    }
}
