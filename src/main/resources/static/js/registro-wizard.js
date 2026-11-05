(function () {
    var form = document.getElementById('form-registro');
    if (!form) {
        return;
    }

    var paneles = Array.prototype.slice.call(form.querySelectorAll('.wizard-panel'));
    var puntos = Array.prototype.slice.call(document.querySelectorAll('.wizard-step-dot'));

    function mostrarPaso(numero) {
        paneles.forEach(function (panel) {
            panel.hidden = panel.dataset.step !== String(numero);
        });
        puntos.forEach(function (punto) {
            punto.classList.toggle('is-active', punto.dataset.dot === String(numero));
        });
    }

    function validarPaso(panel) {
        var campos = panel.querySelectorAll('input');
        for (var i = 0; i < campos.length; i++) {
            if (!campos[i].checkValidity()) {
                campos[i].reportValidity();
                return false;
            }
        }

        var password = panel.querySelector('#password');
        var confirmar = panel.querySelector('#confirmarPassword');
        if (password && confirmar && password.value !== confirmar.value) {
            confirmar.setCustomValidity('Las contraseñas no coinciden.');
            confirmar.reportValidity();
            confirmar.addEventListener('input', function limpiar() {
                confirmar.setCustomValidity('');
                confirmar.removeEventListener('input', limpiar);
            });
            return false;
        }

        return true;
    }

    form.querySelectorAll('.wizard-next').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var panelActual = boton.closest('.wizard-panel');
            if (!validarPaso(panelActual)) {
                return;
            }
            mostrarPaso(parseInt(panelActual.dataset.step, 10) + 1);
        });
    });

    form.querySelectorAll('.wizard-back').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var panelActual = boton.closest('.wizard-panel');
            mostrarPaso(parseInt(panelActual.dataset.step, 10) - 1);
        });
    });

    // Si el servidor rechazó un envío anterior (ej. NIT duplicado), no sabemos
    // en qué paso está el campo con el error: se muestran todos los pasos para
    // que el usuario vea el mensaje y corrija sin perder lo ya diligenciado.
    var errorServidor = document.querySelector('.alert-error');
    if (errorServidor && errorServidor.textContent.trim()) {
        paneles.forEach(function (panel) { panel.hidden = false; });
        puntos.forEach(function (punto) { punto.classList.add('is-active'); });
    }
})();
