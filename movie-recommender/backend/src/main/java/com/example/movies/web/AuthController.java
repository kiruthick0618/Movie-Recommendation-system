package com.example.movies.web;

import com.example.movies.model.AppUser;
import com.example.movies.repo.UserRepository;
import com.example.movies.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(String username, String email, String password, String confirm) {}
    public record LoginRequest(String username, String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthController(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@RequestBody RegisterRequest r) {
        String u = r.username() == null ? "" : r.username().trim();
        String e = r.email() == null ? "" : r.email().trim();
        String p = r.password() == null ? "" : r.password();
        String error = null;
        if (!u.matches("[A-Za-z0-9_]{3,20}")) error = "Username must be 3–20 letters, numbers or underscores.";
        else if (!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) error = "Enter a valid email address.";
        else if (p.length() < 8) error = "Password must be at least 8 characters.";
        else if (r.confirm() != null && !p.equals(r.confirm())) error = "Passwords do not match.";
        else if (users.existsByUsernameIgnoreCase(u)) error = "That username is already taken.";
        else if (users.existsByEmailIgnoreCase(e)) error = "An account with that email already exists.";
        if (error != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);

        AppUser saved = users.save(new AppUser(u, e, encoder.encode(p)));
        return Map.of("token", tokens.issue(saved.getUsername()), "username", saved.getUsername());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest r) {
        AppUser user = r.username() == null ? null : users.findByUsernameIgnoreCase(r.username().trim()).orElse(null);
        if (user == null || r.password() == null || !encoder.matches(r.password(), user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        return Map.of("token", tokens.issue(user.getUsername()), "username", user.getUsername());
    }

    @GetMapping("/me")
    public Map<String, Object> me(Principal principal) {
        AppUser user = users.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        return Map.of("username", user.getUsername(), "favorites", user.getFavorites());
    }
}
