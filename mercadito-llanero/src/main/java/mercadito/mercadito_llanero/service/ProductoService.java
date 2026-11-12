
package mercadito.mercadito_llanero.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import mercadito.mercadito_llanero.model.Producto;
import mercadito.mercadito_llanero.model.Categoria;
import mercadito.mercadito_llanero.model.Usuario;
import mercadito.mercadito_llanero.model.Rol;

import mercadito.mercadito_llanero.repository.ProductoRepository;
import mercadito.mercadito_llanero.repository.CategoriaRepository;
import mercadito.mercadito_llanero.repository.UsuarioRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    // Constructor
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // CONSULTAR TODOS LOS PRODUCTOS
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    // BUSCAR POR CATEGORIA
    public List<Producto> buscarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    // BUSCAR POR NOMBRE
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository
                .findByNombreContainingIgnoreCase(nombre);
    }

    // BUSCAR POR ID
    public Producto buscarPorId(Long id) {

        return productoRepository.findById(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el producto"
                    )
                );
    }

    // VALIDAR QUE EL USUARIO SEA PRODUCTOR
    private Usuario validarProductor(Long usuarioId) {

        if (usuarioId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debe iniciar sesion");
        }

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado")
                );

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La cuenta esta desactivada");
        }

        if (usuario.getRol() != Rol.PRODUCTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los productores pueden administrar productos");
        }

        return usuario;
    }

    // VALIDAR PROPIETARIO DEL PRODUCTO
    private void validarPropietario(
            Producto producto, Usuario productor) {

        if (producto.getProductor() == null ||
            !producto.getProductor().getId()
                    .equals(productor.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene permiso para modificar este producto");
        }
    }

    // CONSULTAR MIS PRODUCTOS
    public List<Producto> listarMisProductos(Long usuarioId) {

        Usuario productor = validarProductor(usuarioId);

        return productoRepository
                .findByProductorId(productor.getId());
    }

    // VALIDAR DATOS DEL PRODUCTO
    private Categoria validarProducto(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "Debe proporcionar los datos del producto");
        }

        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio");
        }

        ValidacionDatos.validarLongitud(producto.getNombre().trim(), 100, "El nombre del producto");
        ValidacionDatos.validarLongitud(producto.getDescripcion(), 300, "La descripción del producto");

        if (producto.getPrecio() == null ||
                producto.getPrecio()
                    .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero");
        }

        BigDecimal precio = producto.getPrecio().stripTrailingZeros();
        if (precio.scale() > 2 || precio.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("El precio admite hasta 10 enteros y 2 decimales");
        }

        if (producto.getCantidad() == null ||
                producto.getCantidad() < 0) {

            throw new IllegalArgumentException(
                    "La cantidad no puede ser negativa");
        }

        if (producto.getCategoria() == null ||
                producto.getCategoria().getId() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una categoria");
        }

        return categoriaRepository
                .findById(producto.getCategoria().getId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "La categoria seleccionada no existe")
                );
    }

    // CREAR PRODUCTO
    public Producto guardarProducto(
            Producto producto, Long usuarioId) {

        Usuario productor = validarProductor(usuarioId);

        Categoria categoria = validarProducto(producto);

        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(categoria);

        // Asignar el productor autenticado
        producto.setProductor(productor);

        return productoRepository.save(producto);
    }

    // ACTUALIZAR PRODUCTO
    public Producto actualizarProducto(
            Long id, Producto datos, Long usuarioId) {

        Usuario productor = validarProductor(usuarioId);

        Producto producto = buscarPorId(id);

        // Comprobar que el producto le pertenece
        validarPropietario(producto, productor);

        Categoria categoria = validarProducto(datos);

        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setCantidad(datos.getCantidad());
        producto.setDisponible(datos.isDisponible());
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    // ELIMINAR PRODUCTO
    public void eliminarProducto(Long id, Long usuarioId) {

        Usuario productor = validarProductor(usuarioId);

        Producto producto = buscarPorId(id);

        // Comprobar que el producto le pertenece
        validarPropietario(producto, productor);

        productoRepository.delete(producto);
    }
}
