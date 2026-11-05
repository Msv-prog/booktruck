document.addEventListener('DOMContentLoaded', function () {
    var overlay = document.getElementById('confirm-modal-overlay');
    if (!overlay) {
        return;
    }
    var messageEl = document.getElementById('confirm-modal-message');
    var btnConfirm = document.getElementById('confirm-modal-accept');
    var btnCancel = document.getElementById('confirm-modal-cancel');
    var motivoGroup = document.getElementById('confirm-modal-motivo-group');
    var motivoSelect = document.getElementById('confirm-modal-motivo');
    var motivoError = document.getElementById('confirm-modal-motivo-error');
    var pendingForm = null;
    var elementoActivadorPrevio = null;

    function abrirModal() {
        elementoActivadorPrevio = document.activeElement;
        overlay.classList.add('is-open');
        btnCancel.focus();
    }

    function cerrarModal() {
        overlay.classList.remove('is-open');
        pendingForm = null;
        if (elementoActivadorPrevio && typeof elementoActivadorPrevio.focus === 'function') {
            elementoActivadorPrevio.focus();
        }
        elementoActivadorPrevio = null;
    }

    document.querySelectorAll('form.js-confirm').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            pendingForm = form;
            messageEl.textContent = form.getAttribute('data-confirm-message') || '¿Confirmas esta acción?';

            var requiereMotivo = form.hasAttribute('data-requiere-motivo');
            motivoGroup.hidden = !requiereMotivo;
            motivoSelect.value = '';
            motivoError.textContent = '';

            abrirModal();
        });
    });

    overlay.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            cerrarModal();
            return;
        }
        if (event.key !== 'Tab') {
            return;
        }
        var enfocables = overlay.querySelectorAll('button, select, input, [tabindex]:not([tabindex="-1"])');
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

    btnCancel.addEventListener('click', function () {
        cerrarModal();
    });

    btnConfirm.addEventListener('click', function () {
        if (pendingForm && pendingForm.hasAttribute('data-requiere-motivo')) {
            if (!motivoSelect.value) {
                motivoError.textContent = 'Debes seleccionar un motivo.';
                return;
            }
            var input = pendingForm.querySelector('input[name="motivo"]');
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'motivo';
                pendingForm.appendChild(input);
            }
            input.value = motivoSelect.value;
        }

        overlay.classList.remove('is-open');
        if (pendingForm) {
            iniciarBarraProgreso();
            aplicarEstadoGuardando(pendingForm);
            pendingForm.submit();
        }
    });
});
