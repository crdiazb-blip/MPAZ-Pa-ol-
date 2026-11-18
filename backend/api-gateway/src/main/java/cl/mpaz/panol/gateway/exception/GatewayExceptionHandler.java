package cl.mpaz.panol.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<Map<String, Object>> manejar(GatewayException e) {
        HttpStatus s = e.getStatus();
        return ResponseEntity.status(s)
                .body(Map.of("status", s.value(), "error", s.getReasonPhrase(), "message", e.getMessage()));
    }
}
