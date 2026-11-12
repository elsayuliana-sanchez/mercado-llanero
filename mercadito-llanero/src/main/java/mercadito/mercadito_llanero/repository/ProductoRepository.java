
package mercadito.mercadito_llanero.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mercadito.mercadito_llanero.model.Producto;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    // Buscar por categoria
    List<Producto> findByCategoriaId(Long categoriaId);

    // Buscar por nombre
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar productos de un productor
    List<Producto> findByProductorId(Long productorId);
}
