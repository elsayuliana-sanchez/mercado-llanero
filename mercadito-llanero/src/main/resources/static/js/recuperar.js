const { api, mensaje, enviar, validarClave } = Mercadito;
const solicitar = document.getElementById('formSolicitar');
const restablecer = document.getElementById('formRestablecer');
const aviso = document.getElementById('mensajeRecuperacion');
solicitar.addEventListener('submit', evento => {
    evento.preventDefault();
    enviar(solicitar, aviso, async () => {
        const correo = solicitar.elements.correo.value.trim();
        await api('/api/usuarios/recuperar-contrasena', {
            method: 'POST', body: JSON.stringify({ correo })
        });
        restablecer.elements.correo.value = correo;
        restablecer.hidden = false;
        mensaje(aviso, 'Solicitud procesada. En esta demostración el código se consulta en la consola del servidor y vence en 10 minutos.', true);
        restablecer.elements.codigo.focus();
    });
});
document.getElementById('yaTengoCodigo').addEventListener('click', () => {
    restablecer.hidden = false;
    restablecer.elements.correo.value = solicitar.elements.correo.value.trim();
    restablecer.elements.correo.focus();
});
restablecer.addEventListener('submit', evento => {
    evento.preventDefault();
    enviar(restablecer, aviso, async () => {
        validarClave(restablecer.elements.nuevaContrasena.value, restablecer.elements.confirmar.value);
        const datos = {
            correo: restablecer.elements.correo.value.trim(),
            codigo: restablecer.elements.codigo.value.trim(),
            nuevaContrasena: restablecer.elements.nuevaContrasena.value
        };
        await api('/api/usuarios/restablecer-contrasena', { method: 'POST', body: JSON.stringify(datos) });
        restablecer.reset();
        restablecer.hidden = true;
        solicitar.reset();
        mensaje(aviso, 'Tu contraseña se actualizó. Ya puedes iniciar sesión con la nueva contraseña.', true);
        document.getElementById('irLogin').hidden = false;
        document.getElementById('irLogin').focus();
    });
});
