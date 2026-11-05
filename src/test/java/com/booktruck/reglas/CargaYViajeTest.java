package com.booktruck.reglas;

import com.booktruck.model.Carga;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.util.Textos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Reglas de cargas y viajes que no necesitan base de datos. */
class CargaYViajeTest {

    @Test
    void laCarroceriaDebeCoincidirExactamente() {
        Carga refrigerada = new Carga();
        refrigerada.setCarroceriaRequerida(TipoCarroceria.REFRIGERADO);

        assertThat(refrigerada.admiteCarroceria(vehiculo(TipoCarroceria.REFRIGERADO))).isTrue();
        // Furgón y refrigerado van aparte (tema de alimentos)
        assertThat(refrigerada.admiteCarroceria(vehiculo(TipoCarroceria.FURGON))).isFalse();

        Carga deFurgon = new Carga();
        deFurgon.setCarroceriaRequerida(TipoCarroceria.FURGON);
        assertThat(deFurgon.admiteCarroceria(vehiculo(TipoCarroceria.REFRIGERADO))).isFalse();
    }

    @Test
    void unaCargaSinCarroceriaAceptaCualquiera() {
        Carga carga = new Carga();
        assertThat(carga.admiteCarroceria(vehiculo(TipoCarroceria.TOLVA))).isTrue();
        assertThat(carga.getCarroceriaTexto()).isEqualTo("Cualquiera");
    }

    @Test
    void unaCargaConFechaPasadaYaNoRecibeOfertas() {
        Carga carga = new Carga();
        carga.setFechaRecoleccion(LocalDateTime.now().minusMinutes(5));
        assertThat(carga.isRecoleccionPasada()).isTrue();

        carga.setFechaRecoleccion(LocalDateTime.now().plusDays(1));
        assertThat(carga.isRecoleccionPasada()).isFalse();
    }

    @Test
    void unViajeProgramadoSeAtrasaDespuesDeLasHorasDeGracia() {
        Carga carga = new Carga();
        carga.setFechaRecoleccion(LocalDateTime.now().minusHours(Viaje.HORAS_GRACIA_INICIO + 1));
        Viaje viaje = new Viaje();
        viaje.setCarga(carga);
        viaje.setEstado(EstadoViaje.PROGRAMADO);
        assertThat(viaje.isAtrasado()).isTrue();

        // Uno ya iniciado nunca está atrasado
        viaje.setEstado(EstadoViaje.EN_CURSO);
        assertThat(viaje.isAtrasado()).isFalse();
    }

    @Test
    void elDineroSeMuestraConPuntosDeMiles() {
        assertThat(Textos.dinero(new BigDecimal("1500000"))).isEqualTo("$1.500.000");
        assertThat(Textos.dinero(new BigDecimal("54000.00"))).isEqualTo("$54.000");
        assertThat(Textos.dinero(null)).isEqualTo("$0");
    }

    private static Vehiculo vehiculo(TipoCarroceria carroceria) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setTipoCarroceria(carroceria);
        return vehiculo;
    }
}
