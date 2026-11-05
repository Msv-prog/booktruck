document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('select[data-lista]').forEach(function (select) {
        var lista = window[select.dataset.lista] || [];
        var targetId = select.dataset.target;
        var hidden = document.getElementById(targetId);
        var otroInput = document.getElementById(targetId + 'Otro');
        if (!hidden) {
            return;
        }

        var valorActual = hidden.value;

        var opcionVacia = document.createElement('option');
        opcionVacia.value = '';
        opcionVacia.textContent = 'Selecciona una opción';
        select.appendChild(opcionVacia);

        var coincide = false;
        lista.forEach(function (item) {
            var opcion = document.createElement('option');
            opcion.value = item;
            opcion.textContent = item;
            if (item === valorActual) {
                opcion.selected = true;
                coincide = true;
            }
            select.appendChild(opcion);
        });

        if (valorActual && !coincide) {
            select.value = 'Otro';
            if (otroInput) {
                otroInput.value = valorActual;
            }
        }

        function sync() {
            if (select.value === 'Otro' && otroInput) {
                otroInput.hidden = false;
                hidden.value = otroInput.value;
            } else {
                if (otroInput) {
                    otroInput.hidden = true;
                }
                hidden.value = select.value;
            }
        }

        select.addEventListener('change', sync);
        if (otroInput) {
            otroInput.addEventListener('input', function () { hidden.value = otroInput.value; });
        }
        sync();
    });
});
