# Evaluación funcional de Mercadito Llanero

## Actualización del frontend del 12 de noviembre de 2026

Se revisaron las páginas existentes y se completó la interfaz servida por Spring Boot en `src/main/resources/static`. Conserva los colores, tipografías y presentación del proyecto y añade adaptación a móviles, etiquetas de campos, foco visible, enlaces de salto al contenido y diálogos operables con teclado.

Quedaron conectados y comprobados desde navegador el registro de productor y comprador, ingreso con credenciales válidas e inválidas, consulta y cierre de sesión, recuperación simulada, panel por rol, perfil de sesión, catálogo, búsqueda, filtros por categoría y detalle de producto. El productor puede crear categorías, publicar, editar y eliminar sus productos con confirmación.

La recuperación utiliza el código que genera la aplicación en su consola local, conforme al alcance académico. Se comprobó el cambio efectivo de contraseña en PostgreSQL, el rechazo del código incorrecto y el ingreso con la nueva clave mientras la anterior deja de funcionar.

La ruta `/panel.html` exige sesión desde el servidor y utiliza `Cache-Control: no-store`. Las operaciones de productos y categorías mantienen sus controles de permisos del backend. La interfaz obtiene la identidad de la sesión HTTP y no guarda contraseñas ni datos de sesión en almacenamiento local del navegador.

### Resultados de esta actualización

- Las 16 pruebas Java pasaron con cero fallos y cero errores usando validación del esquema de PostgreSQL.
- Pasaron 22 comprobaciones de navegador con Microsoft Edge sobre registro, acceso, recuperación, productos, permisos de navegación y tamaños móviles. No se registraron errores JavaScript en esos recorridos.
- Se comprobó además la presentación en escritorio y móvil, el mensaje al interrumpir la conexión del catálogo y el reintento satisfactorio al recuperarla.
- Las comprobaciones de navegador utilizaron cuentas, categorías y productos temporales en la base real. Se retiraron esos datos al finalizar y se confirmó que las cuentas temporales habían sido eliminadas.

### Ejecución y uso

Desde la raíz del proyecto, con PostgreSQL disponible y Java 21 configurado en `JAVA_HOME`, ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080/`. El registro conduce al ingreso. Al ingresar se abre `panel.html` con el perfil y las funciones disponibles según el rol. El enlace de recuperación permite solicitar un código, introducir el que aparece en la consola y guardar una nueva contraseña.

La aplicación necesita ejecutar Spring Boot para utilizar los formularios y el catálogo. Los archivos HTML se sirven desde la misma dirección que la API para compartir la sesión.

### Alcance comercial pendiente

Continúan pendientes el backend y el flujo de solicitudes de compra, el directorio público de productores y la unidad de venta. El panel del comprador permite consultar su cuenta y explorar el catálogo. Las pantallas de esta actualización cubren las funciones disponibles del backend. La política provisional de categorías sigue permitiendo crearlas a productores activos.

Las secciones siguientes conservan el diagnóstico del 11 de noviembre para consultar los hallazgos originales. La afirmación histórica sobre la ausencia de frontend queda sustituida por esta actualización. La documentación Word elaborada antes de este cambio conserva su fecha y estado anterior.

## Diagnóstico del 11 de noviembre

Revisión del 11 de noviembre de 2026, basada en el código disponible, el documento de requisitos e historias de usuario y el propósito descrito en Proyecto final.pdf.

## Resultado

El backend compila con Java 21. Pasaron 16 pruebas, con cero fallos, cero errores y cero omisiones. La aplicación cargó su contexto y conectó con PostgreSQL 18.6. Hibernate validó el esquema existente mediante `spring.jpa.hibernate.ddl-auto=validate`.

La plataforma tiene avances en autenticación y administración de productos. El objetivo de conectar productores y compradores mediante solicitudes de compra continúa incompleto porque falta ese módulo y la interfaz web en la carpeta revisada.

## Correcciones realizadas

| Hallazgo | Efecto anterior | Corrección y evidencia |
|---|---|---|
| Categorías con ID proporcionado por el cliente | Una creación podía intentar modificar un registro existente. | El servicio limpia el ID antes de guardar. Prueba de creación con ID 99 verifica ID limpio. |
| Errores de categorías sin manejo uniforme | Datos vacíos o duplicados podían terminar en un error 500. | Se incorporó `ApiExceptionHandler`, con respuestas 400 para validación y JSON ilegible, y 409 para conflictos de integridad. Hay pruebas HTTP de categoría vacía y JSON inválido. |
| Creación pública de categorías | Un visitante podía escribir en el catálogo. | El controlador exige sesión y productor activo consultado en la base de datos. Prueba HTTP verifica 401 para visitante, 403 para comprador y éxito para productor. |
| Campos superiores a los tamaños de columnas | Nombres, correos o descripciones podían fallar al persistir. | Se validan longitudes de usuario, categoría y producto antes de guardar. |
| Precio fuera de `numeric(12,2)` | Un precio demasiado grande podía fallar y uno con exceso de decimales podía redondearse. | Se admiten como máximo 10 dígitos enteros y 2 decimales significativos. Las pruebas rechazan exceso de tamaño y escala. |
| Contraseña con exceso de bytes para BCrypt o formada por espacios | El registro o restablecimiento podía fallar al generar el hash o aceptar una clave vacía de contenido. | Política compartida para registro y restablecimiento, mínimo 8 caracteres, contenido distinto de espacios y máximo 72 bytes UTF-8. |
| Recuperación con correo inválido y datos nulos | La recuperación aceptaba correos mal formados y una llamada al servicio con datos nulos causaba una excepción inesperada. | Validación compartida de correo y comprobación de datos nulos en ambos métodos. |
| Mensaje ambiguo de recuperación | La respuesta podía interpretarse como un envío de correo real. | El mensaje explica la simulación local y dónde consulta el evaluador el código. |
| Bloqueo de Mockito al cargar su agente durante las pruebas | En este entorno, la conexión dinámica del agente produjo un error de canal y la ejecución quedó sin terminar. | Surefire inicia Mockito como agente usando la versión administrada por Spring Boot. Las 16 pruebas finalizaron correctamente con la configuración del POM. |

Se decidió permitir la creación de categorías a productores activos porque los roles actuales son PRODUCTOR y COMPRADOR. Esta política provisional debe validarse con la responsable del proyecto y ajustarse si se requiere un administrador independiente.

## Comparación con historias y requisitos funcionales

Los estados expresan alcance comprobado. Una prueba con repositorio simulado acredita la lógica ejercitada. La prueba de contexto acredita configuración, conexión y compatibilidad del esquema. El cierre de cada historia completa también requiere sus pantallas y evidencias de uso.

| Requisitos | Historias | Evaluación actual |
|---|---|---|
| RF-01, RF-02 | HU-01, HU-02 | Registro e ingreso presentes. Pruebas de normalización, hash y credenciales correctas e incorrectas aprobadas. Persistencia de nuevas cuentas desde navegador pendiente de demostrar. |
| RF-03, RF-04 | HU-03, HU-04 | Renovación, consulta y cierre de sesión comprobados en controlador con solicitud simulada. Se verifica timeout configurado de 1.800 segundos. Falta prueba temporal real de expiración y acceso a páginas privadas. |
| RF-05, RF-06 | HU-05, HU-06 | Recuperación simulada presente. Pruebas de correo inválido, entradas nulas, cambio de hash, vencimiento y uso único aprobadas. En las pruebas de restablecimiento se prepara un código en memoria. Falta demostrar el flujo completo de solicitud y captura del código mediante interfaz. |
| RF-07 | HU-07, HU-10 | Catálogo y detalle presentes en API. Faltan unidad de venta y datos públicos del productor. La asociación `productor` está excluida del JSON mediante `JsonIgnore`. |
| RF-08 | HU-08, HU-09 | Métodos de búsqueda por nombre y categoría presentes. Se cargaron los repositorios al iniciar el contexto. Falta comprobar consultas con datos representativos y pantallas. |
| RF-09, RF-10 | HU-11, HU-12 | Creación y listado privado presentes. Pruebas de asignación del propietario desde sesión y rechazo de visitante/comprador aprobadas. Falta unidad de venta. |
| RF-11 | HU-13, HU-14 | Actualización de producto y disponibilidad presentes. Faltan unidad, interfaz y bloqueo de solicitudes según disponibilidad. |
| RF-12 | HU-15 | Eliminación física presente y rechazo de eliminación por otro productor comprobado. Falta confirmación en interfaz y retiro que preserve historial cuando exista el módulo de solicitudes. |
| RF-13 | HU-16 | Listado y creación de categorías presentes, con permisos y validación corregidos. Política de autorización provisional. |
| RF-14 | HU-17 | Consulta de datos de sesión presente. Falta pantalla de perfil. |
| RF-15 | HU-18 | Pendiente. No se identificó directorio público de productores ni municipio y contacto público autorizado. |
| RF-16, RF-17 | HU-19 | Pendientes. No hay entidad, repositorio, servicio ni controlador de solicitudes de compra. |
| RF-18 | HU-20, HU-21 | Pendiente. Falta consulta de solicitudes por comprador y productor. |
| RF-19, RF-20 | HU-21, HU-22 | Pendientes. Faltan transiciones de estado, cancelación y aceptación con descuento de inventario transaccional. |
| RF-21 | Todas | No se identificó frontend HTML/CSS/JS/JSX/TSX en esta carpeta. Las historias completas continúan en desarrollo o pendientes. |
| RF-22 | Todas | Respuestas JSON de error presentes y ampliadas. Faltan mensajes, estados vacíos y confirmaciones en interfaz. |

## Requisitos no funcionales

| Requisito | Evaluación |
|---|---|
| RNF-01 Credenciales | BCrypt presente y comprobado en registro/restablecimiento. DTO de respuesta de usuario excluye contraseña. |
| RNF-02 Permisos | Control de productor y propietario presente en productos. Creación de categorías protegida. Falta cubrir futuros módulos y verificar todas las operaciones con pruebas HTTP integrales. |
| RNF-03 Sesiones | Renovación y cierre comprobados, timeout de 30 minutos configurado, HttpOnly y SameSite presentes. HTTPS y cookie Secure en despliegue público pendientes de verificar. |
| RNF-04 Protección de operaciones | Sin mecanismo explícito identificado para token CSRF o limitación de intentos de ingreso y recuperación. SameSite ofrece una protección parcial que debe evaluarse según el despliegue. |
| RNF-05 Validación | Se ampliaron controles de longitudes, contraseña, precio y recuperación en servidor. Falta validación en formularios. |
| RNF-06 Integridad | Esquema JPA validado con PostgreSQL. No se probaron escrituras concurrentes. Solicitudes e inventario transaccional pendientes. |
| RNF-07 Rendimiento | Sin prueba de carga realizada. Listados utilizan `findAll()` sin paginación. |
| RNF-08 Usabilidad | Pendiente de interfaz y pruebas con usuarios. |
| RNF-09 Accesibilidad | Pendiente de interfaz, teclado y verificación de anchuras. |
| RNF-10 Compatibilidad | Sin evaluación en navegadores. |
| RNF-11 Manejo de fallos | Manejador global añadido para errores esperados. Falta verificar caída de base de datos y fallos transaccionales. |
| RNF-12 Configuración y privacidad | Credenciales locales todavía presentes en `application.properties`. La simulación imprime el código en consola, que requiere cuidado al preparar capturas. |
| RNF-13 Respaldos | Sin exportación/restauración comprobada. |
| RNF-14 Mantenibilidad | Capas presentes y 15 pruebas de regresión añadidas. La prueba de contexto existente también pasó. |
| RNF-15 Reproducibilidad | Java 21, wrapper y dependencias operativos en este equipo. Falta instalación limpia, LEEME específico, configuración de ejemplo y mecanismo de preparación de datos. |

## Fallos y brechas pendientes por gravedad

### Alta

- Falta el módulo de solicitudes de compra, que forma parte del objetivo del proyecto. El comprador dispone del catálogo, pero carece del flujo para solicitar y conocer la respuesta del productor.
- Falta una interfaz web identificable en esta carpeta. Las respuestas de la API requieren pantallas para poder demostrar los recorridos de usuario.
- Faltan controles explícitos contra abuso de ingreso/recuperación y una revisión de CSRF para operaciones autenticadas mediante cookies antes de un despliegue público.

### Media

- Falta directorio de productores, municipio y datos públicos autorizados. El detalle del producto omite completamente la relación con el productor en JSON.
- Falta unidad de venta. La cantidad es entera, de modo que se debe definir si se aceptarán pesos y volúmenes fraccionarios.
- Las credenciales locales están incorporadas a la configuración. La entrega académica necesita un archivo de ejemplo sin secretos.
- Los códigos de recuperación se conservan en memoria y se pierden al reiniciar. El cambio de contraseña tampoco invalida expresamente otras sesiones activas.
- Los listados no tienen paginación y falta medición de rendimiento.
- Las categorías se comprueban por nombre ignorando mayúsculas, mientras la unicidad de la columna depende de la comparación de PostgreSQL. Dos creaciones concurrentes con distinta capitalización requieren una restricción equivalente en base de datos.
- Los productos antiguos sin productor asociado no pueden ser modificados por los productores actuales. Su asignación requiere revisar la titularidad de los datos existentes.
- Hay un archivo residual `LoginDTO.jav` además de `LoginDTO.java`. El primero queda fuera de la compilación de Java y puede confundir la edición.

### Verificaciones pendientes

- Pruebas de escritura y lectura de cada flujo contra una base de datos de prueba, con reversión o limpieza controlada.
- Uso completo desde navegador, expiración real de sesión, capturas y pruebas de compatibilidad.
- Respaldo y restauración, instalación limpia y comportamiento ante indisponibilidad de PostgreSQL.

## Evidencia reproducible

Ejecutar desde la raíz del proyecto con `JAVA_HOME` apuntando al JDK 21 instalado.

```powershell
.\mvnw.cmd -o '-Dspring.jpa.hibernate.ddl-auto=validate' test
```

Resultado obtenido en esta revisión

```text
FuncionalidadTests                  15 pruebas, 0 fallos, 0 errores
MercaditoLlaneroApplicationTests      1 prueba,  0 fallos, 0 errores
Total                              16 pruebas, 0 fallos, 0 errores
BUILD SUCCESS
```

La opción `-o` utiliza las dependencias ya descargadas. En un equipo nuevo se requiere descargarlas primero. El POM configura el agente de Mockito con la ruta del repositorio Maven y la versión administrada por Spring Boot.

Los reportes están en `target/surefire-reports`. La prueba de contexto se ejecutó con validación de esquema y completó la conexión a la base existente. Las pruebas nuevas utilizan repositorios simulados y solicitudes HTTP de MockMvc. Queda pendiente verificar las escrituras reales de cada operación y los flujos completos desde navegador.

## Prioridad de continuación

Completar interfaz y solicitudes de compra para cubrir el objetivo comercial, definir la administración de categorías, añadir unidad y perfil público de productor, y ejecutar las pruebas integrales de aceptación con una base de datos de prueba. Actualizar las historias a terminadas cuando sus criterios estén demostrados en el recorrido completo.
