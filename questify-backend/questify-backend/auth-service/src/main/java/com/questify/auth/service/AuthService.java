package com.questify.auth.service;

import com.questify.auth.dto.CreateUserRequest;
import com.questify.auth.dto.LoginRequest;
import com.questify.auth.dto.LoginResponse;
import com.questify.auth.dto.UpdateUserRequest;
import com.questify.auth.dto.UserResponse;
import com.questify.auth.entity.Role;
import com.questify.auth.entity.User;
import com.questify.auth.exception.AuthException;
import com.questify.auth.repository.UserRepository;
import com.questify.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new AuthException("Invalid email or password.", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new AuthException("Account is disabled.", HttpStatus.FORBIDDEN);
        }

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new AuthException("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(UserResponse.from(user), token);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("User not found.", HttpStatus.NOT_FOUND));
        return UserResponse.from(user);
    }

    public List<UserResponse> listUsers(String callerEmail) {
        User caller = userRepository.findByEmailIgnoreCase(callerEmail)
                .orElseThrow(() -> new AuthException("Caller not found.", HttpStatus.UNAUTHORIZED));

        List<User> users;
        if (caller.getRole() == Role.SUPER_ADMIN) {
            users = userRepository.findAll();
        } else if (caller.getInstitutionId() != null) {
            users = userRepository.findByInstitutionId(caller.getInstitutionId());
        } else {
            users = List.of(caller);
        }

        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse createUser(CreateUserRequest request, String callerEmail) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AuthException("User with email " + request.email() + " already exists.", HttpStatus.CONFLICT);
        }

        User caller = userRepository.findByEmailIgnoreCase(callerEmail).orElse(null);

        Long institutionId = request.institutionId();
        String institutionName = request.institutionName();

        if (institutionId == null && caller != null) {
            institutionId = caller.getInstitutionId();
            institutionName = caller.getInstitutionName();
        }

        String rawPassword = (request.password() != null && !request.password().isBlank()) ? request.password() : "Questify@123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User(
                request.name(),
                request.email(),
                encodedPassword,
                request.role(),
                institutionName,
                institutionId
        );
        newUser.setActive(true);

        User saved = userRepository.save(newUser);
        return UserResponse.from(saved);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request, String callerEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("User not found.", HttpStatus.NOT_FOUND));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.email() != null && !request.email().isBlank() && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.email())) {
                throw new AuthException("Email is already taken.", HttpStatus.CONFLICT);
            }
            user.setEmail(request.email());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.institutionId() != null) {
            user.setInstitutionId(request.institutionId());
        }
        if (request.institutionName() != null) {
            user.setInstitutionName(request.institutionName());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        User updated = userRepository.save(user);
        return UserResponse.from(updated);
    }

    public void deleteUser(Long id, String callerEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("User not found.", HttpStatus.NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
    }
}
