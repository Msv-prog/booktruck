var barraProgreso = null;

document.addEventListener('DOMContentLoaded', function () {
    barraProgreso = document.createElement('div');
    barraProgreso.id = 'nav-progress-bar';
    document.body.appendChild(barraProgreso);
});

function iniciarBarraProgreso() {
    if (!barraProgreso) {
        return;
    }
    barraProgreso.classList.add('is-active');
    barraProgreso.style.width = '0%';
    void barraProgreso.offsetWidth;
    barraProgreso.style.width = '75%';
}

window.addEventListener('pageshow', function () {
    if (barraProgreso) {
        barraProgreso.classList.remove('is-active');
        barraProgreso.style.width = '0%';
    }
});

document.addEventListener('click', function (event) {
    var enlace = event.target.closest('a');
    if (!enlace || event.defaultPrevented || event.button !== 0
        || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
        return;
    }
    if (enlace.target && enlace.target !== '_self') {
        return;
    }
    if (enlace.hasAttribute('download')) {
        return;
    }
    var href = enlace.getAttribute('href') || '';
    if (href === '' || href.charAt(0) === '#'
        || href.indexOf('javascript:') === 0 || href.indexOf('mailto:') === 0 || href.indexOf('tel:') === 0) {
        return;
    }
    if (enlace.origin !== window.location.origin) {
        return;
    }
    iniciarBarraProgreso();
}, true);

document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!(form instanceof HTMLFormElement) || form.classList.contains('js-confirm')) {
        return;
    }
    iniciarBarraProgreso();
}, true);
