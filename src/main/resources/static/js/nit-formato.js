document.addEventListener('DOMContentLoaded', function () {
    var input = document.getElementById('nit');
    if (!input) {
        return;
    }

    function formatear(valorCrudo) {
        var digitos = valorCrudo.replace(/\D/g, '').slice(0, 10);
        if (digitos.length <= 1) {
            return digitos;
        }
        var base = digitos.slice(0, -1);
        var verificador = digitos.slice(-1);
        var baseFormateada = base.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
        return baseFormateada + '-' + verificador;
    }

    input.addEventListener('input', function () {
        var posicionCursorAlFinal = input.selectionStart === input.value.length;
        input.value = formatear(input.value);
        if (posicionCursorAlFinal) {
            input.setSelectionRange(input.value.length, input.value.length);
        }

        var digitos = input.value.replace(/\D/g, '');
        if (digitos.length > 0 && digitos.length < 9) {
            input.classList.add('is-invalid');
            var siguiente = input.nextElementSibling;
            if (!siguiente || !siguiente.classList.contains('field-error')) {
                siguiente = document.createElement('small');
                siguiente.className = 'field-error';
                input.insertAdjacentElement('afterend', siguiente);
            }
            siguiente.textContent = 'El NIT debe tener al menos 9 dígitos (incluyendo el de verificación).';
        } else {
            input.classList.remove('is-invalid');
            var mensajeExistente = input.nextElementSibling;
            if (mensajeExistente && mensajeExistente.classList.contains('field-error')) {
                mensajeExistente.textContent = '';
            }
        }
    });
});
