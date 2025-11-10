package com.eduardomoia.despesas.services;

import com.eduardomoia.despesas.dtos.user.UserCreateDTO;
import com.eduardomoia.despesas.dtos.user.UserResponseDTO;
import com.eduardomoia.despesas.dtos.user.UserUpdateDTO;
import com.eduardomoia.despesas.entities.User;
import com.eduardomoia.despesas.entities.enums.Role;
import com.eduardomoia.despesas.exceptions.BusinessException;
import com.eduardomoia.despesas.exceptions.ResourceNotFoundException;
import com.eduardomoia.despesas.repositories.ExpenseRepository;
import com.eduardomoia.despesas.repositories.GroupRepository;
import com.eduardomoia.despesas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO create(UserCreateDTO dto) {
        log.debug("Criando novo usuário com email={}", dto.email());

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com esse e-mail.");
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        user.setRoles(Set.of(Role.ROLE_USER));

        User saved = userRepository.save(user);

        log.info("Usuário criado com sucesso id={} email={}", saved.getId(), saved.getEmail());
        return toResponseDTO(saved);
    }

    public Page<UserResponseDTO> findAll(Pageable pageable) {
        log.debug("Buscando usuários paginados: {}", pageable);
        return userRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public UserResponseDTO findById(Long id) {
        log.debug("Buscando usuário por id={}", id);
        User user = findEntityById(id);
        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateDTO dto) {
        log.debug("Atualizando usuário id={}", id);

        User user = findEntityById(id);

        boolean emailAlterado = !user.getEmail().equals(dto.email());
        if (emailAlterado && userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe outro usuário cadastrado com esse e-mail.");
        }

        user.setName(dto.name());
        user.setEmail(dto.email());

        User updated = userRepository.save(user);
        log.info("Usuário atualizado id={}", updated.getId());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Removendo usuário id={}", id);
        User user = findEntityById(id);

        if (groupRepository.existsByOwner_Id(id)) {
            throw new BusinessException("Não é possível remover o usuário pois ele possui grupos cadastrados.");
        }

        if (expenseRepository.existsByCreatedBy_Id(id)) {
            throw new BusinessException("Não é possível remover o usuário pois ele possui despesas cadastradas.");
        }

        userRepository.delete(user);
        log.info("Usuário removido id={}", id);
    }

    private User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado para o id: " + id));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles() // se não tiver roles, remove esse campo do DTO também
        );
    }
}
