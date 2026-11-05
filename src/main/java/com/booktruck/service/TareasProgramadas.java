package com.booktruck.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Revisiones automáticas que no dependen de que alguien abra una página. */
@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final CargaService cargaService;

    public TareasProgramadas(CargaService cargaService) {
        this.cargaService = cargaService;
    }

    // Al arrancar (tras 30 s) y luego cada 10 minutos
    @Scheduled(initialDelay = 30_000, fixedDelay = 600_000)
    public void vencerCargas() {
        int vencidas = cargaService.vencerCargasPasadas();
        if (vencidas > 0) {
            log.info("Cargas vencidas por fecha de recolección: {}", vencidas);
        }
    }
}
