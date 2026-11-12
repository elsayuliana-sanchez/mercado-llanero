
package mercadito.mercadito_llanero.dto;

import mercadito.mercadito_llanero.model.Rol;

public class RegistroUsuarioDTO {

    private String nombre;
    private String correo;
    private String contrasena;
    private Rol rol;

    public RegistroUsuarioDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
