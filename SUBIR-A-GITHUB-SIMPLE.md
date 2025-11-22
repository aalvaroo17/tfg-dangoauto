# Cómo Subir el Proyecto a GitHub

## Problema
GitHub web NO permite subir carpetas directamente, solo archivos individuales.

## Soluciones

### Opción 1: GitHub Desktop (MÁS FÁCIL) ⭐

1. Descarga: https://desktop.github.com/
2. Instala e inicia sesión con tu cuenta
3. File > Add Local Repository
4. Selecciona: `C:\Users\Alvaro\Desktop\ProyectoTFG1`
5. Verás todos los archivos listos para commit
6. Escribe un mensaje: "Initial commit - DangoAuto TFG"
7. Haz clic en "Commit to main"
8. Publish repository (o Push origin si ya existe)
9. Nombre: `tfg-alvaro` (o el que prefieras)

### Opción 2: Comandos Git (Desde PowerShell)

Abre PowerShell en la carpeta del proyecto:

```powershell
cd C:\Users\Alvaro\Desktop\ProyectoTFG1

# Inicializar Git (si no está)
git init

# Configurar
git config user.name "Alvaro Ramirez"
git config user.email "alvaroramirez19loranca@gmail.com"

# Agregar todo
git add .

# Commit
git commit -m "Initial commit - DangoAuto TFG Project"

# Conectar con GitHub (reemplaza TU_REPO con tu URL)
git remote add origin https://github.com/TU_USUARIO/TU_REPO.git

# Subir
git branch -M main
git push -u origin main
```

### Opción 3: Crear Repo Vacío y Clonar

1. Crea repositorio vacío en GitHub (sin README)
2. Copia la URL (ej: `https://github.com/aalvaroo17/tfg-alvaro.git`)
3. En PowerShell:

```powershell
cd C:\Users\Alvaro\Desktop
git clone https://github.com/aalvaroo17/tfg-alvaro.git
# Copia todos los archivos de ProyectoTFG1 a tfg-alvaro
# Luego:
cd tfg-alvaro
git add .
git commit -m "Initial commit"
git push
```

## Recomendación

**Usa GitHub Desktop** - Es la forma más fácil y visual.

