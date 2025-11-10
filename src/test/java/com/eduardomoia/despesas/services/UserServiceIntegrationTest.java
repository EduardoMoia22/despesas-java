package com.eduardomoia.despesas.services;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret",
        "security.jwt.expiration-ms=3600000"
})
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deleteShouldFailWhenUserHasGroups() {
        User owner = persistUser("owner@example.com");

        Group group = Group.builder()
                .name("Grupo 1")
                .description("Grupo de testes")
                .owner(owner)
                .build();
        groupRepository.save(group);

        assertThatThrownBy(() -> userService.delete(owner.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("grupos");
    }

    @Test
    void deleteShouldFailWhenUserHasExpenses() {
        User owner = persistUser("owner2@example.com");

        Group group = Group.builder()
                .name("Grupo 2")
                .description("Grupo de despesas")
                .owner(owner)
                .build();
        groupRepository.save(group);

        Expense expense = Expense.builder()
                .description("Almoço")
                .amount(new BigDecimal("42.50"))
                .date(LocalDate.now())
                .category(ExpenseCategory.FOOD)
                .group(group)
                .createdBy(owner)
                .build();
        expenseRepository.save(expense);

        assertThatThrownBy(() -> userService.delete(owner.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("despesas");
    }

    private User persistUser(String email) {
        User user = User.builder()
                .name("Tester")
                .email(email)
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(Role.ROLE_USER))
                .build();
        return userRepository.save(user);
    }
}
