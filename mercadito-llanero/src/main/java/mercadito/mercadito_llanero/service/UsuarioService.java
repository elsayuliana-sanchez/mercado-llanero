
package mercadito.mercadito_llanero.service;

import java.util.Locale;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import mercadito.mercadito_llanero.dto.LoginDTO;
import mercadito.mercadito_llanero.dto.RegistroUsuarioDTO;
import mercadito.mercadito_llanero.dto.UsuarioRespuestaDTO;
import mercadito.mercadito_llanero.model.Usuario;
import mercadito.mercadito_llanero.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // REGISTRO DE USUARIOS
    public UsuarioRespuestaDTO registrarUsuario(
            RegistroUsuarioDTO datos) {

        // Validar el nombre
        if (datos == null ||
                datos.getNombre() == null ||
                datos.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio");
        }

        // Validar el correo
        ValidacionDatos.validarLongitud(datos.getNombre().trim(), 100, "El nombre");
        String correo = ValidacionDatos.normalizarCorreo(datos.getCorreo());

        // Evitar correos duplicados
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {

            throw new IllegalArgumentException(
                    "Este correo ya está registrado");
        }

        // Validar contraseña
        ValidacionDatos.validarContrasena(datos.getContrasena());

        // Validar rol
        if (datos.getRol() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar PRODUCTOR o COMPRADOR");
        }

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre().trim());
        usuario.setCorreo(correo);

        // Proteger contraseña
        usuario.setContrasena(
                passwordEncoder.encode(datos.getContrasena()));

        usuario.setRol(datos.getRol());
        usuario.setActivo(true);

        // Guardar usuario
        Usuario guardado = usuarioRepository.save(usuario);

        // Devolver respuesta sin contraseña
        return new UsuarioRespuestaDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getCorreo(),
                guardado.getRol()
        );
    }

    // INICIO DE SESIÓN
    public UsuarioRespuestaDTO iniciarSesion(LoginDTO datos) {

        // Validar datos
        if (datos == null ||
                datos.getCorreo() == null ||
                datos.getCorreo().isBlank() ||
                datos.getContrasena() == null ||
                datos.getContrasena().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar correo y contraseña");
        }

        // Buscar usuario por correo
        String correo = datos.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Correo o contraseña incorrectos")
                );

        // Verificar cuenta activa
        if (!usuario.isActivo()) {

            throw new IllegalArgumentException(
                    "La cuenta se encuentra desactivada");
        }

        // Comprobar contraseña con BCrypt
        boolean contrasenaCorrecta = passwordEncoder.matches(
                datos.getContrasena(),
                usuario.getContrasena()
        );

        if (!contrasenaCorrecta) {

            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos");
        }

        // Devolver usuario sin contraseña
        return new UsuarioRespuestaDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}
