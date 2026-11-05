-- =====================================================================
-- BOOKTRUCK - Script de creación de base de datos
-- Motor: MySQL 8.x
-- =====================================================================

DROP DATABASE IF EXISTS booktruck_db;
CREATE DATABASE booktruck_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE booktruck_db;

-- =====================================================================
-- 1. USUARIOS
-- Tabla base de autenticación. Empresa y Transportador extienden esta
-- tabla mediante una relación 1:1 (usuario_id como FK única).
-- =====================================================================
CREATE TABLE usuarios (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(150) NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,
    rol               VARCHAR(20)  NOT NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Foto de perfil (transportador) o logo (empresa) en uploads/fotos; NULL = se muestran las iniciales
    foto_ruta         VARCHAR(255) NULL,

    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('EMPRESA', 'TRANSPORTADOR', 'ADMINISTRADOR'))
) ENGINE=InnoDB;

-- =====================================================================
-- 2. EMPRESAS
-- =====================================================================
CREATE TABLE empresas (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id        BIGINT NOT NULL,
    nit               VARCHAR(20)  NOT NULL,
    razon_social      VARCHAR(150) NOT NULL,
    telefono          VARCHAR(20)  NOT NULL,
    ciudad            VARCHAR(80)  NOT NULL,
    direccion         VARCHAR(200) NOT NULL,
    estado_verificacion       VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    documento_ruta            VARCHAR(255) NULL,
    documento_nombre_original VARCHAR(255) NULL,
    motivo_rechazo            VARCHAR(255) NULL,

    CONSTRAINT uq_empresas_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_empresas_nit UNIQUE (nit),
    CONSTRAINT fk_empresas_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_empresas_estado_verificacion CHECK (estado_verificacion IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
) ENGINE=InnoDB;

-- =====================================================================
-- 3. TRANSPORTADORES
-- =====================================================================
CREATE TABLE transportadores (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id        BIGINT NOT NULL,
    cedula            VARCHAR(20)  NOT NULL,
    nombre_completo   VARCHAR(150) NOT NULL,
    telefono          VARCHAR(20)  NOT NULL,
    licencia          VARCHAR(30)  NOT NULL,
    disponible        BOOLEAN      NOT NULL DEFAULT FALSE,
    estado_verificacion       VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    documento_ruta            VARCHAR(255) NULL,
    documento_nombre_original VARCHAR(255) NULL,
    motivo_rechazo            VARCHAR(255) NULL,

    CONSTRAINT uq_transportadores_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_transportadores_cedula UNIQUE (cedula),
    CONSTRAINT fk_transportadores_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_transportadores_estado_verificacion CHECK (estado_verificacion IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
) ENGINE=InnoDB;

-- =====================================================================
-- 4. VEHICULOS
-- =====================================================================
CREATE TABLE vehiculos (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    transportador_id      BIGINT NOT NULL,
    placa                 VARCHAR(10)  NOT NULL,
    tipo_vehiculo         VARCHAR(20)  NOT NULL DEFAULT 'SENCILLO',
    marca                 VARCHAR(60)  NOT NULL,
    capacidad_toneladas   DECIMAL(6,2) NOT NULL,
    tipo_carroceria       VARCHAR(30)  NOT NULL,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    -- Peso bruto vehicular (tara + carga máxima) en kg; define la licencia mínima
    -- (C1 hasta 3.500, C2 hasta 10.000 rígidos, C3 articulados o más de 10.000).
    -- NULL solo en vehículos anteriores a la regla; el formulario lo exige.
    peso_bruto_kg         INT          NULL,

    CONSTRAINT uq_vehiculos_placa UNIQUE (placa),
    CONSTRAINT fk_vehiculos_transportador FOREIGN KEY (transportador_id)
        REFERENCES transportadores(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_vehiculos_capacidad CHECK (capacidad_toneladas > 0),
    CONSTRAINT chk_vehiculos_carroceria CHECK (
        tipo_carroceria IN ('ESTACAS', 'FURGON', 'PLANCHA', 'CONTENEDOR', 'REFRIGERADO', 'TOLVA')
    ),
    CONSTRAINT chk_vehiculos_tipo_vehiculo CHECK (tipo_vehiculo IN ('SENCILLO', 'ARTICULADO')),
    CONSTRAINT chk_vehiculos_peso_bruto CHECK (peso_bruto_kg IS NULL OR peso_bruto_kg > 0)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. CARGAS
-- =====================================================================
CREATE TABLE cargas (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id            BIGINT NOT NULL,
    origen                VARCHAR(100) NOT NULL,
    destino                VARCHAR(100) NOT NULL,
    direccion_origen       VARCHAR(200) NOT NULL,
    direccion_destino      VARCHAR(200) NOT NULL,
    tipo_mercancia         VARCHAR(100) NOT NULL,
    peso                   DECIMAL(8,2) NOT NULL,
    precio_ofrecido        DECIMAL(12,2) NOT NULL,
    fecha_recoleccion      DATETIME     NOT NULL,
    fecha_publicacion      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado                 VARCHAR(20)  NOT NULL DEFAULT 'PUBLICADA',
    -- Carrocería que exige la mercancía (debe coincidir exactamente con la del vehículo); NULL = cualquiera
    carroceria_requerida   VARCHAR(30)  NULL,

    CONSTRAINT fk_cargas_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_cargas_peso CHECK (peso > 0),
    CONSTRAINT chk_cargas_precio CHECK (precio_ofrecido >= 0),
    CONSTRAINT chk_cargas_origen_destino CHECK (origen <> destino),
    CONSTRAINT chk_cargas_estado CHECK (
        -- VENCIDA: llegó la fecha de recolección sin que nadie la tomara (tarea automática cada 10 min)
        estado IN ('PUBLICADA', 'EN_PROCESO', 'ASIGNADA', 'COMPLETADA', 'CANCELADA', 'VENCIDA')
    ),
    CONSTRAINT chk_cargas_carroceria CHECK (carroceria_requerida IS NULL OR
        carroceria_requerida IN ('ESTACAS', 'FURGON', 'PLANCHA', 'CONTENEDOR', 'REFRIGERADO', 'TOLVA'))
) ENGINE=InnoDB;

-- =====================================================================
-- 6. OFERTAS
-- =====================================================================
CREATE TABLE ofertas (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    carga_id          BIGINT NOT NULL,
    transportador_id  BIGINT NOT NULL,
    vehiculo_id       BIGINT NOT NULL,
    monto             DECIMAL(12,2) NOT NULL,
    estado            VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    fecha_oferta      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ofertas_carga FOREIGN KEY (carga_id)
        REFERENCES cargas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ofertas_transportador FOREIGN KEY (transportador_id)
        REFERENCES transportadores(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ofertas_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_ofertas_carga_transportador UNIQUE (carga_id, transportador_id),
    CONSTRAINT chk_ofertas_monto CHECK (monto >= 0),
    CONSTRAINT chk_ofertas_estado CHECK (
        estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA')
    )
) ENGINE=InnoDB;

-- =====================================================================
-- 7. VIAJES
-- Nace de una oferta aceptada. La relación con oferta es 1:1.
-- carga_id y transportador_id son redundantes con oferta_id (se podrían
-- derivar navegando oferta -> carga y oferta -> transportador) pero se
-- mantienen desnormalizados para simplificar consultas y reportes.
-- La BD no puede garantizar que coincidan con los de la oferta; esa
-- consistencia la garantiza ViajeService.crearDesdeOferta() al
-- construir el viaje siempre a partir de los datos de la oferta.
-- =====================================================================
CREATE TABLE viajes (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    oferta_id         BIGINT NOT NULL,
    carga_id          BIGINT NOT NULL,
    vehiculo_id       BIGINT NOT NULL,
    transportador_id  BIGINT NOT NULL,
    estado            VARCHAR(20)  NOT NULL DEFAULT 'PROGRAMADO',
    motivo_cancelacion VARCHAR(30) NULL,
    fecha_inicio      DATETIME     NULL,
    fecha_fin         DATETIME     NULL,
    -- Comisión simulada de BookTruck: se calcula al pasar a FINALIZADO (% del monto de la oferta aceptada)
    comision_porcentaje DECIMAL(5,2)  NULL,
    comision_monto      DECIMAL(12,2) NULL,
    -- Quién canceló el viaje (para el historial del transportador que ven las empresas)
    cancelado_por       VARCHAR(20)  NULL,
    -- Prueba de entrega (foto o remesa firmada) que sube el transportador al marcar ENTREGADO
    evidencia_ruta            VARCHAR(255) NULL,
    evidencia_nombre_original VARCHAR(255) NULL,

    CONSTRAINT uq_viajes_oferta UNIQUE (oferta_id),
    CONSTRAINT fk_viajes_oferta FOREIGN KEY (oferta_id)
        REFERENCES ofertas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_viajes_carga FOREIGN KEY (carga_id)
        REFERENCES cargas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_viajes_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_viajes_transportador FOREIGN KEY (transportador_id)
        REFERENCES transportadores(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_viajes_estado CHECK (
        estado IN ('PROGRAMADO', 'EN_CURSO', 'ENTREGADO', 'FINALIZADO', 'CANCELADO')
    ),
    CONSTRAINT chk_viajes_motivo_cancelacion CHECK (
        motivo_cancelacion IS NULL OR motivo_cancelacion IN
            ('CARGA_YA_ASIGNADA', 'ESPACIO_INSUFICIENTE', 'CONDUCTOR_NO_COINCIDE', 'VEHICULO_NO_DISPONIBLE', 'OTRO')
    ),
    CONSTRAINT chk_viajes_cancelado_por CHECK (
        cancelado_por IS NULL OR cancelado_por IN ('EMPRESA', 'TRANSPORTADOR', 'ADMINISTRADOR')
    )
) ENGINE=InnoDB;

-- =====================================================================
-- 8. SEGUIMIENTO_UBICACIONES
-- Única tabla con CASCADE: si el viaje se elimina, su historial de
-- ubicaciones no tiene razón de existir de forma independiente.
-- =====================================================================
CREATE TABLE seguimiento_ubicaciones (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    viaje_id          BIGINT NOT NULL,
    latitud           DECIMAL(10,7) NOT NULL,
    longitud          DECIMAL(10,7) NOT NULL,
    fecha_hora        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion       VARCHAR(255)  NULL,

    CONSTRAINT fk_seguimiento_viaje FOREIGN KEY (viaje_id)
        REFERENCES viajes(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_seguimiento_viaje_fecha ON seguimiento_ubicaciones (viaje_id, fecha_hora);

-- =====================================================================
-- 9. CALIFICACIONES
-- Dos calificaciones posibles por viaje: EMPRESA -> TRANSPORTADOR y
-- TRANSPORTADOR -> EMPRESA. evaluador_tipo identifica quién califica;
-- empresa_id/transportador_id siguen siendo los dos participantes del
-- viaje (no cambia quién es quién, solo quién emitió esta fila).
-- La restricción única es por (viaje_id, evaluador_tipo): máximo una
-- calificación por evaluador y por viaje.
-- =====================================================================
CREATE TABLE calificaciones (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    viaje_id          BIGINT NOT NULL,
    empresa_id        BIGINT NOT NULL,
    transportador_id  BIGINT NOT NULL,
    evaluador_tipo    VARCHAR(20) NOT NULL,
    puntuacion        TINYINT NOT NULL,
    comentario        VARCHAR(500) NULL,
    fecha             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_calificaciones_viaje_evaluador UNIQUE (viaje_id, evaluador_tipo),
    CONSTRAINT fk_calificaciones_viaje FOREIGN KEY (viaje_id)
        REFERENCES viajes(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificaciones_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificaciones_transportador FOREIGN KEY (transportador_id)
        REFERENCES transportadores(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_calificaciones_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    CONSTRAINT chk_calificaciones_evaluador CHECK (evaluador_tipo IN ('EMPRESA', 'TRANSPORTADOR'))
) ENGINE=InnoDB;

-- =====================================================================
-- 10. DOCUMENTOS
-- Documentos de verificación, uno por (dueño, tipo). El dueño es una
-- empresa, un transportador o un vehículo (exactamente uno). El estado de
-- verificación de empresas/transportadores se calcula a partir de estos.
-- Las columnas documento_ruta / documento_nombre_original / motivo_rechazo
-- de empresas y transportadores quedaron sin uso (versión anterior).
-- POLIZA_RC sigue permitido por chk_documentos_tipo, pero la aplicación ya
-- no lo pide (se decidió no exigir póliza; se dejó así para evitar un ALTER).
-- =====================================================================
CREATE TABLE documentos (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id         BIGINT       NULL,
    transportador_id   BIGINT       NULL,
    vehiculo_id        BIGINT       NULL,
    tipo               VARCHAR(40)  NOT NULL,
    archivo_ruta       VARCHAR(255) NOT NULL,
    nombre_original    VARCHAR(255) NOT NULL,
    fecha_vencimiento  DATE         NULL,
    estado             VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo     VARCHAR(255) NULL,
    fecha_subida       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_revision     DATETIME     NULL,

    CONSTRAINT uq_documentos_empresa_tipo       UNIQUE (empresa_id, tipo),
    CONSTRAINT uq_documentos_transportador_tipo UNIQUE (transportador_id, tipo),
    CONSTRAINT uq_documentos_vehiculo_tipo      UNIQUE (vehiculo_id, tipo),
    CONSTRAINT fk_documentos_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_documentos_transportador FOREIGN KEY (transportador_id)
        REFERENCES transportadores(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_documentos_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_documentos_un_dueno CHECK (
        (empresa_id IS NOT NULL) + (transportador_id IS NOT NULL) + (vehiculo_id IS NOT NULL) = 1
    ),
    CONSTRAINT chk_documentos_tipo CHECK (tipo IN (
        'RUT', 'CAMARA_COMERCIO', 'CEDULA_REPRESENTANTE', 'CEDULA', 'LICENCIA_CONDUCCION',
        'LICENCIA_TRANSITO', 'SOAT', 'TECNICOMECANICA', 'POLIZA_RC'
    )),
    CONSTRAINT chk_documentos_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
) ENGINE=InnoDB;

-- =====================================================================
-- 11. NOTIFICACIONES
-- Avisos dentro de la app (campanita): nueva oferta, oferta aceptada,
-- documento aprobado/rechazado, viaje iniciado/entregado, carga vencida...
-- enlace = ruta interna a la que lleva el aviso.
-- =====================================================================
CREATE TABLE notificaciones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL,
    mensaje     VARCHAR(255) NOT NULL,
    enlace      VARCHAR(255) NULL,
    leida       BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_notificaciones_usuario (usuario_id, leida)
) ENGINE=InnoDB;

-- =====================================================================
-- 12. AUDITORIA
-- Acciones de los administradores (aprobar/rechazar documentos, activar o
-- desactivar cuentas y vehículos, crear administradores, cancelar viajes).
-- =====================================================================
CREATE TABLE auditoria (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL,
    accion      VARCHAR(50)  NOT NULL,
    detalle     VARCHAR(255) NOT NULL,
    fecha       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_auditoria_fecha (fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- Índices adicionales de apoyo a consultas frecuentes
-- =====================================================================
CREATE INDEX idx_cargas_estado ON cargas (estado);
CREATE INDEX idx_cargas_empresa ON cargas (empresa_id);
CREATE INDEX idx_ofertas_carga ON ofertas (carga_id);
CREATE INDEX idx_ofertas_transportador ON ofertas (transportador_id);
CREATE INDEX idx_ofertas_vehiculo ON ofertas (vehiculo_id);
CREATE INDEX idx_viajes_transportador ON viajes (transportador_id);
CREATE INDEX idx_viajes_estado ON viajes (estado);
