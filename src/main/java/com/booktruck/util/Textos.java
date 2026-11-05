package com.booktruck.util;

import java.text.Normalizer;

/** Utilidades para las búsquedas de texto de los filtros. */
public final class Textos {

    private Textos() {}

    /** Minúsculas y sin tildes, para que "perez" encuentre "Pérez". */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    /** true si la búsqueda está vacía o si alguno de los valores la contiene. */
    public static boolean coincide(String busqueda, String... valores) {
        String buscado = normalizar(busqueda);
        if (buscado.isEmpty()) {
            return true;
        }
        for (String valor : valores) {
            if (normalizar(valor).contains(buscado)) {
                return true;
            }
        }
        return false;
    }

    /** Dinero con el formato de toda la app: $1.500.000 (sin decimales). */
    public static String dinero(java.math.BigDecimal valor) {
        if (valor == null) {
            return "$0";
        }
        return "$" + String.format(java.util.Locale.ROOT, "%,d", valor.setScale(0, java.math.RoundingMode.HALF_UP).longValue())
                .replace(',', '.');
    }

    /** Dos iniciales para el avatar, ej. "Logística Andina SAS" → "LA" */
    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        StringBuilder iniciales = new StringBuilder();
        for (String palabra : nombre.trim().split("\\s+")) {
            if (palabra.length() <= 3 && palabra.equals(palabra.toLowerCase())) {
                continue;
            }
            iniciales.append(Character.toUpperCase(palabra.charAt(0)));
            if (iniciales.length() == 2) {
                break;
            }
        }
        return iniciales.isEmpty() ? nombre.substring(0, 1).toUpperCase() : iniciales.toString();
    }

    /** Un parámetro de texto vacío cuenta como "sin filtro". */
    public static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
