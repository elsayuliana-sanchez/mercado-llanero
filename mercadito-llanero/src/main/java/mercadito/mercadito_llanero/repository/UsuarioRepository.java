
package mercadito.mercadito_llanero.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mercadito.mercadito_llanero.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Comprobar si un correo ya está registrado
    boolean existsByCorreoIgnoreCase(String correo);

    // Buscar un usuario por su correo
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}
