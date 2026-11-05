package com.booktruck.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Paginación de una lista en memoria; los enlaces conservan los filtros */
public class Paginacion<T> {

    public static final int TAMANO = 15;

    private final List<T> contenido;
    private final int pagina;
    private final int totalPaginas;
    private final int total;
    private final String urlBase;

    private Paginacion(List<T> contenido, int pagina, int totalPaginas, int total, String urlBase) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.totalPaginas = totalPaginas;
        this.total = total;
        this.urlBase = urlBase;
    }

    /** Recorta la lista a la página pedida (empieza en 1); una página fuera de rango se ajusta. */
    public static <T> Paginacion<T> de(List<T> lista, Integer paginaPedida) {
        int total = lista.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) TAMANO));
        int pagina = paginaPedida == null ? 1 : Math.min(Math.max(paginaPedida, 1), totalPaginas);
        int desde = (pagina - 1) * TAMANO;
        int hasta = Math.min(desde + TAMANO, total);

        String url = ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("pagina").build().toUriString();
        String urlBase = url + (url.contains("?") ? "&" : "?") + "pagina=";
        return new Paginacion<>(lista.subList(desde, hasta), pagina, totalPaginas, total, urlBase);
    }

    public List<T> getContenido() { return contenido; }
    public int getPagina() { return pagina; }
    public int getTotalPaginas() { return totalPaginas; }
    public int getTotal() { return total; }
    public boolean isVariasPaginas() { return totalPaginas > 1; }

    /** Enlace a otra página conservando los filtros actuales. */
    public String url(int numero) {
        return urlBase + numero;
    }
}
