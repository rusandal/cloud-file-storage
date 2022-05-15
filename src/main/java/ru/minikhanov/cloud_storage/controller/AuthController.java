package ru.minikhanov.cloud_storage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.minikhanov.cloud_storage.models.LoginRequest;
import ru.minikhanov.cloud_storage.models.security.JwtResponse;
import ru.minikhanov.cloud_storage.service.AuthService;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        System.out.println("post login");
        String token = authService.getToken(loginRequest.getLogin(), loginRequest.getPassword());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @GetMapping("/login?logout")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(@RequestHeader("auth-token") String token){
        System.out.println("post logout");
        authService.deleteToken(token);
    }
}
