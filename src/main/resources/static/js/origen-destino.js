document.addEventListener('DOMContentLoaded', function () {
    function initCascada(deptoId, ciudadId, hiddenId) {
        var deptoSelect = document.getElementById(deptoId);
        var ciudadSelect = document.getElementById(ciudadId);
        var hidden = document.getElementById(hiddenId);
        if (!deptoSelect || !ciudadSelect || !hidden || typeof DEPARTAMENTOS_CIUDADES === 'undefined') {
            return;
        }

        var deptoVacio = document.createElement('option');
        deptoVacio.value = '';
        deptoVacio.textContent = 'Departamento';
        deptoSelect.appendChild(deptoVacio);

        Object.keys(DEPARTAMENTOS_CIUDADES).sort().forEach(function (depto) {
            var opcion = document.createElement('option');
            opcion.value = depto;
            opcion.textContent = depto;
            deptoSelect.appendChild(opcion);
        });

        function poblarCiudades(depto, ciudadSeleccionada) {
            ciudadSelect.innerHTML = '';
            var vacio = document.createElement('option');
            vacio.value = '';
            vacio.textContent = 'Ciudad';
            ciudadSelect.appendChild(vacio);
            (DEPARTAMENTOS_CIUDADES[depto] || []).forEach(function (ciudad) {
                var opcion = document.createElement('option');
                opcion.value = ciudad;
                opcion.textContent = ciudad;
                if (ciudad === ciudadSeleccionada) {
                    opcion.selected = true;
                }
                ciudadSelect.appendChild(opcion);
            });
        }

        var valorActual = hidden.value;
        if (valorActual) {
            Object.keys(DEPARTAMENTOS_CIUDADES).forEach(function (depto) {
                if (DEPARTAMENTOS_CIUDADES[depto].indexOf(valorActual) !== -1) {
                    deptoSelect.value = depto;
                }
            });
        }
        poblarCiudades(deptoSelect.value, valorActual);

        deptoSelect.addEventListener('change', function () {
            poblarCiudades(deptoSelect.value, null);
            hidden.value = ciudadSelect.value;
        });
        ciudadSelect.addEventListener('change', function () {
            hidden.value = ciudadSelect.value;
        });
    }

    initCascada('departamentoOrigen', 'ciudadOrigen', 'origen');
    initCascada('departamentoDestino', 'ciudadDestino', 'destino');
});
