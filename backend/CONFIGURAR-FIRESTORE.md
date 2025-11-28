# Configurar Firestore para Render.com

## Pasos para Configurar Credenciales

### 1. Obtener Credenciales de Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto: `tfg-front-cb1b2`
3. Ve a **Configuración del proyecto** (ícono de engranaje)
4. En la pestaña **Cuentas de servicio**, haz clic en **Generar nueva clave privada**
5. Se descargará un archivo JSON con las credenciales

### 2. Configurar en Render.com

**Opción A: Variable de Entorno (Recomendado)**

1. Ve a tu servicio en Render.com
2. Ve a **Environment** (Variables de entorno)
3. Agrega una nueva variable:
   - **Key**: `GOOGLE_APPLICATION_CREDENTIALS`
   - **Value**: El contenido completo del archivo JSON descargado (como texto)
4. Guarda los cambios

**Opción B: Archivo de Credenciales**

1. Sube el archivo JSON a tu repositorio (en `backend/` como `firebase-credentials.json`)
2. **IMPORTANTE**: Agrega `firebase-credentials.json` a `.gitignore` para no subirlo a GitHub
3. En Render, el código lo detectará automáticamente

### 3. Habilitar Firestore

1. En Firebase Console, ve a **Firestore Database**
2. Si no está creado, haz clic en **Create database**
3. Selecciona **Start in test mode** (para desarrollo)
4. Elige la región (recomendado: `europe-west` o `us-central`)
5. Espera a que se cree la base de datos

### 4. Estructura de Colecciones

El código creará automáticamente estas colecciones:

- **appointments**: Citas de clientes
  - Campos: `id`, `reference`, `name`, `phone`, `date`, `time`, `status`, `created_at`, etc.

- **users**: Usuarios registrados (para implementar después)
  - Campos: `email`, `name`, `phone`, `created_at`, etc.

## Verificación

Después de configurar, el backend:
- Intentará usar Firestore automáticamente
- Si no hay credenciales, usará JSON como fallback
- Verás en los logs: "✓ Usando Firestore para almacenamiento" o "⚠️ Usando almacenamiento local (JSON)"

## Nota de Seguridad

⚠️ **NUNCA subas el archivo de credenciales a GitHub**
- Agrega `firebase-credentials.json` a `.gitignore`
- Usa variables de entorno en Render.com

