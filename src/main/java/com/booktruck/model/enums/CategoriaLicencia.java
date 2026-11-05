package com.booktruck.model.enums;

/** Licencias de carga según el peso bruto: C1 hasta 3.500 kg, C2 hasta 10.000 kg, C3 articulados o más. */
public enum CategoriaLicencia {

    // Los textos de las opciones están en registro-transportador.html y transportador/perfil.html
    C1(3500),
    C2(10000),
    C3(null);

    private final Integer pesoBrutoMaximoKg;

    CategoriaLicencia(Integer pesoBrutoMaximoKg) {
        this.pesoBrutoMaximoKg = pesoBrutoMaximoKg;
    }

    /** Si esta licencia habilita para conducir un vehículo que exige la categoría indicada. */
    public boolean permite(CategoriaLicencia requerida) {
        return requerida == null || compareTo(requerida) >= 0;
    }

    /** Categoría mínima para un vehículo; null si es sencillo y aún no tiene peso bruto. */
    public static CategoriaLicencia requeridaPara(TipoVehiculo tipo, Integer pesoBrutoKg) {
        if (tipo == TipoVehiculo.ARTICULADO) {
            return C3;
        }
        if (pesoBrutoKg == null) {
            return null;
        }
        for (CategoriaLicencia categoria : values()) {
            if (categoria.pesoBrutoMaximoKg == null || pesoBrutoKg <= categoria.pesoBrutoMaximoKg) {
                return categoria;
            }
        }
        return C3;
    }

    /** Convierte el texto guardado en la BD; null si no es una categoría válida. */
    public static CategoriaLicencia de(String valor) {
        if (valor == null) {
            return null;
        }
        for (CategoriaLicencia categoria : values()) {
            if (categoria.name().equals(valor.trim())) {
                return categoria;
            }
        }
        return null;
    }
}
