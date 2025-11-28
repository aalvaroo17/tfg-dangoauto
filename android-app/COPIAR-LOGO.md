# Instrucciones para añadir el logo como icono de la app

## Opción 1: Copiar manualmente (Rápido)

1. Copia el archivo `frontend/static/logo.png` a estas carpetas en Android Studio:
   - `android-app/app/src/main/res/mipmap-mdpi/ic_launcher.png` (48x48px)
   - `android-app/app/src/main/res/mipmap-hdpi/ic_launcher.png` (72x72px)
   - `android-app/app/src/main/res/mipmap-xhdpi/ic_launcher.png` (96x96px)
   - `android-app/app/src/main/res/mipmap-xxhdpi/ic_launcher.png` (144x144px)
   - `android-app/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` (192x192px)

2. También para el icono redondo:
   - `android-app/app/src/main/res/mipmap-mdpi/ic_launcher_round.png`
   - `android-app/app/src/main/res/mipmap-hdpi/ic_launcher_round.png`
   - `android-app/app/src/main/res/mipmap-xhdpi/ic_launcher_round.png`
   - `android-app/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png`
   - `android-app/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`

## Opción 2: Usar Android Studio (Recomendado)

1. Abre Android Studio
2. Click derecho en `app` → New → Image Asset
3. Selecciona "Launcher Icons (Adaptive and Legacy)"
4. En "Foreground Layer", selecciona "Image" y busca `frontend/static/logo.png`
5. Ajusta el tamaño y posición
6. En "Background Layer", usa un color oscuro (#0A0A0A) o transparente
7. Click "Next" y luego "Finish"

Esto generará automáticamente todos los tamaños necesarios.

