# Plataforma de Evaluación Técnica

Backend del kata. La pieza que carga el peso arquitectónico es el **motor de
ejecución**: recibe código que no es de confianza, lo corre aislado y lo califica
contra un conjunto de casos de prueba.

## Cómo correrlo

Todo vive en **WSL (Ubuntu)**. Ahí está el demonio de Docker que el aislamiento
necesita. En Windows nativo (JRE 8, sin Maven) no arranca.

### Qué tiene que estar instalado

| Requisito | Notas |
|---|---|
| Docker Desktop, con integración WSL | El backend habla con `docker` y Postgres corre en un contenedor |
| Java 21 | Temurin / JDK, no el JRE 8 de Windows |
| Maven 3.9+ | |
| Node.js ≥ 24.15 | Angular 22; si usas nvm: `nvm use` en la terminal del frontend |
| Puertos libres | **5432** (Postgres), **8080** (API), **4200** (Angular) |

Abre Docker Desktop **antes** de los comandos. Si Docker no está arriba, `docker compose` falla y Spring tampoco arranca.

### Orden (no invertir)

Desde la raíz del repo, en WSL:

**1. Base de datos**

```bash
cd ~/dev/work/kata-assessment-platform
docker compose up -d
docker ps
```

Tiene que aparecer el contenedor `kata-postgres` en el puerto 5432. Si Spring se lanza antes, Flyway muere con:

`Connection to localhost:5432 refused`

**2. Imágenes del sandbox** (Python, Node, Java, TypeScript, COBOL)

```bash
sh scripts/preparar-imagenes.sh
```

Sin esto el API sube, pero ejecutar código falla al no encontrar las imágenes.

**3. Backend** — otra terminal, y déjala abierta:

```bash
cd ~/dev/work/kata-assessment-platform/backend
# si Java/Maven no están en el PATH:
#   source ~/.local/herramientas/entorno.sh
mvn spring-boot:run
```

Espera a ver que quedó escuchando en **8080**.

**4. Frontend** — otra terminal:

```bash
cd ~/dev/work/kata-assessment-platform/frontend
npx ng serve
```

Queda en <http://localhost:4200>. El proxy de Angular reenvía `/api` a `http://localhost:8080`.

### Parar

- Frontend y backend: `Ctrl+C` en cada terminal.
- Postgres: `docker compose down` en la raíz del repo (borra el contenedor; los datos del volumen se conservan).

### Si algo no sube

| Síntoma | Causa habitual |
|---|---|
| `Connection to localhost:5432 refused` | No corriste `docker compose up -d`, o Docker Desktop está cerrado |
| Puerto 8080 ocupado | Quedó un `mvn spring-boot:run` anterior; ciérralo o mata el proceso |
| `ng serve` pide Node más nuevo | `nvm install 24` y `nvm use 24` |
| Ejecutar código falla / no hay imagen | Falta `sh scripts/preparar-imagenes.sh` |
| COBOL o TypeScript no compilan | Esas imágenes se construyen en el paso 2; no vienen de Docker Hub |

### Probar solo el API, sin interfaz

Con Postgres y el backend ya arriba:

```bash
sh scripts/probar-api.sh
```

Aislamiento (EC01), sin backend:

```bash
sh scripts/probar-aislamiento.sh
```

### Sobre CORS

El servidor de desarrollo de Angular reenvía todo lo que empiece por `/api` hacia
el puerto 8080, según `frontend/proxy.conf.json`. Para el navegador existe un solo
origen, así que no hay que habilitar CORS en Spring ni relajar nada por el lado del
servidor. En producción el mismo efecto se logra sirviendo el bundle detrás del
mismo dominio que la API.

## Frontend

Tres capas, siguiendo el MVVM del documento de arquitectura:

| Capa | Archivo | Responsabilidad |
|---|---|---|
| Vista | `evaluacion.html` | Solo presenta y emite eventos |
| VistaModelo | `evaluacion.ts` | Estado en señales, traduce respuestas |
| Modelo | `servicios/evaluacion-api.ts` | Lo único que conoce la forma de la API |

Al cambiar de lenguaje se carga una plantilla de `modelos/plantillas.ts`. Es la
demostración visible de EC02: la lista del selector la responde el backend en
`/api/lenguajes`, no está escrita en el frontend, así que un lenguaje nuevo aparece
en la interfaz sin recompilar Angular.

Los cuatro estados de un caso (`OK`, `SALIDA_DISTINTA`, `TIEMPO_AGOTADO`,
`ERROR_EJECUCION`) se distinguen con color, porque para el evaluador no es lo mismo
una respuesta equivocada que un programa que se colgó.

## Del escenario de calidad al código

| Escenario | Táctica | Patrón | Dónde vive |
|---|---|---|---|
| EC01 Seguridad | Limitar el acceso, validar la entrada | Proxy | `EjecutorProtegido`, `SandboxDocker` |
| EC02 Modificabilidad | Encapsular, aplazar el enlace | Strategy | `Ejecutor` y el paquete `motor.lenguajes` |
| EC03 Disponibilidad y desempeño | Acotar el tiempo de ejecución, limitar la respuesta a un evento | Template Method | `EjecutorBase`, `LimitesEjecucion` |

### El aislamiento

`SandboxDocker` lanza un contenedor por envío con la red desconectada, el sistema
de archivos raíz en solo lectura, sin capacidades de kernel, con usuario no
privilegiado y con techos de memoria, CPU y número de procesos. El único punto
escribible es el directorio temporal montado, que se borra al terminar.

`scripts/probar-aislamiento.sh` comprueba cada una de esas barreras sin necesidad
del backend, y sirve como evidencia de EC01 en la presentación.

### Por qué un solo contenedor por envío

Arrancar un contenedor cuesta entre cientos de milisegundos y algunos segundos.
Con un contenedor por caso de prueba, un ejercicio de diez casos pagaría ese costo
diez veces. `EjecutorBase` genera un guion que compila una vez y recorre los casos
adentro del mismo contenedor, con un `timeout` por caso para que un ciclo infinito
no arrastre a los demás.

### Agregar un lenguaje

Una clase nueva en `motor.lenguajes` que extienda `EjecutorBase` y declare cuatro
cosas: imagen, nombre del archivo fuente, comando de compilación y comando de
ejecución. `RegistroEjecutores` la descubre en el arranque. No se modifica ningún
archivo existente del motor, que es justo la medida que declara EC02. TypeScript
y COBOL están agregados así: dos clases, dos imágenes con el compilador
preinstalado (el sandbox no tiene red).

## Mediciones observadas

Tomadas con `scripts/probar-api.sh` en WSL 2, con las imágenes ya descargadas.
`milisegundos` es el tiempo del caso medido dentro del contenedor;
`milisegundosTotales` es lo que espera el cliente de punta a punta.

| Envío | Tiempo del caso | Total del envío |
|---|---|---|
| Python, 2 casos | 30 ms y 20 ms | 381 ms |
| JavaScript, 1 caso | 40 ms | 351 ms |
| Java, 1 caso | 110 ms | 1358 ms |
| Ciclo infinito en Python | 5010 ms, cortado | 5322 ms |

La brecha de Java es la más informativa: el caso corre en 110 ms, pero el envío
cuesta 1358 ms porque `javac` compila adentro del contenedor. Es el costo de
compilar, no de aislar, y es el primer lugar donde habría que optimizar si EC03
llegara a apretar.

El tiempo por caso se mide leyendo `/proc/uptime` dentro del contenedor, no con
`date +%s%N`: el `date` de BusyBox que traen las imágenes alpine ignora `%N` y
devuelve solo segundos. La resolución resultante es de 10 ms.

### Una observación sobre el bloqueo de red

El caso que intenta salir a internet termina en tiempo agotado, no con un error
inmediato de red. Con `--network none` la resolución de nombres se queda esperando,
y el `timeout` del socket de Python no cubre esa fase. El egreso está bloqueado, lo
que se comprueba de forma limpia en la prueba 2 de `scripts/probar-aislamiento.sh`;
pero conviene saber que por la API el síntoma es un caso que agota su tiempo.

## Compromisos asumidos

El backend invoca el binario `docker` del anfitrión, así que el proceso de la
aplicación puede crear contenedores. Es cómodo para el kata y es la vía por la que
un atacante que logre ejecución dentro del backend escalaría. En producción esto se
resolvería con un servicio de ejecución aparte, con su propia identidad y sin
acceso al socket de Docker.

El `timeout` por caso mide tiempo de reloj, no de CPU. Un caso que espera en
entrada y salida consume su presupuesto sin usar procesador.
