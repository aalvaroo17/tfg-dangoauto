# Estructura del Proyecto DangoAuto

## Descripción General

Este proyecto está organizado siguiendo las mejores prácticas de desarrollo, separando claramente el frontend, backend y la aplicación Java.

## Estructura de Carpetas

```
ProyectoTFG1/
│
├── frontend/                    # Aplicación web frontend
│   ├── index.html              # Página principal HTML
│   └── static/                 # Archivos estáticos
│       ├── Imagenes/           # Imágenes de vehículos
│       │   ├── ImagenBMW.jpg
│       │   ├── ImagenAudi.webp
│       │   ├── ImagenMercedes.webp
│       │   ├── ImagenGolf.jpeg
│       │   ├── ImagenToyota.webp
│       │   └── ImagenFord.jpg
│       └── descargas/          # Carpeta para descargas (opcional)
│
├── backend/                     # Backend Python (Flask)
│   ├── app.py                  # Aplicación principal Flask
│   ├── requirements.txt         # Dependencias Python
│   └── data/                   # Datos de la aplicación
│       └── citas.json          # Archivo de citas (generado automáticamente)
│
├── java-app/                   # Aplicación Java
│   ├── pom.xml                # Configuración Maven
│   ├── .gitignore             # Archivos a ignorar en Git
│   └── src/                   # Código fuente
│       └── main/
│           ├── java/          # Código Java
│           │   └── apptfg1/
│           │       ├── Main.java
│           │       ├── controller/    # Controladores JavaFX
│           │       ├── model/         # Modelos de datos
│           │       └── service/       # Servicios de negocio
│           └── resources/     # Recursos (FXML, imágenes)
│               ├── login.fxml
│               └── main.fxml
│
├── dist/                      # Archivos compilados para distribución
│   └── DangoAuto.jar          # JAR ejecutable (generado después de compilar)
│
├── docs/                      # Documentación
│   └── ESTRUCTURA.md         # Este archivo
│
└── README.md                  # Documentación principal del proyecto
```

## Flujo de Datos

### Frontend → Backend
- El formulario HTML envía peticiones AJAX a `/api/appointments`
- El backend procesa y guarda en `backend/data/citas.json`

### Aplicación Java
- Lee `backend/data/citas.json` para mostrar citas del usuario
- Busca imágenes en `frontend/static/Imagenes/`

### Descarga de Aplicación
- El backend sirve el JAR desde `dist/DangoAuto.jar` mediante `/download/java-app`
- El frontend muestra un botón que enlaza a esta ruta

## Archivos Importantes

### Backend
- `backend/app.py`: Servidor Flask principal
- `backend/data/citas.json`: Base de datos de citas (JSON)

### Frontend
- `frontend/index.html`: Página web completa (HTML, CSS, JS inline)

### Java
- `java-app/pom.xml`: Configuración Maven
- `java-app/src/main/java/apptfg1/Main.java`: Punto de entrada de la aplicación

## Compilación y Distribución

1. **Backend**: No requiere compilación, solo ejecutar `python backend/app.py`
2. **Java**: Compilar con `mvn clean package` en `java-app/`
3. **Distribución**: Copiar JAR a `dist/` para que esté disponible para descarga

