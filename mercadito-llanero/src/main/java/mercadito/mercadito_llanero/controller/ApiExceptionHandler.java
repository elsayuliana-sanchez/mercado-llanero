package mercadito.mercadito_llanero.controller;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> manejarPermisos(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("mensaje",
                error.getReason() == null ? "Solicitud rechazada" : error.getReason()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> manejarValidacion(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> manejarConflicto(DataIntegrityViolationException error) {
        return ResponseEntity.status(409).body(Map.of("mensaje",
                "Los datos entran en conflicto con un registro existente o relacionado"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> manejarJson(HttpMessageNotReadableException error) {
        return ResponseEntity.badRequest().body(Map.of("mensaje",
                "Revise el cuerpo JSON y los tipos de datos enviados"));
    }
}
