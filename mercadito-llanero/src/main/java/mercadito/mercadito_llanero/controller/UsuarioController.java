
package mercadito.mercadito_llanero.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mercadito.mercadito_llanero.dto.RegistroUsuarioDTO;
import mercadito.mercadito_llanero.dto.UsuarioRespuestaDTO;
import mercadito.mercadito_llanero.dto.LoginDTO;
import mercadito.mercadito_llanero.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Constructor
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ========================================
    // REGISTRO DE USUARIOS
    // ========================================

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(
            @RequestBody RegistroUsuarioDTO datos) {

        try {

            UsuarioRespuestaDTO usuario =
                    usuarioService.registrarUsuario(datos);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(usuario);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                        "mensaje",
                        "No se pudo registrar el usuario"
                    ));
        }
    }

    // ========================================
    // INICIO DE SESIÓN
    // ========================================

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(
            @RequestBody LoginDTO datos,
            HttpServletRequest request) {

        try {

            UsuarioRespuestaDTO usuario =
                    usuarioService.iniciarSesion(datos);

            // Invalidar una sesión anterior
            HttpSession anterior = request.getSession(false);

            if (anterior != null) {
                anterior.invalidate();
            }

            // Crear una nueva sesión
            HttpSession session = request.getSession(true);

            // Guardar los datos del usuario
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("nombre", usuario.getNombre());
            session.setAttribute("correo", usuario.getCorreo());
            session.setAttribute("rol", usuario.getRol());

            // Duración: 30 minutos de inactividad
            session.setMaxInactiveInterval(30 * 60);

            return ResponseEntity.ok(usuario);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // ========================================
    // CONSULTAR SESIÓN ACTUAL
    // ========================================

    @GetMapping("/sesion")
    public ResponseEntity<?> consultarSesion(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("usuarioId") == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "mensaje", "No hay una sesion activa"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "id", session.getAttribute("usuarioId"),
                "nombre", session.getAttribute("nombre"),
                "correo", session.getAttribute("correo"),
                "rol", session.getAttribute("rol")
        ));
    }

    // ========================================
    // CERRAR SESIÓN
    // ========================================

    @PostMapping("/logout")
    public ResponseEntity<?> cerrarSesion(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(Map.of(
                "mensaje", "Sesion cerrada correctamente"
        ));
    }
}
