# Mercadito Llanero

Plataforma web académica para productores y compradores de Yopal y Casanare, desarrollada con Java 21, Spring Boot y PostgreSQL. La interfaz HTML, CSS y JavaScript se sirve desde la misma aplicación.

## Funciones disponibles

- Registro como productor o comprador.
- Inicio, consulta y cierre de sesión.
- Recuperación simulada de contraseña mediante un código mostrado en la consola del servidor.
- Catálogo con búsqueda, filtros por categoría y detalle de producto.
- Panel según el rol y consulta de los datos de la cuenta.
- Creación de categorías y administración de productos propios para productores activos.
- Formularios con validación y presentación adaptable a móviles.

## Requisitos de ejecución

- JDK 21 configurado en `JAVA_HOME`.
- PostgreSQL en ejecución y una base de datos `mercadito_llanero` creada.
- Acceso a Internet para la primera descarga de dependencias de Maven.

## Instalación en PowerShell

Desde la carpeta del proyecto que contiene `pom.xml`, crear la configuración local a partir del ejemplo.

```powershell
Copy-Item src/main/resources/application-example.properties src/main/resources/application.properties
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = 'TU_CONTRASENA_LOCAL'
.\mvnw.cmd spring-boot:run
```

Abrir <http://localhost:8080/>. Para otra ubicación de PostgreSQL, definir `DB_URL` con la URL JDBC correspondiente.

`application.properties` está excluido de Git. Conservar las credenciales en la configuración local o en variables de entorno.

## Recuperación de contraseña

En **¿Olvidaste tu contraseña?**, introducir el correo de la cuenta y solicitar un código. Consultar el código en la consola donde se ejecuta Spring Boot y escribirlo en el formulario de recuperación junto con una nueva contraseña. El código vence en 10 minutos y admite un uso.

Este flujo utiliza la simulación académica indicada en el proyecto. El código permanece en memoria hasta su uso, vencimiento o reinicio de la aplicación.

## Pruebas

Con PostgreSQL configurado y las tablas existentes, ejecutar

```powershell
.\mvnw.cmd '-Dspring.jpa.hibernate.ddl-auto=validate' test
```

La última revisión registró 16 pruebas Java aprobadas y 22 comprobaciones de navegador satisfactorias, además de la comprobación de presentación y reintento de red. Los reportes Java se generan en `target/surefire-reports`.

## Estado del proyecto

El detalle de avances y verificaciones se encuentra en [docs/REVISION_FUNCIONAL.md](docs/REVISION_FUNCIONAL.md). Las solicitudes de compra, el directorio público de productores y la unidad de venta continúan pendientes.
