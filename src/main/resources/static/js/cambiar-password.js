document.addEventListener('DOMContentLoaded', function () {
    var nueva = document.getElementById('passwordNueva');
    var confirmar = document.getElementById('confirmarPasswordNueva');
    if (!nueva || !confirmar) {
        return;
    }

    function validar() {
        if (confirmar.value && nueva.value !== confirmar.value) {
            confirmar.setCustomValidity('Las contraseñas no coinciden.');
        } else {
            confirmar.setCustomValidity('');
        }
    }

    nueva.addEventListener('input', validar);
    confirmar.addEventListener('input', validar);
});
