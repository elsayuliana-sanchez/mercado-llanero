package mercadito.mercadito_llanero.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class ValidacionDatos {
    private ValidacionDatos() {
    }

    public static void validarLongitud(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(campo + " supera el máximo de " + maximo + " caracteres");
        }
    }

    public static String normalizarCorreo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        String correo = valor.trim().toLowerCase(Locale.ROOT);
        validarLongitud(correo, 150, "El correo");
        if (!correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido");
        }
        return correo;
    }

    public static void validarContrasena(String valor) {
        if (valor == null || valor.isBlank() || valor.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres y no estar vacía");
        }
        if (valor.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña supera el límite de 72 bytes de BCrypt");
        }
    }
}
