# Desplegar Backend en Firebase Functions

## Requisitos Previos

1. **Firebase CLI instalado**:
   ```bash
   npm install -g firebase-tools
   ```

2. **Python 3.11** instalado en tu sistema

3. **Autenticado en Firebase**:
   ```bash
   firebase login
   ```

4. **Proyecto Firebase configurado**:
   - El proyecto `tfg-front-cb1b2` debe estar activo
   - Verifica con: `firebase projects:list`

## Pasos para Desplegar

### 1. Habilitar Firebase Functions en tu proyecto

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `tfg-front-cb1b2`
3. Ve a **Functions** en el menú lateral
4. Si es la primera vez, haz clic en **"Get started"**
5. Acepta los términos y habilita la facturación (requerido para Functions)

### 2. Instalar dependencias de Python

Desde la raíz del proyecto:

```bash
cd functions
python -m venv venv

# Windows
venv\Scripts\activate

# Linux/Mac
source venv/bin/activate

pip install -r requirements.txt
```

### 3. Verificar configuración

Asegúrate de que:
- `firebase.json` incluye la sección `functions`
- `.firebaserc` tiene el proyecto correcto
- `functions/requirements.txt` tiene todas las dependencias

### 4. Desplegar Functions

Desde la raíz del proyecto:

```bash
firebase deploy --only functions
```

O para desplegar todo (hosting + functions):

```bash
firebase deploy
```

### 5. Verificar el despliegue

Después del despliegue, verás la URL de tu función:
```
✔  functions[api(us-central1)]: Successful create operation.
Function URL: https://us-central1-tfg-front-cb1b2.cloudfunctions.net/api
```

## Estructura de URLs

Después del despliegue:
- **API Base URL**: `https://us-central1-tfg-front-cb1b2.cloudfunctions.net/api`
- **Endpoints**:
  - `GET /api/appointments`
  - `POST /api/appointments`
  - `GET /api/available-slots?date=YYYY-MM-DD`

## Actualizar Frontend

Una vez desplegado, actualiza `frontend/static/config.js`:

```javascript
window.API_BASE_URL = window.location.hostname === 'localhost' 
    ? 'http://localhost:5000' 
    : 'https://us-central1-tfg-front-cb1b2.cloudfunctions.net';
```

## Probar Localmente

Para probar las functions localmente:

```bash
firebase emulators:start --only functions
```

Esto iniciará un emulador local en `http://localhost:5001`

## Troubleshooting

### Error: "Functions requires a paid plan"
- Necesitas habilitar el plan Blaze (pay-as-you-go) en Firebase
- Las funciones tienen un tier gratuito generoso

### Error: "Python runtime not found"
- Asegúrate de tener Python 3.11 instalado
- Verifica con: `python --version`

### Error: "Module not found"
- Verifica que todas las dependencias estén en `functions/requirements.txt`
- Reinstala: `pip install -r requirements.txt`

