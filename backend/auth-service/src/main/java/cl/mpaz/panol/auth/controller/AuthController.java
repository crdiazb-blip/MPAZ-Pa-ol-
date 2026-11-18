package cl.mpaz.panol.auth.controller;

import cl.mpaz.panol.auth.dto.LoginRequest;
import cl.mpaz.panol.auth.dto.LoginResponse;
import cl.mpaz.panol.auth.dto.UsuarioDto;
import cl.mpaz.panol.auth.exception.ApiException;
import cl.mpaz.panol.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(extraerToken(authorization));
    }

    /** Lo usa el api-gateway para validar cada petición. */
    @GetMapping("/validate")
    public UsuarioDto validate(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.validar(extraerToken(authorization));
    }

    @GetMapping("/me")
    public UsuarioDto me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.validar(extraerToken(authorization));
    }

    static String extraerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() < 8) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Falta el token de autenticación.");
        }
        return authorization.substring(7).trim();
    }
}
