/* Servicios compartidos. La identidad se obtiene de la sesión del servidor. */
window.Mercadito = (() => {
    class ErrorApi extends Error {
        constructor(mensaje, estado) {
            super(mensaje);
            this.estado = estado;
        }
    }

    async function api(ruta, opciones = {}) {
        let respuesta;
        try {
            respuesta = await fetch(ruta, {
                credentials: 'same-origin', cache: 'no-store',
                ...opciones,
                headers: { 'Content-Type': 'application/json', ...opciones.headers },
                signal: opciones.signal || AbortSignal.timeout(15000)
            });
        } catch (error) {
            if (error.name === 'AbortError') throw error;
            throw new ErrorApi('No pudimos conectar. Revisa tu conexión e inténtalo de nuevo.', 0);
        }
        const texto = await respuesta.text();
        let datos;
        try { datos = texto ? JSON.parse(texto) : null; } catch { datos = null; }
        if (!respuesta.ok) {
            if (respuesta.status === 401 && document.body.dataset.privada) {
                location.replace('/login.html?motivo=sesion');
            }
            throw new ErrorApi(datos?.mensaje || 'No se pudo completar la operación. Inténtalo de nuevo.', respuesta.status);
        }
        return datos;
    }

    function mensaje(destino, texto, exito = false) {
        destino.textContent = texto;
        destino.className = 'mensaje-registro ' + (exito ? 'mensaje-exito' : 'mensaje-error');
        destino.setAttribute('role', exito ? 'status' : 'alert');
    }

    async function enviar(formulario, destino, accion) {
        if (formulario.dataset.enviando || !formulario.reportValidity()) return;
        const boton = formulario.querySelector('[type="submit"]');
        const etiqueta = boton.textContent;
        formulario.dataset.enviando = 'true';
        formulario.setAttribute('aria-busy', 'true');
        boton.disabled = true;
        boton.textContent = 'Procesando…';
        destino.textContent = '';
        try { await accion(); }
        catch (error) { mensaje(destino, error.message); }
        finally {
            delete formulario.dataset.enviando;
            formulario.removeAttribute('aria-busy');
            boton.disabled = false;
            boton.textContent = etiqueta;
        }
    }

    function validarClave(clave, confirmacion) {
        if (!clave.trim() || clave.length < 8) throw new Error('Usa una contraseña de al menos 8 caracteres.');
        if (new TextEncoder().encode(clave).length > 72) throw new Error('La contraseña es demasiado larga. Usa hasta 72 bytes, menos si contiene tildes o símbolos.');
        if (clave !== confirmacion) throw new Error('Las contraseñas no coinciden.');
    }

    function elemento(tipo, clase, texto) {
        const nodo = document.createElement(tipo);
        if (clase) nodo.className = clase;
        if (texto !== undefined) nodo.textContent = texto;
        return nodo;
    }

    async function sesion() {
        try { return await api('/api/usuarios/sesion'); }
        catch (error) { if (error.estado === 401) return null; throw error; }
    }

    async function cabecera() {
        const acciones = document.querySelector('.acciones-usuario');
        if (!acciones) return;
        try {
            const usuario = await sesion();
            if (!usuario) return;
            acciones.replaceChildren();
            const panel = elemento('a', 'btn-ingresar', 'Mi cuenta');
            panel.href = '/panel.html';
            const salir = elemento('button', 'btn-registro', 'Cerrar sesión');
            salir.type = 'button';
            salir.addEventListener('click', async () => {
                salir.disabled = true;
                try { await api('/api/usuarios/logout', { method: 'POST' }); location.replace('/login.html?motivo=salida'); }
                catch (error) {
                    salir.disabled = false;
                    let aviso = document.getElementById('errorCabecera');
                    if (!aviso) {
                        aviso = elemento('p', 'mensaje-registro');
                        aviso.id = 'errorCabecera';
                        document.querySelector('header').append(aviso);
                    }
                    mensaje(aviso, error.message);
                }
            });
            acciones.append(panel, salir);
        } catch { /* Los controles públicos siguen disponibles al fallar la conexión. */ }
    }

    document.querySelectorAll('[data-ver-clave]').forEach(boton => {
        boton.addEventListener('click', () => {
            const campo = document.getElementById(boton.dataset.verClave);
            campo.type = campo.type === 'password' ? 'text' : 'password';
            boton.textContent = campo.type === 'password' ? 'Ver' : 'Ocultar';
            boton.setAttribute('aria-label', campo.type === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña');
            boton.setAttribute('aria-pressed', String(campo.type === 'text'));
        });
    });
    cabecera();
    return { api, mensaje, enviar, validarClave, elemento, sesion,
        precio: valor => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 }).format(valor) };
})();
