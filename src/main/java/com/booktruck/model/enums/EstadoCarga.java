package com.booktruck.model.enums;

public enum EstadoCarga {
    PUBLICADA,
    EN_PROCESO,
    ASIGNADA,
    COMPLETADA,
    CANCELADA,
    // Llegó la fecha de recolección y nadie la tomó (ver CargaService.vencerCargasPasadas)
    VENCIDA
}
