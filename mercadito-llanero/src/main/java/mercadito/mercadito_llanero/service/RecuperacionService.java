
package mercadito.mercadito_llanero.service;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import mercadito.mercadito_llanero.model.Usuario;
import mercadito.mercadito_llanero.model.RecuperacionContrasena;
import mercadito.mercadito_llanero.repository.UsuarioRepository;
import mercadito.mercadito_llanero.dto.SolicitarRecuperacionDTO;
import mercadito.mercadito_llanero.dto.RestablecerContrasenaDTO;

@Service
public class RecuperacionService {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final SecureRandom random = new SecureRandom();

    private final Map<String, RecuperacionContrasena> codigos =
            new HashMap<>();

    @Value("${mercadito.recuperacion.simulada:false}")
    private boolean recuperacionSimulada;

    public RecuperacionService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    // GENERAR CODIGO SEGURO
    private String generarCodigo() {

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // VALIDAR Y NORMALIZAR CORREO
    private String normalizarCorreo(String correo) {

        return ValidacionDatos.normalizarCorreo(correo);
    }

    // SOLICITAR RECUPERACION
    public synchronized void solicitarRecuperacion(
            SolicitarRecuperacionDTO datos) {

        if (!recuperacionSimulada) {
            throw new IllegalArgumentException(
                    "Recuperacion simulada deshabilitada");
        }

        if (datos == null) {
            throw new IllegalArgumentException("Debe proporcionar los datos de recuperación");
        }
        String correo = normalizarCorreo(datos.getCorreo());

        // Eliminar codigos vencidos
        codigos.entrySet().removeIf(
                entrada -> entrada.getValue().estaExpirado());

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElse(null);

        // No revelar si el correo existe
        if (usuario == null || !usuario.isActivo()) {
            return;
        }

        // Evitar generar otro codigo durante su vigencia
        if (codigos.containsKey(correo)) {
            return;
        }

        String codigo = generarCodigo();

        LocalDateTime expiracion =
                LocalDateTime.now().plusMinutes(10);

        RecuperacionContrasena recuperacion =
                new RecuperacionContrasena(
                        codigo, expiracion);

        codigos.put(correo, recuperacion);

        // Simulacion del envio de correo
        System.out.println("--------------------------------");
        System.out.println("MERCADITO LLANERO");
        System.out.println("RECUPERACION DE CONTRASENA");
        System.out.println("Correo: " + correo);
        System.out.println("Codigo: " + codigo);
        System.out.println("Valido durante 10 minutos");
        System.out.println("--------------------------------");
    }

    // RESTABLECER CONTRASENA
    public synchronized void restablecerContrasena(
            RestablecerContrasenaDTO datos) {

        if (!recuperacionSimulada) {
            throw new IllegalArgumentException(
                    "Recuperacion simulada deshabilitada");
        }

        if (datos == null) {
            throw new IllegalArgumentException("Debe proporcionar los datos de recuperación");
        }
        String correo = normalizarCorreo(datos.getCorreo());

        if (datos.getCodigo() == null ||
                datos.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar el codigo de recuperacion");
        }

        ValidacionDatos.validarContrasena(datos.getNuevaContrasena());

        RecuperacionContrasena recuperacion =
                codigos.get(correo);

        if (recuperacion == null ||
                recuperacion.estaExpirado()) {

            codigos.remove(correo);

            throw new IllegalArgumentException(
                    "El codigo es invalido o ha expirado");
        }

        if (!recuperacion.getCodigo()
                .equals(datos.getCodigo().trim())) {

            throw new IllegalArgumentException(
                    "El codigo es invalido o ha expirado");
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "No se pudo restablecer la contrasena"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException(
                    "No se pudo restablecer la contrasena");
        }

        // Proteger la nueva contrasena
        String contrasenaProtegida =
                passwordEncoder.encode(
                        datos.getNuevaContrasena());

        usuario.setContrasena(contrasenaProtegida);

        // Actualizar PostgreSQL
        usuarioRepository.save(usuario);

        // El codigo solo puede utilizarse una vez
        codigos.remove(correo);
    }
}
