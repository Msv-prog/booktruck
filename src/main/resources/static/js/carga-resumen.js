// Formulario de carga: no deja escoger fechas pasadas y muestra un resumen para confirmar antes de enviar.
document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('form-carga');
    var overlay = document.getElementById('resumen-carga-overlay');
    if (!form || !overlay) {
        return;
    }

    // Fecha y hora mínima: ahora (hora local), en el formato de datetime-local
    var fecha = document.getElementById('fechaRecoleccion');
    var ahora = new Date();
    ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
    fecha.min = ahora.toISOString().slice(0, 16);

    var lista = document.getElementById('resumen-carga-lista');
    var btnVolver = document.getElementById('resumen-carga-volver');
    var btnConfirmar = document.getElementById('resumen-carga-confirmar');
    var dinero = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
    var confirmado = false;

    function valor(id) {
        var campo = document.getElementById(id);
        return campo ? campo.value.trim() : '';
    }

    function fila(titulo, texto) {
        var dt = document.createElement('dt');
        dt.textContent = titulo;
        var dd = document.createElement('dd');
        dd.textContent = texto || '-';
        lista.appendChild(dt);
        lista.appendChild(dd);
    }

    form.addEventListener('submit', function (event) {
        if (confirmado) {
            return;
        }
        event.preventDefault();
        if (!form.reportValidity()) {
            volver();
            return;
        }
        var carroceria = document.getElementById('carroceriaRequerida');
        var cuando = fecha.value ? new Date(fecha.value) : null;

        lista.innerHTML = '';
        fila('Ruta', valor('origen') + ' → ' + valor('destino'));
        fila('Recoger en', valor('direccionOrigen'));
        fila('Entregar en', valor('direccionDestino'));
        fila('Mercancía', valor('tipoMercancia') + ' · ' + valor('peso') + ' ton');
        fila('Carrocería', carroceria ? carroceria.options[carroceria.selectedIndex].text : 'Cualquiera');
        fila('Precio ofrecido', valor('precioOfrecido') ? dinero.format(Number(valor('precioOfrecido'))) : '');
        fila('Recolección', cuando ? cuando.toLocaleString('es-CO', { dateStyle: 'long', timeStyle: 'short' }) : '');

        overlay.classList.add('is-open');
        btnConfirmar.focus();
    });

    // form-submit-guard.js ya puso el botón en "Guardando..."; al volver a editar se restaura
    function volver() {
        overlay.classList.remove('is-open');
        form.querySelectorAll('button[type="submit"]').forEach(function (boton) {
            boton.disabled = false;
            if (boton.dataset.textoOriginal) {
                boton.textContent = boton.dataset.textoOriginal;
            }
        });
    }

    btnVolver.addEventListener('click', volver);
    overlay.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            volver();
        }
    });
    btnConfirmar.addEventListener('click', function () {
        confirmado = true;
        overlay.classList.remove('is-open');
        if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
        } else {
            form.submit();
        }
    });
});
