var MARCAS_VEHICULO = [
    'Chevrolet', 'Ford', 'Freightliner', 'International', 'Kenworth', 'Hino',
    'Isuzu', 'JAC', 'Foton', 'Dongfeng', 'Volvo', 'Mercedes-Benz', 'Mack', 'Otro'
];

var TIPOS_MERCANCIA = [
    'Alimentos', 'Electrodomésticos', 'Textiles', 'Materiales de construcción',
    'Maquinaria', 'Muebles', 'Productos químicos', 'Papelería', 'Repuestos',
    'Vehículos', 'Productos agrícolas', 'Otro'
];

var DEPARTAMENTOS_CIUDADES = {
    'Amazonas': ['Leticia'],
    'Antioquia': ['Medellín', 'Bello', 'Envigado', 'Itagüí', 'Rionegro', 'Apartadó'],
    'Arauca': ['Arauca'],
    'Atlántico': ['Barranquilla', 'Soledad', 'Malambo'],
    'Bolívar': ['Cartagena', 'Magangué'],
    'Boyacá': ['Tunja', 'Duitama', 'Sogamoso'],
    'Caldas': ['Manizales', 'La Dorada'],
    'Caquetá': ['Florencia'],
    'Casanare': ['Yopal'],
    'Cauca': ['Popayán'],
    'Cesar': ['Valledupar'],
    'Chocó': ['Quibdó'],
    'Córdoba': ['Montería'],
    'Cundinamarca': ['Bogotá', 'Soacha', 'Zipaquirá', 'Facatativá', 'Chía'],
    'Guainía': ['Inírida'],
    'Guaviare': ['San José del Guaviare'],
    'Huila': ['Neiva'],
    'La Guajira': ['Riohacha'],
    'Magdalena': ['Santa Marta'],
    'Meta': ['Villavicencio'],
    'Nariño': ['Pasto', 'Ipiales'],
    'Norte de Santander': ['Cúcuta', 'Ocaña'],
    'Putumayo': ['Mocoa'],
    'Quindío': ['Armenia'],
    'Risaralda': ['Pereira', 'Dosquebradas'],
    'San Andrés y Providencia': ['San Andrés'],
    'Santander': ['Bucaramanga', 'Floridablanca', 'Girón', 'Barrancabermeja'],
    'Sucre': ['Sincelejo'],
    'Tolima': ['Ibagué'],
    'Valle del Cauca': ['Cali', 'Palmira', 'Buenaventura', 'Tuluá'],
    'Vaupés': ['Mitú'],
    'Vichada': ['Puerto Carreño']
};

var CIUDADES_PRINCIPALES = (function () {
    var todas = [];
    Object.keys(DEPARTAMENTOS_CIUDADES).forEach(function (depto) {
        DEPARTAMENTOS_CIUDADES[depto].forEach(function (ciudad) {
            if (todas.indexOf(ciudad) === -1) {
                todas.push(ciudad);
            }
        });
    });
    return todas.sort();
})();
