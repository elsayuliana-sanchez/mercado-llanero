
package mercadito.mercadito_llanero.service;

import java.util.List;

import org.springframework.stereotype.Service;

import mercadito.mercadito_llanero.model.Categoria;
import mercadito.mercadito_llanero.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Constructor
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Consultar todas las categorías
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    // Guardar una nueva categoría
    public Categoria guardarCategoria(Categoria categoria) {

        // Verificar que el nombre no esté vacío
        if (categoria.getNombre() == null ||
            categoria.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                "El nombre de la categoría es obligatorio"
            );
        }

        // Eliminar espacios innecesarios
        String nombre = categoria.getNombre().trim();

        // Verificar si la categoría ya existe
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {

            throw new IllegalArgumentException(
                "Ya existe una categoría con ese nombre"
            );
        }

        // Asignar el nombre corregido
        categoria.setNombre(nombre);

        // Guardar la categoría en PostgreSQL
        return categoriaRepository.save(categoria);
    }

    // Buscar una categoría por su ID
    public Categoria buscarPorId(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "No se encontró la categoría"
                    )
                );
    }
}
