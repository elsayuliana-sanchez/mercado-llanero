package mercadito.mercadito_llanero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import mercadito.mercadito_llanero.controller.*;
import mercadito.mercadito_llanero.dto.*;
import mercadito.mercadito_llanero.model.*;
import mercadito.mercadito_llanero.repository.*;
import mercadito.mercadito_llanero.service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FuncionalidadTests {
    private UsuarioRepository usuarios;
    private ProductoRepository productos;
    private CategoriaRepository categorias;
    private ProductoService productoService;
    private Usuario productor;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        productos = mock(ProductoRepository.class);
        categorias = mock(CategoriaRepository.class);
        productoService = new ProductoService(productos, categorias, usuarios);
        productor = new Usuario();
        productor.setId(1L);
        productor.setRol(Rol.PRODUCTOR);
        productor.setActivo(true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(productor));
    }

    @Test
    void categoriaConIdSeCreaComoNueva() {
        Categoria categoria = new Categoria();
        categoria.setId(99L);
        categoria.setNombre(" Frutas ");
        when(categorias.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        new CategoriaService(categorias).guardarCategoria(categoria);
        assertNull(categoria.getId());
        assertEquals("Frutas", categoria.getNombre());
        verify(categorias).save(categoria);
    }

    @Test
    void categoriaInvalidaDevuelve400DesdeHttp() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new CategoriaController(new CategoriaService(categorias), usuarios))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioId", 1L);
        mvc.perform(post("/api/categorias").session(sesion).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\"}")).andExpect(status().isBadRequest());
        verify(categorias, never()).save(any());
    }

    @Test
    void categoriaDuplicadaYDemasiadoLargaSeRechazan() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Frutas");
        when(categorias.existsByNombreIgnoreCase("Frutas")).thenReturn(true);
        CategoriaService servicio = new CategoriaService(categorias);
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarCategoria(categoria));
        categoria.setNombre("a".repeat(81));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarCategoria(categoria));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarCategoria(null));
        verify(categorias, never()).save(any());
    }

    @Test
    void crearCategoriaExigeProductorActivo() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new CategoriaController(new CategoriaService(categorias), usuarios))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Frutas\"}")).andExpect(status().isUnauthorized());
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioId", 1L);
        productor.setRol(Rol.COMPRADOR);
        mvc.perform(post("/api/categorias").session(sesion).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Frutas\"}")).andExpect(status().isForbidden());
        productor.setRol(Rol.PRODUCTOR);
        when(categorias.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        mvc.perform(post("/api/categorias").session(sesion).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Frutas\"}")).andExpect(status().isOk());
        verify(categorias, times(1)).save(any());
    }

    @Test
    void jsonInvalidoDevuelve400() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new CategoriaController(new CategoriaService(categorias), usuarios))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{invalido")).andExpect(status().isBadRequest());
    }

    @Test
    void registroNormalizaYProtegeContrasenaYPermiteLogin() {
        RegistroUsuarioDTO datos = registro();
        when(usuarios.save(any())).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setId(5L);
            when(usuarios.findByCorreoIgnoreCase(usuario.getCorreo())).thenReturn(Optional.of(usuario));
            assertTrue(new BCryptPasswordEncoder().matches(datos.getContrasena(), usuario.getContrasena()));
            return usuario;
        });
        UsuarioService servicio = new UsuarioService(usuarios);
        assertEquals("ana@example.com", servicio.registrarUsuario(datos).getCorreo());
        LoginDTO login = new LoginDTO();
        login.setCorreo(" ANA@EXAMPLE.COM ");
        login.setContrasena(datos.getContrasena());
        assertEquals(5L, servicio.iniciarSesion(login).getId());
        login.setContrasena("incorrecta");
        assertThrows(IllegalArgumentException.class, () -> servicio.iniciarSesion(login));
    }

    @Test
    void registroRechazaNombreExcesivoYContrasenaIncompatible() {
        UsuarioService servicio = new UsuarioService(usuarios);
        RegistroUsuarioDTO datos = registro();
        datos.setNombre("a".repeat(101));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarUsuario(datos));
        datos.setNombre("Ana");
        datos.setContrasena("á".repeat(37));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarUsuario(datos));
        datos.setContrasena("        ");
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarUsuario(datos));
        verify(usuarios, never()).save(any());
    }

    @Test
    void loginRenuevaSesionYLogoutBloqueaConsulta() {
        UsuarioService servicio = mock(UsuarioService.class);
        when(servicio.iniciarSesion(any())).thenReturn(
                new UsuarioRespuestaDTO(1L, "Ana", "ana@example.com", Rol.PRODUCTOR));
        UsuarioController controlador = new UsuarioController(servicio);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession anterior = new MockHttpSession();
        request.setSession(anterior);
        controlador.iniciarSesion(new LoginDTO(), request);
        assertTrue(anterior.isInvalid());
        assertEquals(1800, request.getSession(false).getMaxInactiveInterval());
        assertEquals(200, controlador.consultarSesion(request).getStatusCode().value());
        controlador.cerrarSesion(request);
        assertEquals(401, controlador.consultarSesion(request).getStatusCode().value());
    }

    @Test
    void visitanteYCompradorNoPuedenPublicar() {
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> productoService.guardarProducto(new Producto(), null)).getStatusCode().value());
        productor.setRol(Rol.COMPRADOR);
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> productoService.guardarProducto(new Producto(), 1L)).getStatusCode().value());
        verify(productos, never()).save(any());
    }

    @Test
    void productorAjenoNoPuedeEliminar() {
        Usuario otro = new Usuario();
        otro.setId(2L);
        Producto producto = new Producto();
        producto.setProductor(otro);
        when(productos.findById(7L)).thenReturn(Optional.of(producto));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> productoService.eliminarProducto(7L, 1L)).getStatusCode().value());
        verify(productos, never()).delete(any());
    }

    @Test
    void precioFueraDeColumnaSeRechazaAntesDeGuardar() {
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setPrecio(new BigDecimal("10000000000.00"));
        assertThrows(IllegalArgumentException.class, () -> productoService.guardarProducto(producto, 1L));
        producto.setPrecio(new BigDecimal("1.001"));
        assertThrows(IllegalArgumentException.class, () -> productoService.guardarProducto(producto, 1L));
        verify(productos, never()).save(any());
    }

    @Test
    void publicacionAsignaPropietarioDesdeSesion() {
        Categoria categoria = new Categoria();
        categoria.setId(3L);
        when(categorias.findById(3L)).thenReturn(Optional.of(categoria));
        Producto producto = new Producto();
        producto.setId(99L);
        producto.setNombre(" Arroz ");
        producto.setCantidad(2);
        producto.setPrecio(new BigDecimal("1500.00"));
        producto.setCategoria(categoria);
        producto.setProductor(new Usuario());
        when(productos.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        Producto guardado = productoService.guardarProducto(producto, 1L);
        assertSame(productor, guardado.getProductor());
        assertNull(guardado.getId());
        assertEquals("Arroz", guardado.getNombre());
    }

    @Test
    void recuperacionRechazaCorreoInvalidoYNulos() {
        RecuperacionService servicio = recuperacion();
        SolicitarRecuperacionDTO datos = new SolicitarRecuperacionDTO();
        datos.setCorreo("correo-invalido");
        assertThrows(IllegalArgumentException.class, () -> servicio.solicitarRecuperacion(datos));
        assertThrows(IllegalArgumentException.class, () -> servicio.solicitarRecuperacion(null));
        assertThrows(IllegalArgumentException.class, () -> servicio.restablecerContrasena(null));
        verifyNoInteractions(usuarios);
    }

    @Test
    @SuppressWarnings("unchecked")
    void recuperacionCambiaHashYElCodigoSoloFuncionaUnaVez() {
        RecuperacionService servicio = recuperacion();
        Map<String, RecuperacionContrasena> codigos = (Map<String, RecuperacionContrasena>)
                ReflectionTestUtils.getField(servicio, "codigos");
        codigos.put("ana@example.com", new RecuperacionContrasena("demostracion", LocalDateTime.now().plusMinutes(10)));
        when(usuarios.findByCorreoIgnoreCase("ana@example.com")).thenReturn(Optional.of(productor));
        RestablecerContrasenaDTO datos = new RestablecerContrasenaDTO();
        datos.setCorreo("ana@example.com");
        datos.setCodigo("demostracion");
        datos.setNuevaContrasena("NuevaClave123");
        servicio.restablecerContrasena(datos);
        assertTrue(new BCryptPasswordEncoder().matches("NuevaClave123", productor.getContrasena()));
        verify(usuarios).save(productor);
        assertThrows(IllegalArgumentException.class, () -> servicio.restablecerContrasena(datos));
    }

    @Test
    @SuppressWarnings("unchecked")
    void codigoVencidoNoCambiaContrasena() {
        RecuperacionService servicio = recuperacion();
        Map<String, RecuperacionContrasena> codigos = (Map<String, RecuperacionContrasena>)
                ReflectionTestUtils.getField(servicio, "codigos");
        codigos.put("ana@example.com", new RecuperacionContrasena("vencido", LocalDateTime.now().minusMinutes(1)));
        RestablecerContrasenaDTO datos = new RestablecerContrasenaDTO();
        datos.setCorreo("ana@example.com");
        datos.setCodigo("vencido");
        datos.setNuevaContrasena("NuevaClave123");
        assertThrows(IllegalArgumentException.class, () -> servicio.restablecerContrasena(datos));
        verify(usuarios, never()).save(any());
    }

    private RegistroUsuarioDTO registro() {
        RegistroUsuarioDTO datos = new RegistroUsuarioDTO();
        datos.setNombre("Ana");
        datos.setCorreo(" ANA@EXAMPLE.COM ");
        datos.setContrasena("ClaveValida123");
        datos.setRol(Rol.PRODUCTOR);
        return datos;
    }

    private RecuperacionService recuperacion() {
        RecuperacionService servicio = new RecuperacionService(usuarios);
        ReflectionTestUtils.setField(servicio, "recuperacionSimulada", true);
        return servicio;
    }
}
