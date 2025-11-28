# Script para compilar el APK de DangoAuto
Write-Host "=== Compilando APK de DangoAuto ===" -ForegroundColor Green
Write-Host ""

# Verificar si existe gradlew.bat
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "⚠️  No se encontró gradlew.bat" -ForegroundColor Yellow
    Write-Host "   Creando Gradle Wrapper..." -ForegroundColor Yellow
    
    # Intentar crear el wrapper
    if (Get-Command gradle -ErrorAction SilentlyContinue) {
        gradle wrapper
    } else {
        Write-Host "❌ Gradle no está instalado" -ForegroundColor Red
        Write-Host ""
        Write-Host "Opciones:" -ForegroundColor Yellow
        Write-Host "1. Instala Android Studio (incluye Gradle)" -ForegroundColor White
        Write-Host "2. O instala Gradle manualmente: https://gradle.org/install/" -ForegroundColor White
        Write-Host ""
        Write-Host "Alternativa: Compila desde Android Studio:" -ForegroundColor Cyan
        Write-Host "  Build > Build Bundle(s) / APK(s) > Build APK(s)" -ForegroundColor White
        exit 1
    }
}

# Compilar APK debug
Write-Host "📦 Compilando APK debug..." -ForegroundColor Cyan
Write-Host ""

try {
    .\gradlew.bat assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            Write-Host ""
            Write-Host "✅ APK compilado exitosamente!" -ForegroundColor Green
            Write-Host "📍 Ubicación: $apkPath" -ForegroundColor Cyan
            Write-Host ""
            
            # Mostrar tamaño del archivo
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Host "📊 Tamaño: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "✓ El APK está listo para descargar desde la web" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Compilación completada pero no se encontró el APK" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ Error durante la compilación" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Alternativa: Compila desde Android Studio" -ForegroundColor Yellow
    Write-Host "   Build > Build Bundle(s) / APK(s) > Build APK(s)" -ForegroundColor White
    exit 1
}

