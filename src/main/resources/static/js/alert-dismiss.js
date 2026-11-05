document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.alert').forEach(function (alerta) {
        if (alerta.querySelector('.alert-close')) {
            return;
        }
        var boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'alert-close';
        boton.setAttribute('aria-label', 'Cerrar');
        boton.innerHTML = '&times;';
        boton.addEventListener('click', function () {
            alerta.classList.add('is-closing');
            alerta.addEventListener('transitionend', function () {
                alerta.remove();
            }, { once: true });
        });
        alerta.appendChild(boton);
    });
});
