
package mercadito.mercadito_llanero.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import mercadito.mercadito_llanero.model.Producto;
import mercadito.mercadito_llanero.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    // Constructor
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // OBTENER ID DEL USUARIO AUTENTICADO
    private Long obtenerUsuarioId(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Long) session.getAttribute("usuarioId");
    }

    // CONSULTAR TODOS LOS PRODUCTOS
    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarProductos();
    }

    // BUSCAR POR CATEGORIA
    @GetMapping("/categoria/{id}")
    public List<Producto> buscarPorCategoria(
            @PathVariable Long id) {

        return productoService.buscarPorCategoria(id);
    }

    // BUSCAR POR NOMBRE
    @GetMapping("/buscar")
    public List<Producto> buscarPorNombre(
            @RequestParam String nombre) {

        return productoService.buscarPorNombre(nombre);
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public Producto buscarPorId(@PathVariable Long id) {

        return productoService.buscarPorId(id);
    }

    // CONSULTAR MIS PRODUCTOS
    @GetMapping("/mis-productos")
    public List<Producto> listarMisProductos(
            HttpServletRequest request) {

        Long usuarioId = obtenerUsuarioId(request);

        return productoService.listarMisProductos(usuarioId);
    }

    // REGISTRAR PRODUCTO
    @PostMapping
    public ResponseEntity<?> guardarProducto(
            @RequestBody Producto producto,
            HttpServletRequest request) {

        Long usuarioId = obtenerUsuarioId(request);

        Producto nuevo = productoService
                .guardarProducto(producto, usuarioId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevo);
    }

    // ACTUALIZAR PRODUCTO
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Producto producto,
            HttpServletRequest request) {

        Long usuarioId = obtenerUsuarioId(request);

        Producto actualizado = productoService
                .actualizarProducto(id, producto, usuarioId);

        return ResponseEntity.ok(actualizado);
    }

    // ELIMINAR PRODUCTO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long usuarioId = obtenerUsuarioId(request);

        productoService.eliminarProducto(id, usuarioId);

        return ResponseEntity.ok(
                Map.of("mensaje",
                       "Producto eliminado correctamente")
        );
    }

    // MANEJAR ERRORES DE VALIDACION
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> manejarValidacion(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", e.getMessage()));
    }

    // MANEJAR ERRORES DE PERMISOS
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> manejarPermisos(
            ResponseStatusException e) {

        return ResponseEntity
                .status(e.getStatusCode())
                .body(Map.of("mensaje", e.getReason()));
    }
}
