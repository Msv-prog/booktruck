// Abre y cierra el panel de la campanita; se cierra al hacer clic afuera o con Escape.
document.addEventListener('DOMContentLoaded', function () {
    var campana = document.querySelector('.campana');
    if (!campana) {
        return;
    }
    var boton = campana.querySelector('.campana-boton');
    var panel = campana.querySelector('.campana-panel');

    function abrir(abierto) {
        panel.hidden = !abierto;
        boton.setAttribute('aria-expanded', abierto ? 'true' : 'false');
    }

    boton.addEventListener('click', function (event) {
        event.stopPropagation();
        abrir(panel.hidden);
    });
    document.addEventListener('click', function (event) {
        if (!panel.hidden && !campana.contains(event.target)) {
            abrir(false);
        }
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !panel.hidden) {
            abrir(false);
            boton.focus();
        }
    });
});
