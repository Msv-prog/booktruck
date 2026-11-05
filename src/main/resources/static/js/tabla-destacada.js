document.addEventListener('DOMContentLoaded', function () {
    var marcada = sessionStorage.getItem('booktruck-fila-marcada');
    if (!marcada) {
        return;
    }
    sessionStorage.removeItem('booktruck-fila-marcada');

    document.querySelectorAll('.app-table tbody tr').forEach(function (fila) {
        var primeraCelda = fila.querySelector('td');
        if (primeraCelda && primeraCelda.textContent.trim() === marcada) {
            fila.classList.add('fila-destacada');
        }
    });
});
