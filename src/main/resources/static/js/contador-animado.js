document.addEventListener('DOMContentLoaded', function () {
    var DURACION_MS = 700;

    document.querySelectorAll('.stat-value').forEach(function (elemento) {
        var textoFinal = elemento.textContent.trim();
        // Solo se animan números simples (ej. "12" o "20.98"); montos como "$1.500.000" o "-" se dejan tal cual
        if (!/^\d+(\.\d+)?$/.test(textoFinal)) {
            return;
        }
        var destino = parseFloat(textoFinal);
        var decimales = textoFinal.indexOf('.') >= 0 ? textoFinal.split('.')[1].length : 0;

        var inicio = null;

        function paso(marcaTiempo) {
            if (!inicio) {
                inicio = marcaTiempo;
            }
            var progreso = Math.min((marcaTiempo - inicio) / DURACION_MS, 1);
            var progresoSuave = 1 - Math.pow(1 - progreso, 3);
            elemento.textContent = decimales > 0
                ? (progresoSuave * destino).toFixed(decimales)
                : Math.floor(progresoSuave * destino);

            if (progreso < 1) {
                requestAnimationFrame(paso);
            } else {
                elemento.textContent = textoFinal;
            }
        }

        requestAnimationFrame(paso);
    });
});
