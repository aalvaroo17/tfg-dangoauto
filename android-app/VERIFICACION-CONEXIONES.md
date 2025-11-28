# Verificación de Conexiones - Todo Listo ✅

## ✅ Backend (Render.com)
- **URL**: `https://tfg-dangoauto.onrender.com`
- **Endpoints de autenticación**:
  - `POST /api/auth/register` - Registro de usuarios
  - `POST /api/auth/login` - Inicio de sesión
- **Almacenamiento**: Firestore (colección `users`)
- **CORS**: Configurado para web y Android

## ✅ Frontend Web
- **index.html**: Sistema de login/registro conectado con API
- **app.html**: Sistema de login/registro conectado con API
- **Fallback**: localStorage si API no disponible
- **Sincronización**: Guarda en Firestore y localStorage

## ✅ App Android
- **LoginActivity**: Pantalla de login/registro
- **MainActivity**: Verifica sesión al iniciar
- **API URL**: `https://tfg-dangoauto.onrender.com`
- **Almacenamiento**: SharedPreferences (local) + Firestore (servidor)
- **Dependencias**: OkHttp para peticiones HTTP
- **Permisos**: Internet configurado en manifest

## ✅ Flujo de Autenticación

1. **Registro Web** → Guarda en Firestore → Puede iniciar sesión en Android
2. **Registro Android** → Guarda en Firestore → Puede iniciar sesión en Web
3. **Login Web** → Verifica en Firestore → Sincroniza con localStorage
4. **Login Android** → Verifica en Firestore → Guarda en SharedPreferences

## ✅ Todo está conectado y listo para probar

