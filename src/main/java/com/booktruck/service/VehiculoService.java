package com.booktruck.service;

import com.booktruck.dto.VehiculoForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.enums.CategoriaLicencia;
import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.model.enums.TipoVehiculo;
import com.booktruck.repository.VehiculoRepository;
import com.booktruck.util.Textos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VehiculoService {

    // El tractocamión más pesado permitido en Colombia (3S3) ronda los 52.000 kg
    private static final int PESO_BRUTO_MAXIMO_KG = 60000;

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<Vehiculo> listarPorTransportador(Transportador transportador) {
        return vehiculoRepository.findByTransportadorId(transportador.getId());
    }

    public long contarPorTransportador(Long transportadorId) {
        return vehiculoRepository.countByTransportadorId(transportadorId);
    }

    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAllByOrderByIdDesc();
    }

    // Filtros del admin (null o vacío = sin filtro)
    public List<Vehiculo> filtrar(List<Vehiculo> vehiculos, TipoVehiculo tipo, TipoCarroceria carroceria,
                                   Boolean activo, String placa) {
        return vehiculos.stream()
                .filter(v -> tipo == null || v.getTipoVehiculo() == tipo)
                .filter(v -> carroceria == null || v.getTipoCarroceria() == carroceria)
                .filter(v -> activo == null || v.getActivo().equals(activo))
                .filter(v -> Textos.coincide(placa, v.getPlaca()))
                .toList();
    }

    @Transactional
    public Vehiculo registrar(Transportador transportador, VehiculoForm form) {
        if (form.getPlaca() == null || form.getPlaca().isBlank()) {
            throw new NegocioException("La placa es obligatoria.");
        }
        if (form.getTipoVehiculo() == null) {
            throw new NegocioException("El tipo de vehículo es obligatorio.");
        }
        if (form.getMarca() == null || form.getMarca().isBlank()) {
            throw new NegocioException("La marca es obligatoria.");
        }
        if (form.getCapacidadToneladas() == null || form.getCapacidadToneladas().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("La capacidad debe ser mayor que cero.");
        }
        if (form.getTipoCarroceria() == null) {
            throw new NegocioException("El tipo de carrocería es obligatorio.");
        }
        validarPesoBruto(transportador, form.getTipoVehiculo(), form.getCapacidadToneladas(), form.getPesoBrutoKg());

        String placa = form.getPlaca().trim().toUpperCase();
        if (form.getTipoVehiculo() == TipoVehiculo.ARTICULADO) {
            if (!placa.matches("^[RS][0-9]{5}$")) {
                throw new NegocioException("La placa de un vehículo articulado debe tener el formato de remolque/semirremolque: R o S seguido de 5 números (ej: S67890).");
            }
        } else {
            if (!placa.matches("^[A-Z]{3}[0-9]{3}$")) {
                throw new NegocioException("La placa debe tener el formato de 3 letras y 3 números (ej: ABC123).");
            }
        }
        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new NegocioException("Ya existe un vehículo registrado con esa placa.");
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setTransportador(transportador);
        vehiculo.setPlaca(placa);
        vehiculo.setTipoVehiculo(form.getTipoVehiculo());
        vehiculo.setMarca(form.getMarca());
        vehiculo.setCapacidadToneladas(form.getCapacidadToneladas());
        vehiculo.setTipoCarroceria(form.getTipoCarroceria());
        vehiculo.setPesoBrutoKg(form.getPesoBrutoKg());
        vehiculo.setActivo(true);
        return vehiculoRepository.save(vehiculo);
    }

    /** Completa el peso bruto de un vehículo que no lo tenía (solo una vez) */
    @Transactional
    public Vehiculo completarPesoBruto(Transportador transportador, Long vehiculoId, Integer pesoBrutoKg) {
        Vehiculo vehiculo = obtenerPropio(transportador, vehiculoId);
        if (vehiculo.getPesoBrutoKg() != null) {
            throw new NegocioException("El peso bruto de " + vehiculo.getPlaca() + " ya está registrado.");
        }
        validarPesoBruto(transportador, vehiculo.getTipoVehiculo(), vehiculo.getCapacidadToneladas(), pesoBrutoKg);
        vehiculo.setPesoBrutoKg(pesoBrutoKg);
        return vehiculoRepository.save(vehiculo);
    }

    // El peso bruto define la licencia y debe ser mayor que la capacidad
    private void validarPesoBruto(Transportador transportador, TipoVehiculo tipo, BigDecimal capacidadToneladas,
                                  Integer pesoBrutoKg) {
        if (pesoBrutoKg == null) {
            throw new NegocioException("El peso bruto vehicular es obligatorio. Lo encuentras en la licencia de tránsito.");
        }
        if (pesoBrutoKg <= 0 || pesoBrutoKg > PESO_BRUTO_MAXIMO_KG) {
            throw new NegocioException("El peso bruto debe estar entre 1 y 60.000 kg.");
        }
        BigDecimal capacidadKg = capacidadToneladas.multiply(BigDecimal.valueOf(1000));
        if (BigDecimal.valueOf(pesoBrutoKg).compareTo(capacidadKg) <= 0) {
            throw new NegocioException("El peso bruto (vehículo vacío + carga máxima) debe ser mayor que la capacidad de carga ("
                    + capacidadKg.stripTrailingZeros().toPlainString() + " kg).");
        }
        CategoriaLicencia requerida = CategoriaLicencia.requeridaPara(tipo, pesoBrutoKg);
        CategoriaLicencia propia = transportador.getCategoriaLicencia();
        if (propia == null || !propia.permite(requerida)) {
            throw new NegocioException("Este vehículo requiere licencia " + requerida.name() + " y la tuya es "
                    + transportador.getLicencia() + ". Si ya tienes una licencia mayor, actualízala primero en tu perfil.");
        }
    }

    @Transactional
    public Vehiculo buscarPorId(Long vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new NegocioException("El vehículo no existe."));
    }

    public Vehiculo obtenerPropio(Transportador transportador, Long vehiculoId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new NegocioException("El vehículo no existe."));
        if (!vehiculo.getTransportador().getId().equals(transportador.getId())) {
            throw new NegocioException("No tienes permiso sobre este vehículo.");
        }
        return vehiculo;
    }

    public void alternarEstado(Transportador transportador, Long vehiculoId) {
        Vehiculo vehiculo = obtenerPropio(transportador, vehiculoId);
        vehiculo.setActivo(!vehiculo.getActivo());
        vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo alternarEstadoAdmin(Long vehiculoId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new NegocioException("El vehículo no existe."));
        vehiculo.setActivo(!vehiculo.getActivo());
        return vehiculoRepository.save(vehiculo);
    }
}
