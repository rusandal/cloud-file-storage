package ru.minikhanov.cloud_storage.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.minikhanov.cloud_storage.models.LoginRequest;
import ru.minikhanov.cloud_storage.models.security.JwtResponse;
import ru.minikhanov.cloud_storage.service.AuthService;

@RestController
@Log4j2
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.getToken(loginRequest.getLogin(), loginRequest.getPassword());
        //log.info("User: "+loginRequest.getLogin()+" token: "+token);
        log.info("User: " + loginRequest.getLogin() + " was registrated with token " + token);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /*@PostMapping("/logout")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(@RequestHeader("auth-token") String token){
        authService.deleteToken(token);
    }*/
}
