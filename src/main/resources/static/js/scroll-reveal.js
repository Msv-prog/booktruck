document.addEventListener('DOMContentLoaded', function () {
    var elementos = document.querySelectorAll('.scroll-reveal');
    if (!elementos.length) {
        return;
    }

    if (!('IntersectionObserver' in window)) {
        elementos.forEach(function (el) { el.classList.add('is-visible'); });
        return;
    }

    var observador = new IntersectionObserver(function (entradas) {
        entradas.forEach(function (entrada) {
            if (entrada.isIntersecting) {
                entrada.target.classList.add('is-visible');
                observador.unobserve(entrada.target);
            }
        });
    }, { threshold: 0.15 });

    elementos.forEach(function (el) { observador.observe(el); });
});
