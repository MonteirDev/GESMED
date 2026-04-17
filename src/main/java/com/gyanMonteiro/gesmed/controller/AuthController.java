package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.RequestDTO.LoginAuthRequestDTO;
import com.gyanMonteiro.gesmed.RequestDTO.RegisterAuthRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.LoginResponseDTO;
import com.gyanMonteiro.gesmed.Service.UserDetailsServiceImpl;
import com.gyanMonteiro.gesmed.config.security.JwtService;
import com.gyanMonteiro.gesmed.entity.User;
import com.gyanMonteiro.gesmed.enums.Role;
import com.gyanMonteiro.gesmed.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginAuthRequestDTO dto){
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.username(), dto.password())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterAuthRequestDTO dto){
        if (userRepository.findByUsername(dto.username()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists!");
        }
        User user = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()),
                Role.ROLE_ADMIN
        );

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User created successfully.");
    }
}
