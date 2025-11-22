from flask import Flask, request, jsonify, render_template_string
from flask_cors import CORS
import json
import os
from datetime import datetime, timedelta
import re
import uuid

# Configurar Flask - desactivar carpeta estática por defecto
app = Flask(__name__, static_folder=None)
app.config['SEND_FILE_MAX_AGE_DEFAULT'] = 0  # Desactivar caché para desarrollo

# Configurar CORS para producción (Firebase) y desarrollo local
CORS(app, resources={
    r"/api/*": {
        "origins": [
            "https://tfg-front.web.app",
            "https://tfg-front.firebaseapp.com",
            "http://localhost:5000",
            "http://127.0.0.1:5000"
        ],
        "methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})

class DangoAutoBot:
    def __init__(self, appointments_file='data/citas.json'):
        # Obtener la ruta absoluta del archivo de datos
        base_dir = os.path.dirname(os.path.abspath(__file__))
        self.appointments_file = os.path.join(base_dir, appointments_file)
        self.appointments = self.load_appointments()

    def load_appointments(self):
        """Cargar citas desde el archivo JSON"""
        if os.path.exists(self.appointments_file):
            try:
                with open(self.appointments_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    if isinstance(data, list):
                        return data
                    else:
                        return []
            except Exception as e:
                print("Error leyendo citas.json:", e)
                return []
        else:
            # Crear archivo vacío si no existe
            with open(self.appointments_file, 'w', encoding='utf-8') as f:
                json.dump([], f)
            return []

    def save_appointments(self):
        """Guardar citas en citas.json"""
        try:
            with open(self.appointments_file, 'w', encoding='utf-8') as f:
                json.dump(self.appointments, f, indent=2, ensure_ascii=False)
            return True
        except Exception as e:
            print("Error guardando citas:", e)
            return False

    # Validaciones básicas
    def validate_phone(self, phone):
        phone_clean = re.sub(r'[\s\-\(\)]', '', phone)
        pattern = r'^(\+34|0034|34)?[6789]\d{8}$'
        return re.match(pattern, phone_clean) is not None

    def validate_name(self, name):
        return len(name.strip()) >= 2 and name.strip().replace(' ','').isalpha()

    def validate_date_time(self, date_str, time_str):
        try:
            appointment_datetime = datetime.strptime(f"{date_str} {time_str}", "%Y-%m-%d %H:%M")
            if appointment_datetime <= datetime.now():
                return False, "La fecha y hora deben ser futuras"
            if appointment_datetime > datetime.now() + timedelta(days=90):
                return False, "No se pueden programar citas a más de 3 meses"
            weekday = appointment_datetime.weekday()
            hour = appointment_datetime.hour
            if weekday == 6:
                return False, "No trabajamos los domingos"
            if weekday == 5 and (hour < 10 or hour > 14):
                return False, "Sábados solo de 10:00 a 14:00"
            if weekday < 5 and (hour < 9 or hour > 18):
                return False, "Horario de atención: Lunes a Viernes 9:00-18:00"
            return True, "Horario válido"
        except ValueError:
            return False, "Formato de fecha u hora inválido"

    def is_slot_available(self, date_str, time_str):
        for apt in self.appointments:
            if apt['date'] == date_str and apt['time'] == time_str and apt['status'] != 'cancelada':
                return False
        return True

    def create_appointment(self, name, phone, date_str, time_str):
        if not self.validate_name(name):
            return {"success": False, "message": "Nombre inválido", "error_code": "INVALID_NAME"}
        if not self.validate_phone(phone):
            return {"success": False, "message": "Teléfono inválido", "error_code": "INVALID_PHONE"}
        date_valid, msg = self.validate_date_time(date_str, time_str)
        if not date_valid:
            return {"success": False, "message": msg, "error_code": "INVALID_DATETIME"}
        if not self.is_slot_available(date_str, time_str):
            return {"success": False, "message": "Horario ocupado", "error_code": "SLOT_UNAVAILABLE"}

        appointment_id = str(uuid.uuid4())[:8].upper()
        reference = f"DANGO{appointment_id}"
        appointment = {
            "id": appointment_id,
            "reference": reference,
            "name": name.strip().title(),
            "phone": phone.strip(),
            "date": date_str,
            "time": time_str,
            "datetime_full": f"{date_str} {time_str}",
            "status": "confirmada",
            "created_at": datetime.now().isoformat(),
            "notes": ""
        }
        self.appointments.append(appointment)
        if self.save_appointments():
            return {"success": True, "message": "¡Cita creada exitosamente!", "appointment": appointment, "reference": reference}
        else:
            return {"success": False, "message": "Error al guardar la cita", "error_code": "SAVE_ERROR"}

    def get_appointments(self, date_filter=None):
        if date_filter:
            return [apt for apt in self.appointments if apt['date'] == date_filter]
        return self.appointments

    def get_appointment_by_reference(self, reference):
        for apt in self.appointments:
            if apt['reference'] == reference:
                return apt
        return None

    def cancel_appointment(self, reference):
        for apt in self.appointments:
            if apt['reference'] == reference:
                apt['status'] = 'cancelada'
                apt['cancelled_at'] = datetime.now().isoformat()
                self.save_appointments()
                return {"success": True, "message": "Cita cancelada"}
        return {"success": False, "message": "Cita no encontrada"}

    def get_available_slots(self, date_str):
        try:
            target_date = datetime.strptime(date_str, "%Y-%m-%d")
            weekday = target_date.weekday()
            if weekday == 6:
                return []
            elif weekday == 5:
                slots = ["10:00","11:00","12:00","13:00","14:00"]
            else:
                slots = ["09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00"]

            occupied = [apt['time'] for apt in self.appointments if apt['date'] == date_str and apt['status'] != 'cancelada']
            available = [slot for slot in slots if slot not in occupied]

            if target_date.date() == datetime.now().date():
                current_hour = datetime.now().hour
                available = [s for s in available if int(s.split(':')[0]) > current_hour]

            return available
        except ValueError:
            return []

# Inicializar bot
bot = DangoAutoBot()

# -------------------- Rutas API --------------------
@app.route('/api/appointments', methods=['GET'])
def api_get_appointments():
    date_filter = request.args.get('date')
    appointments = bot.get_appointments(date_filter)
    return jsonify({"success": True, "appointments": appointments, "total": len(appointments)})

@app.route('/api/appointments', methods=['POST'])
def api_create_appointment():
    data = request.get_json()
    for key in ['name','phone','date','time']:
        if key not in data:
            return jsonify({"success": False, "message": "Faltan campos", "error_code": "MISSING_FIELDS"}), 400
    result = bot.create_appointment(data['name'], data['phone'], data['date'], data['time'])
    return jsonify(result), 200 if result['success'] else 400

@app.route('/api/appointments/<reference>', methods=['GET'])
def api_get_appointment(reference):
    apt = bot.get_appointment_by_reference(reference)
    if apt:
        return jsonify({"success": True, "appointment": apt})
    else:
        return jsonify({"success": False, "message": "Cita no encontrada"}), 404

@app.route('/api/appointments/<reference>/cancel', methods=['POST'])
def api_cancel_appointment(reference):
    result = bot.cancel_appointment(reference)
    return jsonify(result), 200 if result['success'] else 404

@app.route('/api/available-slots', methods=['GET'])
def api_available_slots():
    date_str = request.args.get('date')
    if not date_str:
        return jsonify({"success": False, "message": "Fecha requerida"}), 400
    slots = bot.get_available_slots(date_str)
    return jsonify({"success": True, "date": date_str, "slots": slots, "total_available": len(slots)})

# -------------------- Servir HTML --------------------
@app.route('/')
def home():
    """Servir la página principal"""
    html_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'frontend', 'index.html')
    with open(html_path, "r", encoding="utf-8") as f:
        html_content = f.read()
    return render_template_string(html_content)

@app.route('/app')
def web_app():
    """Servir la aplicación web (versión navegador)"""
    html_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'frontend', 'app.html')
    with open(html_path, "r", encoding="utf-8") as f:
        html_content = f.read()
    return render_template_string(html_content)

@app.route('/static/<path:filename>')
def serve_static(filename):
    """Servir archivos estáticos"""
    from flask import send_from_directory, abort
    static_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'frontend', 'static')
    
    # Normalizar la ruta para Windows
    static_dir = os.path.normpath(static_dir)
    file_path = os.path.normpath(os.path.join(static_dir, filename))
    
    # Seguridad: asegurar que el archivo esté dentro de static_dir
    if not file_path.startswith(static_dir):
        print(f"⚠️ Intento de acceso fuera del directorio estático: {filename}")
        abort(403)
    
    # Debug: verificar que el archivo existe
    if not os.path.exists(file_path):
        print(f"⚠️ Archivo estático no encontrado: {file_path}")
        print(f"   Buscado en: {static_dir}")
        print(f"   Filename recibido: {filename}")
        print(f"   Ruta completa: {file_path}")
        abort(404)
    
    print(f"✓ Sirviendo archivo estático: {filename}")
    return send_from_directory(static_dir, filename)

@app.route('/download/java-app')
def download_java_app():
    """
    Ruta para descargar la aplicación Java.
    Busca el archivo JAR o EXE en la carpeta dist/ y lo sirve con headers correctos.
    """
    from flask import send_file, Response
    
    # Buscar el archivo en dist/ (priorizar .exe, luego .zip, luego .jar)
    dist_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'dist')
    possible_files = [
        os.path.join(dist_dir, 'DangoAuto.exe'),  # Prioridad: .exe primero
        os.path.join(dist_dir, 'DangoAuto.zip'),  # ZIP con JAR + script
        os.path.join(dist_dir, 'DangoAuto.jar'),
        os.path.join(dist_dir, 'dangoauto-app-1.0.0.jar')
    ]
    
    print(f"Buscando archivo en: {dist_dir}")
    for file_path in possible_files:
        print(f"  - Verificando: {file_path} (existe: {os.path.exists(file_path)})")
        if os.path.exists(file_path):
            try:
                filename = os.path.basename(file_path)
                print(f"  ✓ Encontrado: {filename}")
                # Nombre del archivo para descarga
                if filename.endswith('.exe'):
                    download_filename = 'DangoAuto.exe'
                    mimetype = 'application/x-msdownload'
                elif filename.endswith('.zip'):
                    download_filename = 'DangoAuto.zip'
                    mimetype = 'application/zip'
                else:
                    download_filename = 'DangoAuto.jar'
                    mimetype = 'application/java-archive'
                
                return send_file(
                    file_path,
                    as_attachment=True,
                    download_name=download_filename,
                    mimetype=mimetype
                )
            except Exception as e:
                print(f"Error al servir archivo: {e}")
                return Response(
                    f"Error al acceder al archivo: {str(e)}",
                    status=500,
                    mimetype='text/plain'
                )
    
    # Si no se encuentra el archivo
    error_msg = f"La aplicación Java aún no está compilada.\n\nBuscado en: {dist_dir}\n\nEjecuta 'build.bat' en la raíz del proyecto para generar el JAR."
    return Response(error_msg, status=404, mimetype='text/plain')

if __name__ == '__main__':
    import os
    port = int(os.environ.get('PORT', 5000))
    print("=" * 50)
    print("DangoAuto - Sistema de Gestión de Citas")
    print("=" * 50)
    print(f"Servidor iniciado en: http://localhost:{port}")
    print(f"API disponible en: http://localhost:{port}/api/")
    print(f"Archivo de datos: {bot.appointments_file}")
    print("=" * 50)
    # En producción (Render/Cloud Run) usar gunicorn, en desarrollo usar Flask
    if os.environ.get('FLASK_ENV') == 'production':
        # gunicorn se encargará de servir la app
        pass
    else:
        app.run(debug=True, host='0.0.0.0', port=port)
