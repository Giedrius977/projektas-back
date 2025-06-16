package lt.ca.javau12.furnibay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OpenAPIExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOpenApiException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("API Documentation Error: " + ex.getMessage());
    }
}

