# DangoAuto - Sistema de Gestión de Concesionario

Proyecto TFG que integra una aplicación web con backend Python y una aplicación móvil Android.

## 📋 Descripción

DangoAuto es un sistema completo para la gestión de un concesionario de vehículos que incluye:

- **Frontend Web**: Interfaz web moderna con HTML/CSS/JavaScript para mostrar el catálogo de vehículos y gestionar citas
- **Backend Python**: API REST con Flask para procesar formularios y gestionar citas
- **Aplicación Android**: Aplicación móvil desarrollada en Android Studio con Java para búsqueda de vehículos y gestión de citas

## 🗂️ Estructura del Proyecto

```
ProyectoTFG1/
├── frontend/              # Aplicación web frontend
│   ├── index.html        # Página principal
│   ├── app.html          # Aplicación web completa
│   └── static/           # Archivos estáticos (CSS, imágenes, JS)
│       ├── config.js     # Configuración de la API
│       └── Imagenes/     # Imágenes de vehículos
│
├── backend/              # Backend Python (Flask)
│   ├── app.py           # Aplicación principal Flask
│   ├── requirements.txt  # Dependencias Python
│   ├── Procfile         # Configuración para Render.com
│   └── data/            # Datos de la aplicación
│       └── citas.json   # Archivo de citas (generado automáticamente)
│
├── android-app/         # Aplicación Android
│   ├── app/             # Módulo principal de la app
│   │   └── src/main/
│   │       ├── java/    # Código fuente Java
│   │       └── res/     # Recursos (layouts, strings, etc.)
│   └── build.gradle     # Configuración Gradle
│
├── firebase.json        # Configuración Firebase Hosting
└── .firebaserc         # Configuración de proyecto Firebase
```

## 🚀 Requisitos

### Backend Python
- Python 3.8 o superior
- pip (gestor de paquetes Python)

### Aplicación Android
- Android Studio Hedgehog o superior
- Android SDK 24+ (Android 7.0)
- Java 8+

## 📦 Instalación

### 1. Backend Python

```bash
cd backend
pip install -r requirements.txt
```

### 2. Aplicación Android

1. Abre Android Studio
2. File > Open > Selecciona la carpeta `android-app`
3. Espera a que Gradle sincronice las dependencias
4. Ejecuta la aplicación en un emulador o dispositivo físico

## 🏃 Ejecución

### Backend Python (Local)

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
- **Aplicación web**: http://localhost:5000/app
- **API REST**: http://localhost:5000/api/

### Despliegue

#### Frontend (Firebase Hosting)
```bash
firebase deploy --only hosting
```

#### Backend (Render.com)
- Configura el Root Directory como `backend`
- Build Command: `pip install -r requirements.txt`
- Start Command: `gunicorn app:app --bind 0.0.0.0:$PORT`

## 📚 API REST

### Endpoints Disponibles

- `GET /api/appointments` - Obtener todas las citas
- `POST /api/appointments` - Crear una nueva cita
- `GET /api/appointments/<reference>` - Obtener una cita por referencia
- `POST /api/appointments/<reference>/cancel` - Cancelar una cita
- `GET /api/available-slots?date=YYYY-MM-DD` - Obtener horarios disponibles

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
- **Backend**: Python 3, Flask, Flask-CORS, Gunicorn
- **Aplicación Android**: Java, Android SDK, Material Design Components
- **Despliegue**: Firebase Hosting, Render.com

## 📝 Notas de Desarrollo

- El backend usa CORS para permitir peticiones desde el frontend
- Las citas se validan según horarios de negocio (L-V 9:00-18:00, S 10:00-14:00)
- La aplicación Android requiere Android 7.0+ (API 24+)

## 📄 Licencia

Este proyecto es parte de un Trabajo de Fin de Grado (TFG).

## 👤 Autor

Desarrollado como proyecto académico.

---

**Versión**: 2.0.0  
**Última actualización**: 2025
