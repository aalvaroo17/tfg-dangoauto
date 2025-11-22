# DangoAuto - Sistema de Gestión de Concesionario

Proyecto TFG que integra una aplicación web con backend Python y una aplicación de escritorio en Java.

## 📋 Descripción

DangoAuto es un sistema completo para la gestión de un concesionario de vehículos que incluye:

- **Frontend Web**: Interfaz web moderna con HTML/CSS/JavaScript para mostrar el catálogo de vehículos y gestionar citas
- **Backend Python**: API REST con Flask para procesar formularios y gestionar citas
- **Aplicación Java**: Aplicación de escritorio JavaFX para búsqueda de vehículos y gestión de citas

## 🗂️ Estructura del Proyecto

```
ProyectoTFG1/
├── frontend/              # Aplicación web frontend
│   ├── index.html        # Página principal
│   └── static/           # Archivos estáticos (CSS, imágenes, JS)
│       └── Imagenes/     # Imágenes de vehículos
│
├── backend/              # Backend Python (Flask)
│   ├── app.py           # Aplicación principal Flask
│   ├── requirements.txt  # Dependencias Python
│   └── data/            # Datos de la aplicación
│       └── citas.json   # Archivo de citas (generado automáticamente)
│
├── java-app/            # Aplicación Java
│   ├── pom.xml         # Configuración Maven
│   └── src/            # Código fuente Java
│       └── main/
│           ├── java/   # Código Java
│           └── resources/ # Recursos (FXML)
│
├── dist/               # Archivos compilados para distribución
│   └── DangoAuto.jar   # JAR ejecutable (generado)
│
└── docs/               # Documentación adicional
```

## 🚀 Requisitos

### Backend Python
- Python 3.8 o superior
- pip (gestor de paquetes Python)

### Aplicación Java
- Java 8 o superior
- Maven 3.6 o superior

**Instalación de Maven (Windows):**
```bash
winget install Apache.Maven
```

O descarga manual desde: https://maven.apache.org/download.cgi

## 📦 Instalación

### 1. Backend Python

```bash
cd backend
pip install -r requirements.txt
```

### 2. Aplicación Java

No requiere instalación adicional. Maven descargará las dependencias automáticamente.

## 🏃 Ejecución

### Backend Python

Desde la raíz del proyecto:

```bash
python backend/app.py
```

O desde la carpeta backend:

```bash
cd backend
python app.py
```

El servidor se iniciará en `http://localhost:5000`

### Aplicación Web

Una vez iniciado el backend, accede a:
- **Página principal**: http://localhost:5000
- **API REST**: http://localhost:5000/api/

### Compilar Aplicación Java

**Opción 1: Usando el script de build (recomendado)**

Desde la raíz del proyecto:

```bash
# Windows (PowerShell)
.\build.bat

# Windows (CMD)
build.bat

# Linux/Mac
chmod +x build.sh
./build.sh
```

**Opción 2: Manualmente**

Desde la carpeta `java-app`:

```bash
cd java-app
mvn clean package
```

El JAR ejecutable se generará en `java-app/target/dangoauto-app-1.0.0.jar`

**Para ejecutar la aplicación Java:**

```bash
java -jar java-app/target/dangoauto-app-1.0.0.jar
```

O si usaste el script de build:

```bash
java -jar dist/DangoAuto.jar
```

### Generar JAR para Distribución

Después de compilar, copia el JAR a la carpeta `dist`:

```bash
# Windows
copy java-app\target\dangoauto-app-1.0.0.jar dist\DangoAuto.jar

# Linux/Mac
cp java-app/target/dangoauto-app-1.0.0.jar dist/DangoAuto.jar
```

## 📥 Descarga de la Aplicación Java

La aplicación Java se puede descargar desde la web:

1. Inicia el servidor backend: `python backend/app.py`
2. Accede a la página web: http://localhost:5000
3. Haz clic en el botón "Descargar Aplicación Java" en la sección de descarga
4. El archivo JAR se descargará automáticamente

**Nota**: El archivo debe estar en `dist/DangoAuto.jar` para que la descarga funcione.

## 🔧 Configuración

### Archivo de Citas

Las citas se guardan automáticamente en `backend/data/citas.json`. Este archivo se crea automáticamente si no existe.

### Rutas de Imágenes

Las imágenes de los vehículos deben estar en `frontend/static/Imagenes/`. La aplicación Java busca estas imágenes en rutas relativas.

## 📚 API REST

### Endpoints Disponibles

- `GET /api/appointments` - Obtener todas las citas
- `POST /api/appointments` - Crear una nueva cita
- `GET /api/appointments/<reference>` - Obtener una cita por referencia
- `POST /api/appointments/<reference>/cancel` - Cancelar una cita
- `GET /api/available-slots?date=YYYY-MM-DD` - Obtener horarios disponibles
- `GET /download/java-app` - Descargar aplicación Java

### Ejemplo de Creación de Cita

```json
POST /api/appointments
Content-Type: application/json

{
    "name": "Juan Pérez",
    "phone": "612345678",
    "date": "2025-12-01",
    "time": "10:00"
}
```

## 🛠️ Tecnologías Utilizadas

- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Backend**: Python 3, Flask, Flask-CORS
- **Aplicación Java**: Java 8, JavaFX, Jackson (JSON)
- **Build Tool**: Maven

## 📝 Notas de Desarrollo

- El proyecto está configurado para Java 8 para máxima compatibilidad
- La aplicación JavaFX requiere que JavaFX esté disponible (incluido en Java 8)
- El backend usa CORS para permitir peticiones desde el frontend
- Las citas se validan según horarios de negocio (L-V 9:00-18:00, S 10:00-14:00)

## 📄 Licencia

Este proyecto es parte de un Trabajo de Fin de Grado (TFG).

## 👤 Autor

Desarrollado como proyecto académico.

---

**Versión**: 1.0.0  
**Última actualización**: 2025

