// Los mensajes de éxito de las páginas internas salen como avisos flotantes que se cierran solos.
// Los errores y advertencias se quedan en la página para que el usuario los lea con calma.
document.addEventListener('DOMContentLoaded', function () {
    var exitos = document.querySelectorAll('.app-content > .alert-success');
    if (!exitos.length) {
        return;
    }

    var contenedor = document.createElement('div');
    contenedor.className = 'toast-contenedor';
    contenedor.setAttribute('role', 'status');
    contenedor.setAttribute('aria-live', 'polite');
    document.body.appendChild(contenedor);

    exitos.forEach(function (alerta) {
        alerta.classList.add('toast');
        contenedor.appendChild(alerta);

        var temporizador = null;
        function programarCierre() {
            temporizador = setTimeout(function () {
                alerta.classList.add('is-closing');
                setTimeout(function () { alerta.remove(); }, 300);
            }, 5000);
        }
        // Si el usuario pasa el mouse por encima, el aviso espera
        alerta.addEventListener('mouseenter', function () { clearTimeout(temporizador); });
        alerta.addEventListener('mouseleave', programarCierre);
        programarCierre();
    });
});
