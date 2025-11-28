// Configuración de la API
// Detectar automáticamente el entorno y usar la URL correcta del backend
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    // Desarrollo local
    window.API_BASE_URL = 'http://localhost:5000';
} else {
    // Producción - URL del backend en Render
    window.API_BASE_URL = 'https://tfg-dangoauto.onrender.com';
}

