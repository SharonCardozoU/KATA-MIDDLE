# Plataforma de Evaluación Técnica

Backend del kata. La pieza que carga el peso arquitectónico es el **motor de
ejecución**: recibe código que no es de confianza, lo corre aislado y lo califica
contra un conjunto de casos de prueba.

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
docker compose up -d
docker ps
```

**2. Imágenes del sandbox** (Python, Node, Java, TypeScript, COBOL)

```bash
sh scripts/preparar-imagenes.sh
```

**3. Backend** — otra terminal, y déjala abierta:

```bash
# si Java/Maven no están en el PATH:
#   source ~/.local/herramientas/entorno.sh
mvn spring-boot:run
```

Espera a ver que quedó escuchando en **8080**.

**4. Frontend** — otra terminal:

```bash
npx ng serve
```

Queda en <http://localhost:4200>. El proxy de Angular reenvía `/api` a `http://localhost:8080`.

### Parar

- Frontend y backend: `Ctrl+C` en cada terminal.
- Postgres: `docker compose down` en la raíz del repo (borra el contenedor; los datos del volumen se conservan).

### Pruebas unitarias

No necesitan Docker ni los puertos 8080/4200. Desde la raíz del repo, en WSL:

**Backend** (`Calificador`, `EjecutorProtegido`, `RegistroEjecutores`):

```bash
cd backend
# si Java/Maven no están en el PATH:
#   source ~/.local/herramientas/entorno.sh
mvn test
```

**Frontend** (portada, rutas, reloj, plantillas, carga y confirmación):

```bash
cd frontend
npx ng test --watch=false
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

