const { api, mensaje, enviar, validarClave } = Mercadito;
const login = document.getElementById('formLogin');
const registro = document.getElementById('formRegistro');

if (login) {
    const motivo = new URLSearchParams(location.search).get('motivo');
    const aviso = document.getElementById('mensajeLogin');
    if (motivo === 'sesion') mensaje(aviso, 'Inicia sesión para acceder a tu cuenta.');
    if (motivo === 'salida') mensaje(aviso, 'Has cerrado sesión correctamente.', true);
    login.addEventListener('submit', evento => {
        evento.preventDefault();
        enviar(login, aviso, async () => {
            const datos = { correo: login.elements.correo.value.trim(), contrasena: login.elements.contrasena.value };
            await api('/api/usuarios/login', { method: 'POST', body: JSON.stringify(datos) });
            login.elements.contrasena.value = '';
            location.replace('/panel.html');
        });
    });
}

if (registro) {
    const aviso = document.getElementById('mensajeRegistro');
    registro.addEventListener('submit', evento => {
        evento.preventDefault();
        enviar(registro, aviso, async () => {
            const datos = {
                nombre: registro.elements.nombre.value.trim(), correo: registro.elements.correo.value.trim(),
                rol: registro.elements.rol.value, contrasena: registro.elements.contrasena.value
            };
            if (!datos.nombre) throw new Error('Escribe tu nombre.');
            validarClave(datos.contrasena, registro.elements.confirmarContrasena.value);
            await api('/api/usuarios/registro', { method: 'POST', body: JSON.stringify(datos) });
            registro.reset();
            mensaje(aviso, 'Tu cuenta está creada. Continúa para iniciar sesión.', true);
            document.getElementById('registroExitoso').hidden = false;
            document.getElementById('registroExitoso').focus();
        });
    });
}
