(function () {
    var contenedor = document.getElementById('mapa');
    var tabla = document.getElementById('tabla-puntos');
    if (!contenedor || !tabla) {
        return;
    }

    delete L.Icon.Default.prototype._getIconUrl;
    L.Icon.Default.mergeOptions({
        iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
        iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png'
    });

    var filas = tabla.querySelectorAll('tbody tr');
    var puntos = [];
    filas.forEach(function (fila) {
        puntos.push({
            lat: parseFloat(fila.dataset.lat),
            lon: parseFloat(fila.dataset.lon),
            fecha: fila.dataset.fecha,
            obs: fila.dataset.obs || ''
        });
    });
    if (puntos.length === 0) {
        return;
    }

    // La tabla llega más reciente primero; para la ruta cronológica se invierte.
    var puntosCronologicos = puntos.slice().reverse();
    var actual = puntos[0];

    var mapa = L.map(contenedor).setView([actual.lat, actual.lon], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(mapa);

    if (puntosCronologicos.length > 1) {
        var linea = puntosCronologicos.map(function (p) { return [p.lat, p.lon]; });
        L.polyline(linea, { color: '#2563EB', weight: 4, opacity: 0.7 }).addTo(mapa);
    }

    var bounds = L.latLngBounds(puntosCronologicos.map(function (p) { return [p.lat, p.lon]; }));
    mapa.fitBounds(bounds, { maxZoom: 15, padding: [40, 40] });

    var marcadorActual = null;
    puntosCronologicos.forEach(function (p, indice) {
        var esActual = indice === puntosCronologicos.length - 1;
        var marcador = L.marker([p.lat, p.lon]).addTo(mapa);
        var etiqueta = esActual ? '<strong>Ubicación actual</strong><br>' : '';
        marcador.bindPopup(etiqueta + p.fecha + (p.obs ? '<br>' + p.obs : ''));
        if (esActual) {
            marcadorActual = marcador;
        }
    });

    if (marcadorActual) {
        setTimeout(function () { marcadorActual.openPopup(); }, 0);
    }
})();
