const { api, elemento, precio } = Mercadito;
const lista = document.getElementById('listaProductos');
const aviso = document.getElementById('mensajeProductos');
const busqueda = document.getElementById('buscarProducto');
const filtro = document.getElementById('filtroCategoria');
let consultaActual = 0;

function tarjeta(producto) {
    const tarjeta = elemento('article', 'tarjeta-producto');
    const imagen = elemento('div', 'imagen-producto imagen-producto-icono', '🌱');
    imagen.setAttribute('aria-hidden', 'true');
    const info = elemento('div', 'info-producto');
    info.append(elemento('span', 'categoria-producto', producto.categoria?.nombre || 'Producto local'),
        elemento('h3', '', producto.nombre), elemento('p', 'descripcion-producto', producto.descripcion || 'Producto de nuestra región'),
        elemento('p', 'precio-producto', precio(producto.precio)),
        elemento('p', 'cantidad-producto', 'Cantidad disponible: ' + producto.cantidad),
        elemento('span', producto.disponible && producto.cantidad > 0 ? 'estado-disponible' : 'estado-agotado',
            producto.disponible && producto.cantidad > 0 ? 'Disponible' : 'Agotado'));
    const acciones = elemento('div', 'acciones-form');
    const detalle = elemento('button', 'btn-producto', 'Ver detalle');
    detalle.type = 'button';
    detalle.setAttribute('aria-label', 'Ver detalle de ' + producto.nombre);
    detalle.addEventListener('click', () => mostrarDetalle(producto.id));
    acciones.append(detalle);
    info.append(acciones);
    tarjeta.append(imagen, info);
    return tarjeta;
}

async function mostrarDetalle(id) {
    const dialogo = document.getElementById('detalleProducto');
    const contenido = document.getElementById('contenidoDetalle');
    contenido.replaceChildren(elemento('h2', '', 'Cargando producto…'));
    contenido.firstChild.id = 'detalleNombre';
    dialogo.showModal();
    try {
        const producto = await api('/api/productos/' + id);
        if (!dialogo.open) return;
        const titulo = elemento('h2', '', producto.nombre);
        titulo.id = 'detalleNombre';
        contenido.replaceChildren(titulo, elemento('p', 'categoria-producto', producto.categoria?.nombre || 'Producto local'),
            elemento('p', '', producto.descripcion || 'Sin descripción adicional.'),
            elemento('p', 'precio-producto', precio(producto.precio)),
            elemento('p', '', 'Cantidad disponible: ' + producto.cantidad),
            elemento('p', '', producto.disponible && producto.cantidad > 0 ? 'Disponible' : 'Agotado'));
    } catch (error) {
        contenido.replaceChildren(elemento('h2', '', 'No pudimos abrir el producto'), elemento('p', '', error.message));
        contenido.firstChild.id = 'detalleNombre';
    }
}

async function cargar() {
    const numeroConsulta = ++consultaActual;
    lista.setAttribute('aria-busy', 'true');
    aviso.textContent = 'Consultando productos…';
    const nombre = busqueda.value.trim();
    const categoria = filtro.value;
    document.querySelectorAll('[data-categoria]').forEach(boton => {
        boton.setAttribute('aria-pressed', String(boton.dataset.categoria === categoria));
    });
    try {
        const ruta = nombre ? '/api/productos/buscar?nombre=' + encodeURIComponent(nombre)
            : categoria ? '/api/productos/categoria/' + categoria : '/api/productos';
        let productos = await api(ruta);
        if (numeroConsulta !== consultaActual) return;
        if (categoria) productos = productos.filter(producto => String(producto.categoria?.id) === categoria);
        lista.replaceChildren(...productos.map(tarjeta));
        aviso.textContent = productos.length ? productos.length + ' productos encontrados.' : 'No encontramos productos para estos filtros.';
        if (!productos.length) lista.append(elemento('p', 'estado-vacio', 'Prueba otra categoría o publica el primer producto desde tu cuenta de productor.'));
    } catch (error) {
        if (numeroConsulta !== consultaActual) return;
        lista.replaceChildren();
        aviso.textContent = error.message + ' Puedes volver a buscar para reintentar.';
    } finally {
        if (numeroConsulta === consultaActual) lista.setAttribute('aria-busy', 'false');
    }
}

async function cargarCategorias() {
    const contenedor = document.getElementById('categoriasCatalogo');
    try {
        const categorias = await api('/api/categorias');
        const todas = { id: '', nombre: 'Todas las categorías' };
        [todas, ...categorias.filter(categoria => categoria.activa)].forEach(categoria => {
            const boton = elemento('button', '', categoria.nombre);
            boton.type = 'button';
            boton.dataset.categoria = String(categoria.id);
            boton.setAttribute('aria-pressed', String(categoria.id === ''));
            boton.addEventListener('click', () => {
                filtro.value = String(categoria.id);
                cargar();
                document.getElementById('productos').scrollIntoView({ behavior: 'smooth' });
            });
            contenedor.append(boton);
            if (categoria.id !== '') {
                const opcion = elemento('option', '', categoria.nombre);
                opcion.value = categoria.id;
                filtro.append(opcion);
            }
        });
    } catch {
        contenedor.append(elemento('p', '', 'Las categorías no se pudieron cargar. Puedes buscar por nombre.'));
    }
}

document.getElementById('formBuscar').addEventListener('submit', evento => { evento.preventDefault(); cargar(); });
filtro.addEventListener('change', cargar);
document.getElementById('limpiarFiltros').addEventListener('click', () => { busqueda.value = ''; filtro.value = ''; cargar(); });
document.getElementById('cerrarDetalle').addEventListener('click', () => document.getElementById('detalleProducto').close());
cargarCategorias();
cargar();
