// Modal genérico de "Ver detalles": cada botón .js-ver-detalle apunta (data-detalle) al id de un
// <template> renderizado en el servidor con el contenido de esa fila.
document.addEventListener('DOMContentLoaded', function () {
    var overlay = document.getElementById('detalle-modal-overlay');
    if (!overlay) {
        return;
    }
    var titulo = document.getElementById('detalle-modal-titulo');
    var cuerpo = document.getElementById('detalle-modal-cuerpo');
    var btnCerrar = document.getElementById('detalle-modal-cerrar');
    var elementoActivadorPrevio = null;

    function abrirModal(boton) {
        var plantilla = document.getElementById(boton.getAttribute('data-detalle'));
        if (!plantilla) {
            return;
        }
        titulo.textContent = boton.getAttribute('data-detalle-titulo') || 'Detalles';
        cuerpo.innerHTML = '';
        cuerpo.appendChild(plantilla.content.cloneNode(true));
        elementoActivadorPrevio = boton;
        overlay.classList.add('is-open');
        // El overlay pasa de visibility:hidden a visible con transición; se enfoca en el siguiente tick
        setTimeout(function () { btnCerrar.focus(); }, 50);
    }

    function cerrarModal() {
        if (!overlay.classList.contains('is-open')) {
            return;
        }
        overlay.classList.remove('is-open');
        if (elementoActivadorPrevio) {
            elementoActivadorPrevio.focus();
        }
        elementoActivadorPrevio = null;
    }

    document.querySelectorAll('.js-ver-detalle').forEach(function (boton) {
        boton.addEventListener('click', function () {
            abrirModal(boton);
        });
    });

    btnCerrar.addEventListener('click', cerrarModal);

    overlay.addEventListener('click', function (event) {
        if (event.target === overlay) {
            cerrarModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            cerrarModal();
        }
    });

    overlay.addEventListener('keydown', function (event) {
        if (event.key !== 'Tab') {
            return;
        }
        var enfocables = overlay.querySelectorAll('a[href], button, select, input, [tabindex]:not([tabindex="-1"])');
        if (!enfocables.length) {
            return;
        }
        var primero = enfocables[0];
        var ultimo = enfocables[enfocables.length - 1];
        if (event.shiftKey && document.activeElement === primero) {
            event.preventDefault();
            ultimo.focus();
        } else if (!event.shiftKey && document.activeElement === ultimo) {
            event.preventDefault();
            primero.focus();
        }
    });
});
