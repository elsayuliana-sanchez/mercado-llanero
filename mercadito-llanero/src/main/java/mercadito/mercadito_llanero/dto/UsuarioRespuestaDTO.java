
package mercadito.mercadito_llanero.dto;

import mercadito.mercadito_llanero.model.Rol;

public class UsuarioRespuestaDTO {

    private Long id;
    private String nombre;
    private String correo;
    private Rol rol;

    public UsuarioRespuestaDTO(
            Long id,
            String nombre,
            String correo,
            Rol rol) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public Rol getRol() {
        return rol;
    }
}
