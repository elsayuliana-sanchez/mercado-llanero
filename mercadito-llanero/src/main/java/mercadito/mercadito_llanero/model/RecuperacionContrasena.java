
package mercadito.mercadito_llanero.model;

import java.time.LocalDateTime;

public class RecuperacionContrasena {

    private String codigo;
    private LocalDateTime expiracion;

    public RecuperacionContrasena(
            String codigo, LocalDateTime expiracion) {

        this.codigo = codigo;
        this.expiracion = expiracion;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public boolean estaExpirado() {
        return !LocalDateTime.now().isBefore(expiracion);
    }
}
