# Pasos Detallados - GitHub Desktop

## Paso 1: Agregar Repositorio Local

1. En GitHub Desktop, haz clic en **"File"** (arriba a la izquierda)
2. Selecciona **"Add local repository..."** (o presiona `Ctrl+O`)
3. En la ventana que se abre:
   - Haz clic en **"Choose..."** o **"Browse..."**
   - Navega a: `C:\Users\Alvaro\Desktop\ProyectoTFG1`
   - Selecciona la carpeta `ProyectoTFG1`
   - Haz clic en **"Select Folder"** o **"Abrir"**

## Paso 2: Verificar Archivos

GitHub Desktop mostrará:
- A la izquierda: lista de archivos nuevos/modificados
- Abajo: área para escribir el mensaje de commit

## Paso 3: Hacer Commit

1. En la parte inferior, verás un campo de texto
2. Escribe: `Initial commit - DangoAuto TFG Project`
3. Haz clic en el botón **"Commit to main"** (botón azul, abajo a la izquierda)

## Paso 4: Publicar en GitHub

1. Después del commit, verás un botón **"Publish repository"** arriba
2. Haz clic en **"Publish repository"**
3. Se abrirá una ventana:
   - **Name**: `tfg-alvaro` (o el nombre que prefieras)
   - **Description**: (opcional) "TFG - DangoAuto Concesionario"
   - **Keep this code private**: (opcional) Marca si quieres privado
4. Haz clic en **"Publish repository"**

## Paso 5: Verificar

Una vez publicado, verás:
- El repositorio conectado
- Un botón **"View on GitHub"** - haz clic para verlo en la web

## Si No Ves "Add local repository"

Si no aparece la opción:
1. Ve a **"File"** > **"Options"** (o `Ctrl+,`)
2. Verifica que estés logueado con tu cuenta
3. Si no, haz clic en **"Sign in"** y usa tu cuenta de GitHub

## Solución de Problemas

### "This directory does not appear to be a Git repository"
- Haz clic en **"create a repository"** en la misma ventana
- O ejecuta `git init` en PowerShell primero

### No veo los archivos
- Asegúrate de haber seleccionado la carpeta correcta
- Verifica que la carpeta contenga `frontend/`, `backend/`, etc.

