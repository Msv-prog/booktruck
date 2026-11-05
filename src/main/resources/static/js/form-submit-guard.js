function aplicarEstadoGuardando(form) {
    var esLogout = form.classList.contains('logout-form');
    form.querySelectorAll('button[type="submit"]').forEach(function (boton) {
        boton.disabled = true;
        if (!esLogout) {
            boton.dataset.textoOriginal = boton.textContent;
            boton.innerHTML = '<span class="btn-spinner" aria-hidden="true"></span>Guardando...';
        }
    });
}

document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!(form instanceof HTMLFormElement) || form.method.toLowerCase() !== 'post') {
        return;
    }

    var fila = form.closest('tr');
    var primeraCelda = fila ? fila.querySelector('td') : null;
    if (primeraCelda) {
        sessionStorage.setItem('booktruck-fila-marcada', primeraCelda.textContent.trim());
    }

    if (form.classList.contains('js-confirm')) {
        return;
    }
    aplicarEstadoGuardando(form);
}, true);
