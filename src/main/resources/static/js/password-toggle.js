document.addEventListener('DOMContentLoaded', function () {
    var iconOjo = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
    var iconOjoTachado = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a19.3 19.3 0 0 1 5.06-5.94M9.9 4.24A9.7 9.7 0 0 1 12 4c7 0 11 8 11 8a19.4 19.4 0 0 1-2.16 3.19"></path><path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>';

    document.querySelectorAll('input[type="password"]').forEach(function (input) {
        if (input.closest('.password-wrapper')) {
            return;
        }
        var wrapper = document.createElement('div');
        wrapper.className = 'password-wrapper';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        var boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'password-toggle-btn';
        boton.setAttribute('aria-label', 'Mostrar contraseña');
        boton.innerHTML = iconOjo;
        wrapper.appendChild(boton);

        boton.addEventListener('click', function () {
            var mostrando = input.type === 'text';
            input.type = mostrando ? 'password' : 'text';
            boton.innerHTML = mostrando ? iconOjo : iconOjoTachado;
            boton.setAttribute('aria-label', mostrando ? 'Mostrar contraseña' : 'Ocultar contraseña');
        });
    });
});
