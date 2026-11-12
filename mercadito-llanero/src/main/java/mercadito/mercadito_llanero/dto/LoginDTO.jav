
package mercadito.mercadito_llanero.dto;

public class LoginDTO {

    private String correo;
    private String contrasena;

    // Constructor vacío
    public LoginDTO() {
    }

    // Obtener correo
    public String getCorreo() {
        return correo;
    }

    // Modificar correo
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    // Obtener contraseña
    public String getContrasena() {
        return contrasena;
    }

    // Modificar contraseña
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
