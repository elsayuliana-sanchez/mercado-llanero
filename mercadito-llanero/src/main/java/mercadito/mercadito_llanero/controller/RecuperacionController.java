
package mercadito.mercadito_llanero.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mercadito.mercadito_llanero.dto.SolicitarRecuperacionDTO;
import mercadito.mercadito_llanero.dto.RestablecerContrasenaDTO;
import mercadito.mercadito_llanero.service.RecuperacionService;

@RestController
@RequestMapping("/api/usuarios")
public class RecuperacionController {

    private final RecuperacionService recuperacionService;

    public RecuperacionController(
            RecuperacionService recuperacionService) {

        this.recuperacionService = recuperacionService;
    }

    // SOLICITAR CODIGO
    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<?> solicitarRecuperacion(
            @RequestBody SolicitarRecuperacionDTO datos) {

        try {

            recuperacionService.solicitarRecuperacion(datos);

            return ResponseEntity.ok(Map.of(
                "mensaje",
                "Si el correo esta registrado, se enviara un codigo de recuperacion"
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CAMBIAR CONTRASENA
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<?> restablecerContrasena(
            @RequestBody RestablecerContrasenaDTO datos) {

        try {

            recuperacionService.restablecerContrasena(datos);

            return ResponseEntity.ok(Map.of(
                "mensaje",
                "Contrasena actualizada correctamente"
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
