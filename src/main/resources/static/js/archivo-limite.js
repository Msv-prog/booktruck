// Valida el tamaño de los archivos antes de subirlos: <input type="file" data-max-mb="5">.
// El servidor vuelve a validar; esto solo evita que el usuario espere a subir un archivo que será rechazado.
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[type="file"][data-max-mb]').forEach(function (input) {
        var maxBytes = parseFloat(input.getAttribute('data-max-mb')) * 1024 * 1024;
        var mensaje = document.createElement('small');
        mensaje.className = 'field-error';
        input.insertAdjacentElement('afterend', mensaje);

        function archivoValido() {
            var archivo = input.files && input.files[0];
            if (archivo && archivo.size > maxBytes) {
                var tamanoMb = (archivo.size / (1024 * 1024)).toFixed(1);
                mensaje.textContent = 'El archivo pesa ' + tamanoMb + ' MB y no puede superar los '
                    + input.getAttribute('data-max-mb') + ' MB.';
                input.classList.add('is-invalid');
                return false;
            }
            mensaje.textContent = '';
            input.classList.remove('is-invalid');
            return true;
        }

        // Se vacía el campo: como es "required", el navegador ya no deja enviar el formulario
        input.addEventListener('change', function () {
            if (!archivoValido()) {
                input.value = '';
            }
        });
    });
});
