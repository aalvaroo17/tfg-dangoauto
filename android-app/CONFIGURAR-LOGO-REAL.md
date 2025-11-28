# Configurar el Logo Real de la App

## Instrucciones para usar logo.png como icono

El logo actual es un placeholder. Para usar tu `logo.png` real:

### Opción 1: Desde Android Studio (Recomendado - Automático)

1. **Abre Android Studio**
2. **Click derecho en `app`** → **New** → **Image Asset**
3. En la ventana que se abre:
   - **Icon Type**: Selecciona "Launcher Icons (Adaptive and Legacy)"
   - **Foreground Layer**:
     - Selecciona "Image"
     - Click en el icono de carpeta y busca: `frontend/static/logo.png`
     - Ajusta el tamaño si es necesario (resize)
   - **Background Layer**:
     - Selecciona "Color"
     - Usa color oscuro: `#0A0A0A` (o transparente si el logo ya tiene fondo)
   - **Resize**: Ajusta si es necesario para que el logo se vea bien
4. Click **Next** → **Finish**

Esto generará automáticamente todos los tamaños necesarios en las carpetas `mipmap-*`.

### Opción 2: Copiar manualmente (Requiere redimensionar)

Si prefieres hacerlo manualmente, necesitas redimensionar el logo a estos tamaños:

- `mipmap-mdpi/ic_launcher.png`: 48x48px
- `mipmap-hdpi/ic_launcher.png`: 72x72px  
- `mipmap-xhdpi/ic_launcher.png`: 96x96px
- `mipmap-xxhdpi/ic_launcher.png`: 144x144px
- `mipmap-xxxhdpi/ic_launcher.png`: 192x192px

Y lo mismo para `ic_launcher_round.png`.

**Recomendación**: Usa la Opción 1 (Android Studio) ya que es más fácil y genera todo automáticamente.

