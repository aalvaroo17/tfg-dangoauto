# Generar APK de la Aplicación DangoAuto

## Opción 1: Generar APK desde Android Studio (Más Fácil)

### Pasos:

1. **Abre el proyecto en Android Studio**
   - File > Open > Selecciona `android-app`

2. **Generar APK de Debug (para pruebas)**
   - Build > Build Bundle(s) / APK(s) > Build APK(s)
   - O desde la terminal de Android Studio: `./gradlew assembleDebug`
   - El APK se generará en: `android-app/app/build/outputs/apk/debug/app-debug.apk`

3. **Generar APK de Release (para distribución)**
   - Build > Generate Signed Bundle / APK
   - Selecciona "APK"
   - Si no tienes una keystore, crea una nueva:
     - Clic en "Create new..."
     - Completa los datos (nombre, contraseña, etc.)
     - Guarda la keystore en un lugar seguro
   - Selecciona "release" como build variant
   - Finish
   - El APK se generará en: `android-app/app/build/outputs/apk/release/app-release.apk`

## Opción 2: Generar APK desde la Terminal

### Desde la raíz del proyecto:

```bash
cd android-app
./gradlew assembleDebug
```

O para release (requiere keystore configurado):

```bash
cd android-app
./gradlew assembleRelease
```

## Instalar el APK en tu móvil

### Método 1: USB
1. Conecta tu móvil por USB
2. Activa "Depuración USB" en opciones de desarrollador
3. Copia el APK al móvil
4. Abre el archivo en el móvil y permite "Instalar desde fuentes desconocidas"

### Método 2: Transferencia inalámbrica
1. Sube el APK a Google Drive, Dropbox, etc.
2. Descárgalo desde tu móvil
3. Instálalo (permite "Instalar desde fuentes desconocidas")

### Método 3: QR Code
1. Sube el APK a un servicio como `transfer.sh` o similar
2. Genera un QR code con el enlace
3. Escanea el QR desde tu móvil y descarga

## Nota sobre APK de Release

Para generar un APK de release firmado, necesitas configurar la firma en `app/build.gradle`:

```gradle
android {
    ...
    signingConfigs {
        release {
            storeFile file('path/to/keystore.jks')
            storePassword 'tu-password'
            keyAlias 'tu-alias'
            keyPassword 'tu-password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            ...
        }
    }
}
```

## Ubicación de los APK generados

- **Debug APK**: `android-app/app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `android-app/app/build/outputs/apk/release/app-release.apk`

