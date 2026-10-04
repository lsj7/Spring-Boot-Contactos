package com.miagenda.contacto;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AgendaController.class)
public class AgendaExceptionHandler {

    @ExceptionHandler(ContactoNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("detail", "Contacto no encontrado"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException exception) {
        List<Map<String, String>> detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("msg", error.getDefaultMessage() == null
                        ? "Dato no válido"
                        : error.getDefaultMessage()))
                .toList();
        if (detail.isEmpty()) {
            detail = List.of(Map.of("msg", "Dato no válido"));
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("detail", detail));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> unreadable() {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("detail", "El cuerpo de la solicitud no es válido"));
    }
}
