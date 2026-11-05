document.addEventListener('DOMContentLoaded', function () {
    var enlaces = document.querySelectorAll('.app-topbar-menu a');
    var actual = window.location.pathname;
    var mejor = null;

    enlaces.forEach(function (enlace) {
        var ruta = new URL(enlace.href, window.location.origin).pathname;
        if (actual === ruta || actual.indexOf(ruta + '/') === 0) {
            if (mejor === null || ruta.length > mejor.length) {
                mejor = ruta;
            }
        }
    });

    enlaces.forEach(function (enlace) {
        var ruta = new URL(enlace.href, window.location.origin).pathname;
        enlace.classList.toggle('is-active', ruta === mejor);
    });
});
