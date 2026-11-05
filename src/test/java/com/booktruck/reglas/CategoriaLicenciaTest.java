package com.booktruck.reglas;

import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.enums.CategoriaLicencia;
import com.booktruck.model.enums.TipoVehiculo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Regla de licencias por peso bruto vehicular: C1 hasta 3.500 kg, C2 hasta 10.000 kg, C3 el resto y articulados. */
class CategoriaLicenciaTest {

    @Test
    void laCategoriaRequeridaSaleDelPesoBruto() {
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.SENCILLO, 3500)).isEqualTo(CategoriaLicencia.C1);
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.SENCILLO, 3501)).isEqualTo(CategoriaLicencia.C2);
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.SENCILLO, 10000)).isEqualTo(CategoriaLicencia.C2);
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.SENCILLO, 10001)).isEqualTo(CategoriaLicencia.C3);
    }

    @Test
    void unArticuladoSiempreExigeC3() {
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.ARTICULADO, 9000)).isEqualTo(CategoriaLicencia.C3);
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.ARTICULADO, null)).isEqualTo(CategoriaLicencia.C3);
    }

    @Test
    void unSencilloSinPesoBrutoNoSePuedeEvaluar() {
        assertThat(CategoriaLicencia.requeridaPara(TipoVehiculo.SENCILLO, null)).isNull();
    }

    @Test
    void unaCategoriaMayorIncluyeLasMenores() {
        assertThat(CategoriaLicencia.C3.permite(CategoriaLicencia.C1)).isTrue();
        assertThat(CategoriaLicencia.C3.permite(CategoriaLicencia.C2)).isTrue();
        assertThat(CategoriaLicencia.C2.permite(CategoriaLicencia.C1)).isTrue();
        assertThat(CategoriaLicencia.C2.permite(CategoriaLicencia.C3)).isFalse();
        assertThat(CategoriaLicencia.C1.permite(CategoriaLicencia.C2)).isFalse();
    }

    @Test
    void soloSeAceptanC1C2YC3() {
        assertThat(CategoriaLicencia.de("C2")).isEqualTo(CategoriaLicencia.C2);
        assertThat(CategoriaLicencia.de("B1")).isNull();
        assertThat(CategoriaLicencia.de(null)).isNull();
    }

    @Test
    void elTransportadorSoloConduceLoQueSuLicenciaPermite() {
        Transportador c1 = new Transportador();
        c1.setLicencia("C1");

        Vehiculo nhr = vehiculo(TipoVehiculo.SENCILLO, 3500);
        Vehiculo camion = vehiculo(TipoVehiculo.SENCILLO, 9000);
        Vehiculo sinPeso = vehiculo(TipoVehiculo.SENCILLO, null);

        assertThat(c1.puedeConducir(nhr)).isTrue();
        assertThat(c1.puedeConducir(camion)).isFalse();
        assertThat(c1.puedeConducir(sinPeso)).isFalse();
        assertThat(nhr.getClasePeso()).isEqualTo("Liviano");
        assertThat(camion.getClasePeso()).isEqualTo("Mediano");
    }

    private static Vehiculo vehiculo(TipoVehiculo tipo, Integer pesoBruto) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setTipoVehiculo(tipo);
        vehiculo.setPesoBrutoKg(pesoBruto);
        return vehiculo;
    }
}
