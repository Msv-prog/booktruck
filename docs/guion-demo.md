# Guía para la demostración

Orden que sigo para mostrar el proyecto (unos 15 minutos). Antes de empezar hay que tener MySQL encendido y la aplicación corriendo en http://localhost:8080.

Las cuentas de empresa y transportador que nombro aquí son las de mis datos de prueba locales; en una instalación nueva hay que registrarlas desde la página principal. Todas usan la contraseña `Test1234!`.

## 1. Página principal
Mostrar la página de inicio, los términos y la política de privacidad. Explicar que la plataforma gana una comisión del 3 % sobre cada viaje y que se le descuenta al transportador.

## 2. Registro y verificación
- En el registro de transportador se escoge la categoría de licencia (C1, C2 o C3).
- Entrar con una cuenta sin verificar (transportador9): aparece el aviso de lo que falta y no puede ofertar.
- Entrar como administrador, ir a Transportadores, filtrar por pendientes y revisar los documentos: aprobar uno y rechazar otro con un motivo.
- En Auditoría queda registrado quién lo hizo y cuándo.
- En el dashboard se ven los documentos vencidos o que están por vencer.

## 3. La empresa publica una carga
Con empresa1: publicar una carga escogiendo origen, destino, peso, carrocería (por ejemplo refrigerado) y fecha. Antes de publicar sale un resumen para confirmar. En Mis cargas se puede editar, cancelar o volver a publicar una carga vencida.

## 4. El transportador oferta
Con transportador3: en Cargas disponibles marcar "Solo las que puedo tomar". Al ofertar solo aparecen los vehículos que sirven para esa carga y los demás muestran el motivo (por ejemplo, que la carga pide refrigerado). En Vehículos se ve qué licencia exige cada uno según su peso bruto.

## 5. La empresa acepta la oferta
La empresa recibe la notificación de la nueva oferta. En Ofertas recibidas se ve la calificación, los viajes del transportador y su vehículo. Al seleccionar una oferta se crea el viaje y el transportador recibe la notificación.

## 6. Viaje y entrega
- El transportador inicia el viaje y registra su ubicación.
- La empresa ve el recorrido en el mapa desde la página del viaje.
- El transportador marca la entrega subiendo la remesa o una foto.
- La empresa revisa la prueba, confirma la recepción (ahí se calcula la comisión) y califica.

## 7. Administración
Dashboard con gráficas, reportes por rango de fechas, filtros y paginación en las tablas y el registro de auditoría.

## 8. Pruebas
Las reglas principales tienen pruebas automáticas:

```bash
mvnw test -Dtest=CategoriaLicenciaTest,CargaYViajeTest,VerificacionTest,ComisionTest
```

## Cuentas de mis datos de prueba

- `admin@booktruck.test`: administrador.
- `empresa1@booktruck.test`: empresa con cargas, ofertas y viajes.
- `empresa7@booktruck.test`: empresa con un documento rechazado.
- `transportador1@booktruck.test`: tiene un vehículo con la técnico-mecánica rechazada.
- `transportador3@booktruck.test`: tiene un vehículo refrigerado.
- `transportador5@booktruck.test`: licencia por vencer.
- `transportador10@booktruck.test`: licencia C2.
- `transportador12@booktruck.test`: licencia C1 con camión liviano.
