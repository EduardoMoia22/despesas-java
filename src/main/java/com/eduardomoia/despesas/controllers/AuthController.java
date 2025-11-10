package com.eduardomoia.despesas.controllers;

import com.eduardomoia.despesas.dtos.auth.AuthRequestDTO;
import com.eduardomoia.despesas.dtos.auth.AuthResponseDTO;
import com.eduardomoia.despesas.dtos.user.UserCreateDTO;
import com.eduardomoia.despesas.dtos.user.UserResponseDTO;
import com.eduardomoia.despesas.entities.User;
import com.eduardomoia.despesas.exceptions.ResourceNotFoundException;
import com.eduardomoia.despesas.repositories.UserRepository;
import com.eduardomoia.despesas.security.JwtService;
import com.eduardomoia.despesas.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(userDetails);

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
