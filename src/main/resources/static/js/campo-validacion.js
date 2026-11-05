document.addEventListener('DOMContentLoaded', function () {
    var reglas = {
        telefono: { regex: /^[0-9]{7,10}$/, mensaje: 'Solo se permiten números (máximo 10 dígitos).' },
        cedula: { regex: /^[0-9]{6,10}$/, mensaje: 'La cédula solo debe contener números (máximo 10 dígitos).' },
        placa: { regex: /^[A-Za-z]{3}[0-9]{3}$/, mensaje: 'La placa debe tener el formato de 3 letras y 3 números (ej: ABC123).' },
        peso: { min: 0.01, mensaje: 'El peso debe ser mayor que cero.' },
        capacidadToneladas: { min: 0.01, mensaje: 'La capacidad debe ser mayor que cero.' },
        precioOfrecido: { min: 0, mensaje: 'El precio no puede ser negativo.' },
        monto: { min: 0, mensaje: 'El monto no puede ser negativo.' }
    };

    // Se expone para que páginas puntuales (como el registro de vehículo) puedan
    // ajustar una regla en caliente según otro campo del formulario (ej: el
    // formato de placa cambia según si el vehículo es sencillo o articulado).
    window.reglasValidacion = reglas;

    var DEMORA_MS = 600;
    var temporizadores = new WeakMap();

    function mostrarError(input, mensaje) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
        var siguiente = input.nextElementSibling;
        if (!siguiente || !siguiente.classList.contains('field-error')) {
            siguiente = document.createElement('small');
            siguiente.className = 'field-error';
            input.insertAdjacentElement('afterend', siguiente);
        }
        siguiente.textContent = mensaje;
    }

    function limpiarError(input) {
        input.classList.remove('is-invalid', 'is-valid');
        var siguiente = input.nextElementSibling;
        if (siguiente && siguiente.classList.contains('field-error')) {
            siguiente.textContent = '';
        }
    }

    function marcarValido(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        var siguiente = input.nextElementSibling;
        if (siguiente && siguiente.classList.contains('field-error')) {
            siguiente.textContent = '';
        }
    }

    function evaluar(input, regla) {
        var valor = input.value.trim();
        if (valor === '') {
            limpiarError(input);
            return;
        }
        if (regla.regex && !regla.regex.test(valor)) {
            mostrarError(input, regla.mensaje);
            return;
        }
        if (regla.min !== undefined) {
            var numero = parseFloat(valor);
            if (isNaN(numero) || numero < regla.min) {
                mostrarError(input, regla.mensaje);
                return;
            }
        }
        marcarValido(input);
    }

    Object.keys(reglas).forEach(function (id) {
        var input = document.getElementById(id);
        if (!input) {
            return;
        }
        var regla = reglas[id];

        // Mientras se escribe: si ya es válido, se limpia el error de inmediato;
        // si no lo es, se espera un momento antes de avisar (para no molestar
        // mientras la persona todavía está completando el valor).
        input.addEventListener('input', function () {
            var valor = input.value.trim();
            var pasaRegex = !regla.regex || regla.regex.test(valor);
            var pasaMin = regla.min === undefined || (!isNaN(parseFloat(valor)) && parseFloat(valor) >= regla.min);
            if (valor === '') {
                limpiarError(input);
            } else if (pasaRegex && pasaMin) {
                marcarValido(input);
            }

            var temporizadorPrevio = temporizadores.get(input);
            if (temporizadorPrevio) {
                clearTimeout(temporizadorPrevio);
            }
            temporizadores.set(input, setTimeout(function () { evaluar(input, regla); }, DEMORA_MS));
        });

        input.addEventListener('blur', function () {
            var temporizadorPrevio = temporizadores.get(input);
            if (temporizadorPrevio) {
                clearTimeout(temporizadorPrevio);
            }
            evaluar(input, regla);
        });
    });
});
