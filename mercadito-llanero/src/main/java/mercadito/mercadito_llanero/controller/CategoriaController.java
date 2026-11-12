
package mercadito.mercadito_llanero.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import mercadito.mercadito_llanero.model.Rol;
import mercadito.mercadito_llanero.model.Usuario;
import mercadito.mercadito_llanero.repository.UsuarioRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mercadito.mercadito_llanero.model.Categoria;
import mercadito.mercadito_llanero.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final UsuarioRepository usuarioRepository;

    // Constructor
    public CategoriaController(CategoriaService categoriaService, UsuarioRepository usuarioRepository) {
        this.categoriaService = categoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    // Consultar todas las categorías
    @GetMapping
    public List<Categoria> listarCategorias() {
        return categoriaService.listarCategorias();
    }

    // Registrar una nueva categoría
    @PostMapping
    public Categoria guardarCategoria(@RequestBody Categoria categoria, HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("usuarioId") instanceof Long usuarioId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        if (!usuario.isActivo() || usuario.getRol() != Rol.PRODUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo productores activos pueden crear categorías");
        }
        return categoriaService.guardarCategoria(categoria);
    }
}
