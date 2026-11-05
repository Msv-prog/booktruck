// Dibuja las gráficas de los inicios con Chart.js. Cada <canvas data-grafica="clave" data-tipo="barras|dona|linea"
// data-dinero> toma sus datos de window.datosGraficas (lo arma EstadisticasService).
(function () {
    var datos = window.datosGraficas;
    if (!datos || typeof Chart === 'undefined') {
        return;
    }

    var estilos = getComputedStyle(document.documentElement);
    function color(variable, respaldo) {
        var valor = estilos.getPropertyValue(variable).trim();
        return valor || respaldo;
    }
    var azul = color('--azul-principal', '#2563EB');
    var texto = color('--texto-secundario', '#64748B');
    var borde = color('--borde', '#E2E8F0');
    var paleta = [azul, color('--exito', '#16A34A'), color('--advertencia', '#D97706'), '#8B5CF6', color('--error', '#DC2626')];

    Chart.defaults.color = texto;
    Chart.defaults.font.family = 'Inter, sans-serif';

    var dinero = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

    document.querySelectorAll('canvas[data-grafica]').forEach(function (canvas) {
        var clave = canvas.dataset.grafica;
        var tipo = canvas.dataset.tipo || 'barras';
        var esDinero = canvas.hasAttribute('data-dinero');
        var serie = datos[clave];
        if (!serie) {
            return;
        }

        if (tipo === 'dona') {
            var vacia = !serie.valores.length;
            canvas.closest('.grafica-tarjeta').classList.toggle('is-vacia', vacia);
            if (vacia) {
                return;
            }
            new Chart(canvas, {
                type: 'doughnut',
                data: { labels: serie.etiquetas, datasets: [{ data: serie.valores, backgroundColor: paleta, borderWidth: 0 }] },
                options: { maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
            });
            return;
        }

        var sinDatos = serie.every(function (v) { return Number(v) === 0; });
        canvas.closest('.grafica-tarjeta').classList.toggle('is-vacia', sinDatos);
        if (sinDatos) {
            return;
        }
        new Chart(canvas, {
            type: tipo === 'linea' ? 'line' : 'bar',
            data: {
                labels: datos.meses,
                datasets: [{
                    label: canvas.dataset.etiqueta || '',
                    data: serie.map(Number),
                    backgroundColor: azul,
                    borderColor: azul,
                    borderRadius: 6,
                    tension: 0.3,
                    fill: false
                }]
            },
            options: {
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { callbacks: { label: function (c) { return esDinero ? dinero.format(c.parsed.y) : c.parsed.y; } } }
                },
                scales: {
                    x: { grid: { display: false } },
                    y: {
                        beginAtZero: true,
                        grid: { color: borde },
                        ticks: {
                            precision: 0,
                            callback: function (v) { return esDinero ? dinero.format(v) : v; }
                        }
                    }
                }
            }
        });
    });
})();
