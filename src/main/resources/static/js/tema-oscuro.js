var CLAVE_TEMA = 'booktruck-tema';

function aplicarTema(tema) {
    if (tema === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
    } else {
        document.documentElement.removeAttribute('data-theme');
    }
}

aplicarTema(localStorage.getItem(CLAVE_TEMA));

document.addEventListener('DOMContentLoaded', function () {
    var boton = document.getElementById('theme-toggle-btn');
    if (!boton) {
        return;
    }
    var textoEl = boton.querySelector('.theme-toggle-texto');

    function actualizarBoton() {
        var esOscuro = document.documentElement.getAttribute('data-theme') === 'dark';
        boton.setAttribute('aria-pressed', String(esOscuro));
        boton.setAttribute('aria-label', esOscuro ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
        if (textoEl) {
            textoEl.textContent = esOscuro ? 'Modo claro' : 'Modo oscuro';
        }
    }

    actualizarBoton();

    boton.addEventListener('click', function () {
        var esOscuro = document.documentElement.getAttribute('data-theme') === 'dark';
        var nuevoTema = esOscuro ? 'light' : 'dark';
        aplicarTema(nuevoTema);
        localStorage.setItem(CLAVE_TEMA, nuevoTema);
        actualizarBoton();
    });
});
