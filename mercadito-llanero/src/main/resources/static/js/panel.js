const { api, mensaje, enviar, elemento, precio, sesion } = Mercadito;
const estado = document.getElementById('cargandoPanel');
const aviso = document.getElementById('mensajePanel');
const lista = document.getElementById('misProductos');
const form = document.getElementById('formProducto');
const categoriaSelect = document.getElementById('productoCategoria');
let idEditar = null;
let idEliminar = null;

document.querySelectorAll('[data-cerrar]').forEach(boton => {
    boton.addEventListener('click', () => document.getElementById(boton.dataset.cerrar).close());
});

async function categorias() {
    const seleccion = categoriaSelect.value;
    const datos = await api('/api/categorias');
    const inicial = elemento('option', '', 'Selecciona una categoría');
    inicial.value = '';
    categoriaSelect.replaceChildren(inicial);
    datos.forEach(categoria => {
        const opcion = elemento('option', '', categoria.nombre);
        opcion.value = categoria.id;
        categoriaSelect.append(opcion);
    });
    categoriaSelect.value = seleccion;
}

async function cargarProductos() {
    lista.setAttribute('aria-busy', 'true');
    try {
        const productos = await api('/api/productos/mis-productos');
        lista.replaceChildren();
        if (!productos.length) lista.append(elemento('p', 'estado-vacio', 'Aún no tienes publicaciones. Crea una categoría si la necesitas y publica tu primer producto.'));
        productos.forEach(producto => {
            const tarjeta = elemento('article', 'tarjeta-producto');
            const info = elemento('div', 'info-producto');
            info.append(elemento('span', 'categoria-producto', producto.categoria?.nombre || 'Sin categoría'),
                elemento('h3', '', producto.nombre), elemento('p', 'precio-producto', precio(producto.precio)),
                elemento('p', '', 'Cantidad: ' + producto.cantidad),
                elemento('p', '', producto.disponible && producto.cantidad > 0 ? 'Disponible' : 'No disponible'));
            const acciones = elemento('div', 'acciones-form');
            const editar = elemento('button', 'boton-secundario', 'Editar');
            editar.type = 'button';
            editar.setAttribute('aria-label', 'Editar ' + producto.nombre);
            editar.addEventListener('click', () => abrirProducto(producto));
            const eliminar = elemento('button', 'boton-secundario boton-peligro', 'Eliminar');
            eliminar.type = 'button';
            eliminar.setAttribute('aria-label', 'Eliminar ' + producto.nombre);
            eliminar.addEventListener('click', () => {
                idEliminar = producto.id;
                document.getElementById('descripcionEliminar').textContent = '¿Quieres eliminar «' + producto.nombre + '»? Esta publicación se retirará del catálogo.';
                document.getElementById('mensajeEliminar').textContent = '';
                document.getElementById('dialogoEliminar').showModal();
            });
            acciones.append(editar, eliminar);
            info.append(acciones);
            tarjeta.append(info);
            lista.append(tarjeta);
        });
        return true;
    } catch (error) {
        mensaje(aviso, error.message + ' Usa Actualizar listado para reintentar.');
        return false;
    } finally { lista.setAttribute('aria-busy', 'false'); }
}

async function abrirProducto(producto = null) {
    try { await categorias(); }
    catch (error) { mensaje(aviso, error.message); return; }
    idEditar = producto?.id || null;
    form.reset();
    document.getElementById('mensajeProducto').textContent = '';
    document.getElementById('tituloProducto').textContent = producto ? 'Editar producto' : 'Publicar producto';
    if (producto) {
        form.elements.productoNombre.value = producto.nombre;
        form.elements.productoDescripcion.value = producto.descripcion || '';
        form.elements.productoPrecio.value = producto.precio;
        form.elements.productoCantidad.value = producto.cantidad;
        form.elements.productoCategoria.value = producto.categoria?.id || '';
        form.elements.productoDisponible.checked = producto.disponible;
    }
    document.getElementById('dialogoProducto').showModal();
}

form.addEventListener('submit', evento => {
    evento.preventDefault();
    enviar(form, document.getElementById('mensajeProducto'), async () => {
        const datos = {
            nombre: form.elements.productoNombre.value.trim(), descripcion: form.elements.productoDescripcion.value.trim(),
            precio: form.elements.productoPrecio.value, cantidad: Number(form.elements.productoCantidad.value),
            categoria: { id: Number(form.elements.productoCategoria.value) }, disponible: form.elements.productoDisponible.checked
        };
        if (!datos.nombre) throw new Error('Escribe el nombre del producto.');
        await api('/api/productos' + (idEditar ? '/' + idEditar : ''), {
            method: idEditar ? 'PUT' : 'POST', body: JSON.stringify(datos)
        });
        document.getElementById('dialogoProducto').close();
        if (await cargarProductos()) mensaje(aviso, 'Producto guardado correctamente.', true);
    });
});

document.getElementById('formCategoria').addEventListener('submit', evento => {
    evento.preventDefault();
    const formulario = evento.currentTarget;
    enviar(formulario, document.getElementById('mensajeCategoria'), async () => {
        const nombre = document.getElementById('categoriaNombre').value.trim();
        if (!nombre) throw new Error('Escribe el nombre de la categoría.');
        await api('/api/categorias', { method: 'POST', body: JSON.stringify({
            nombre, descripcion: document.getElementById('categoriaDescripcion').value.trim()
        }) });
        formulario.reset();
        document.getElementById('dialogoCategoria').close();
        mensaje(aviso, 'Categoría creada. Ya puedes seleccionarla al publicar un producto.', true);
    });
});

document.getElementById('formEliminar').addEventListener('submit', evento => {
    evento.preventDefault();
    enviar(evento.currentTarget, document.getElementById('mensajeEliminar'), async () => {
        await api('/api/productos/' + idEliminar, { method: 'DELETE' });
        document.getElementById('dialogoEliminar').close();
        if (await cargarProductos()) mensaje(aviso, 'Producto eliminado correctamente.', true);
    });
});

document.getElementById('nuevoProducto').addEventListener('click', () => abrirProducto());
document.getElementById('nuevaCategoria').addEventListener('click', () => {
    document.getElementById('mensajeCategoria').textContent = '';
    document.getElementById('dialogoCategoria').showModal();
});
document.getElementById('recargarProductos').addEventListener('click', async () => {
    aviso.textContent = '';
    await cargarProductos();
});

async function iniciar() {
    try {
        const usuario = await sesion();
        if (!usuario) { location.replace('/login.html?motivo=sesion'); return; }
        document.getElementById('perfilNombre').textContent = usuario.nombre;
        document.getElementById('perfilCorreo').textContent = usuario.correo;
        document.getElementById('perfilRol').textContent = usuario.rol === 'PRODUCTOR' ? 'Productor' : 'Comprador';
        document.getElementById('saludoUsuario').textContent = 'Hola, ' + usuario.nombre;
        document.getElementById('contenidoPanel').hidden = false;
        estado.hidden = true;
        if (usuario.rol === 'PRODUCTOR') {
            document.getElementById('panelProductor').hidden = false;
            await cargarProductos();
        } else document.getElementById('panelComprador').hidden = false;
    } catch (error) { mensaje(estado, error.message + ' Recarga esta página para reintentar.'); }
}
window.addEventListener('pageshow', evento => { if (evento.persisted) iniciar(); });
iniciar();
