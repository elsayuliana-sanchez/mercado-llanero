
package mercadito.mercadito_llanero.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import mercadito.mercadito_llanero.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

}
