from flask import Flask, request, jsonify, render_template_string
from flask_cors import CORS
import json
import os
from datetime import datetime, timedelta
import re
import uuid

# Firebase Admin SDK
import firebase_admin
from firebase_admin import credentials, firestore

# Inicializar Firebase Admin (solo una vez)
if not firebase_admin._apps:
    # Opción 1: Si GOOGLE_APPLICATION_CREDENTIALS es un archivo JSON
    cred_path = os.environ.get('GOOGLE_APPLICATION_CREDENTIALS')
    if cred_path and os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
        print("✓ Firebase Admin inicializado desde archivo")
    # Opción 2: Si GOOGLE_APPLICATION_CREDENTIALS es el contenido JSON (Render.com)
    elif cred_path and cred_path.strip().startswith('{'):
        try:
            import json
            cred_dict = json.loads(cred_path)
            cred = credentials.Certificate(cred_dict)
            firebase_admin.initialize_app(cred)
            print("✓ Firebase Admin inicializado desde variable de entorno")
        except Exception as e:
            print(f"⚠️ Error parseando credenciales desde variable: {e}")
            try:
                firebase_admin.initialize_app()
            except Exception as e2:
                print(f"⚠️ No se pudo inicializar Firebase Admin: {e2}")
    # Opción 3: Application Default Credentials (Google Cloud)
    else:
        try:
            firebase_admin.initialize_app()
            print("✓ Firebase Admin inicializado con Application Default Credentials")
        except Exception as e:
            print(f"⚠️ Advertencia: No se pudo inicializar Firebase Admin: {e}")
            print("   El backend funcionará en modo local (JSON) hasta configurar credenciales")

# Obtener cliente de Firestore
try:
    db = firestore.client()
    print("✓ Firestore inicializado correctamente")
except Exception as e:
    print(f"⚠️ Advertencia: No se pudo conectar a Firestore: {e}")
    db = None

# Configurar Flask - desactivar carpeta estática por defecto
app = Flask(__name__, static_folder=None)
app.config['SEND_FILE_MAX_AGE_DEFAULT'] = 0  # Desactivar caché para desarrollo

# Configurar CORS para producción (Firebase) y desarrollo local
CORS(app, resources={
    r"/api/*": {
        "origins": [
            "https://tfg-front-cb1b2.web.app",
            "https://tfg-front-cb1b2.firebaseapp.com",
            "https://tfg-front.web.app",
            "https://tfg-front.firebaseapp.com",
            "http://localhost:5000",
            "http://127.0.0.1:5000"
        ],
        "methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        "allow_headers": ["Content-Type", "Authorization"],
        "supports_credentials": True,
        "expose_headers": ["Content-Type"]
    }
})

# Manejar preflight OPTIONS explícitamente
@app.after_request
def after_request(response):
    if request.method == 'OPTIONS':
        response.headers.add('Access-Control-Allow-Origin', request.headers.get('Origin', '*'))
        response.headers.add('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        response.headers.add('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        response.headers.add('Access-Control-Max-Age', '3600')
    return response

class DangoAutoBot:
    def __init__(self, appointments_file='data/citas.json', use_firestore=True):
        """
        Inicializar bot de DangoAuto
        use_firestore: Si True, usa Firestore. Si False, usa archivos JSON (fallback)
        """
        self.use_firestore = use_firestore and db is not None
        self.db = db if self.use_firestore else None
        
        # Fallback a JSON si Firestore no está disponible
        if not self.use_firestore:
            base_dir = os.path.dirname(os.path.abspath(__file__))
            self.appointments_file = os.path.join(base_dir, appointments_file)
            self.appointments = self.load_appointments()
            print("⚠️ Usando almacenamiento local (JSON) - Firestore no disponible")
        else:
            print("✓ Usando Firestore para almacenamiento")

    def load_appointments(self):
        """Cargar citas desde JSON (solo para fallback)"""
        # Asegurar que el directorio existe
        data_dir = os.path.dirname(self.appointments_file)
        if data_dir and not os.path.exists(data_dir):
            os.makedirs(data_dir, exist_ok=True)
            print(f"✓ Directorio creado: {data_dir}")
        
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
            try:
                with open(self.appointments_file, 'w', encoding='utf-8') as f:
                    json.dump([], f)
                print(f"✓ Archivo de citas creado: {self.appointments_file}")
            except Exception as e:
                print(f"Error creando archivo de citas: {e}")
            return []

    def save_appointments(self):
        """Guardar citas en JSON (solo para fallback)"""
        # Asegurar que el directorio existe
        data_dir = os.path.dirname(self.appointments_file)
        if data_dir and not os.path.exists(data_dir):
            os.makedirs(data_dir, exist_ok=True)
        
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
        """Verificar si un horario está disponible"""
        if self.use_firestore:
            try:
                appointments_ref = self.db.collection('appointments')
                # Consulta simplificada: solo buscar citas confirmadas (evita necesidad de índice compuesto)
                # Buscamos citas en esa fecha y hora con status 'confirmada'
                query = appointments_ref.where('date', '==', date_str)\
                                       .where('time', '==', time_str)\
                                       .where('status', '==', 'confirmada')\
                                       .limit(1)
                docs = list(query.stream())
                is_available = len(docs) == 0
                if not is_available:
                    print(f"⚠️ Horario ocupado: {date_str} {time_str} (encontradas {len(docs)} citas)")
                else:
                    print(f"✓ Horario disponible: {date_str} {time_str}")
                return is_available
            except Exception as e:
                print(f"❌ Error verificando disponibilidad en Firestore: {e}")
                import traceback
                traceback.print_exc()
                # Si hay error, asumir que está disponible (más seguro que bloquear todo)
                print(f"⚠️ Asumiendo horario disponible debido a error: {date_str} {time_str}")
                return True
        else:
            for apt in self.appointments:
                if apt['date'] == date_str and apt['time'] == time_str and apt['status'] != 'cancelada':
                    return False
            return True

    def create_appointment(self, name, phone, date_str, time_str, user_id=None):
        """Crear una nueva cita"""
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
        now = datetime.now()
        appointment = {
            "id": appointment_id,
            "reference": reference,
            "name": name.strip().title(),
            "phone": phone.strip(),
            "date": date_str,
            "time": time_str,
            "datetime_full": f"{date_str} {time_str}",
            "status": "confirmada",
            "created_at": now.isoformat(),
            "created_at_timestamp": firestore.SERVER_TIMESTAMP if self.use_firestore else now,
            "notes": "",
            "user_id": user_id  # Para asociar con usuario si está logueado
        }
        
        if self.use_firestore:
            try:
                appointments_ref = self.db.collection('appointments')
                # Preparar datos para Firestore (sin campos None y con timestamp correcto)
                appointment_data = {
                    "id": appointment_id,
                    "reference": reference,
                    "name": name.strip().title(),
                    "phone": phone.strip(),
                    "date": date_str,
                    "time": time_str,
                    "datetime_full": f"{date_str} {time_str}",
                    "status": "confirmada",
                    "created_at": now.isoformat(),
                    "created_at_timestamp": firestore.SERVER_TIMESTAMP,
                    "notes": ""
                }
                # Solo agregar user_id si no es None
                if user_id:
                    appointment_data['user_id'] = user_id
                
                # Guardar en Firestore
                doc_ref = appointments_ref.document()
                doc_ref.set(appointment_data)
                appointment['firestore_id'] = doc_ref.id
                print(f"✓ Cita guardada en Firestore: {reference} (ID: {doc_ref.id})")
                # Preparar respuesta con datos actualizados
                response_data = {
                    "success": True,
                    "message": "¡Cita creada exitosamente!",
                    "appointment": {
                        "id": appointment_id,
                        "reference": reference,
                        "name": name.strip().title(),
                        "phone": phone.strip(),
                        "date": date_str,
                        "time": time_str,
                        "datetime_full": f"{date_str} {time_str}",
                        "status": "confirmada",
                        "created_at": now.isoformat(),
                        "notes": "",
                        "firestore_id": doc_ref.id
                    },
                    "reference": reference
                }
                if user_id:
                    response_data["appointment"]["user_id"] = user_id
                return response_data
            except Exception as e:
                print(f"❌ Error guardando cita en Firestore: {e}")
                import traceback
                traceback.print_exc()
                return {"success": False, "message": f"Error al guardar la cita: {str(e)}", "error_code": "SAVE_ERROR"}
        else:
            # Para JSON, limpiar el diccionario antes de guardar
            appointment_clean = {
                "id": appointment_id,
                "reference": reference,
                "name": name.strip().title(),
                "phone": phone.strip(),
                "date": date_str,
                "time": time_str,
                "datetime_full": f"{date_str} {time_str}",
                "status": "confirmada",
                "created_at": now.isoformat(),
                "notes": ""
            }
            if user_id:
                appointment_clean['user_id'] = user_id
            
            self.appointments.append(appointment_clean)
            if self.save_appointments():
                return {"success": True, "message": "¡Cita creada exitosamente!", "appointment": appointment_clean, "reference": reference}
            else:
                return {"success": False, "message": "Error al guardar la cita", "error_code": "SAVE_ERROR"}

    def get_appointments(self, date_filter=None, user_id=None):
        """Obtener citas, opcionalmente filtradas por fecha o usuario"""
        if self.use_firestore:
            try:
                appointments_ref = self.db.collection('appointments')
                query = appointments_ref
                
                if date_filter:
                    query = query.where('date', '==', date_filter)
                if user_id:
                    query = query.where('user_id', '==', user_id)
                
                docs = query.stream()
                appointments = []
                for doc in docs:
                    apt = doc.to_dict()
                    if apt:  # Verificar que el documento no esté vacío
                        apt['firestore_id'] = doc.id
                        # Convertir timestamp de Firestore a string si existe
                        if 'created_at_timestamp' in apt:
                            timestamp = apt['created_at_timestamp']
                            if hasattr(timestamp, 'isoformat'):
                                apt['created_at_timestamp'] = timestamp.isoformat()
                        appointments.append(apt)
                print(f"✓ Obtenidas {len(appointments)} citas de Firestore")
                return appointments
            except Exception as e:
                print(f"❌ Error obteniendo citas de Firestore: {e}")
                import traceback
                traceback.print_exc()
                return []
        else:
            appointments = self.appointments
            if date_filter:
                appointments = [apt for apt in appointments if apt['date'] == date_filter]
            if user_id:
                appointments = [apt for apt in appointments if apt.get('user_id') == user_id]
            return appointments

    def get_appointment_by_reference(self, reference):
        """Obtener una cita por su referencia"""
        if self.use_firestore:
            try:
                appointments_ref = self.db.collection('appointments')
                query = appointments_ref.where('reference', '==', reference).limit(1)
                docs = list(query.stream())
                if docs:
                    apt = docs[0].to_dict()
                    if apt:  # Verificar que el documento no esté vacío
                        apt['firestore_id'] = docs[0].id
                        # Convertir timestamp si existe
                        if 'created_at_timestamp' in apt:
                            timestamp = apt['created_at_timestamp']
                            if hasattr(timestamp, 'isoformat'):
                                apt['created_at_timestamp'] = timestamp.isoformat()
                        return apt
                return None
            except Exception as e:
                print(f"❌ Error obteniendo cita de Firestore: {e}")
                import traceback
                traceback.print_exc()
                return None
        else:
            for apt in self.appointments:
                if apt['reference'] == reference:
                    return apt
            return None

    def cancel_appointment(self, reference):
        """Cancelar una cita"""
        if self.use_firestore:
            try:
                appointments_ref = self.db.collection('appointments')
                query = appointments_ref.where('reference', '==', reference).limit(1)
                docs = list(query.stream())
                if docs:
                    doc_ref = appointments_ref.document(docs[0].id)
                    update_data = {
                        'status': 'cancelada',
                        'cancelled_at': datetime.now().isoformat(),
                        'cancelled_at_timestamp': firestore.SERVER_TIMESTAMP
                    }
                    doc_ref.update(update_data)
                    print(f"✓ Cita cancelada en Firestore: {reference}")
                    return {"success": True, "message": "Cita cancelada"}
                return {"success": False, "message": "Cita no encontrada"}
            except Exception as e:
                print(f"❌ Error cancelando cita en Firestore: {e}")
                import traceback
                traceback.print_exc()
                return {"success": False, "message": f"Error al cancelar la cita: {str(e)}"}
        else:
            for apt in self.appointments:
                if apt['reference'] == reference:
                    apt['status'] = 'cancelada'
                    apt['cancelled_at'] = datetime.now().isoformat()
                    self.save_appointments()
                    return {"success": True, "message": "Cita cancelada"}
            return {"success": False, "message": "Cita no encontrada"}

    def get_available_slots(self, date_str):
        """Obtener horarios disponibles para una fecha"""
        try:
            target_date = datetime.strptime(date_str, "%Y-%m-%d")
            weekday = target_date.weekday()
            if weekday == 6:
                return []
            elif weekday == 5:
                slots = ["10:00","11:00","12:00","13:00","14:00"]
            else:
                slots = ["09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00"]

            if self.use_firestore:
                try:
                    appointments_ref = self.db.collection('appointments')
                    query = appointments_ref.where('date', '==', date_str)\
                                           .where('status', '!=', 'cancelada')
                    docs = query.stream()
                    occupied = []
                    for doc in docs:
                        apt_data = doc.to_dict()
                        if apt_data and 'time' in apt_data:
                            occupied.append(apt_data['time'])
                except Exception as e:
                    print(f"❌ Error obteniendo citas ocupadas de Firestore: {e}")
                    import traceback
                    traceback.print_exc()
                    occupied = []
            else:
                occupied = [apt['time'] for apt in self.appointments if apt['date'] == date_str and apt['status'] != 'cancelada']
            
            available = [slot for slot in slots if slot not in occupied]

            # Filtrar horarios pasados si es el día de hoy
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

@app.route('/api/appointments', methods=['OPTIONS'])
def api_appointments_options():
    """Manejar peticiones OPTIONS (preflight) para CORS"""
    response = jsonify({})
    response.headers.add('Access-Control-Allow-Origin', request.headers.get('Origin', '*'))
    response.headers.add('Access-Control-Allow-Headers', 'Content-Type, Authorization')
    response.headers.add('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
    response.headers.add('Access-Control-Max-Age', '3600')
    return response

@app.route('/api/appointments', methods=['POST'])
def api_create_appointment():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"success": False, "message": "No se recibieron datos"}), 400
        for key in ['name','phone','date','time']:
            if key not in data:
                return jsonify({"success": False, "message": "Faltan campos", "error_code": "MISSING_FIELDS"}), 400
        
        # Obtener user_id si está disponible (para futura autenticación)
        user_id = data.get('user_id', None)
        
        result = bot.create_appointment(data['name'], data['phone'], data['date'], data['time'], user_id)
        
        # Devolver respuesta con código de estado correcto
        if result.get('success'):
            return jsonify(result), 200
        else:
            return jsonify(result), 400
    except Exception as e:
        print(f"❌ Error en api_create_appointment: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({"success": False, "message": f"Error del servidor: {str(e)}"}), 500

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

@app.route('/download/android-app')
def download_android_app():
    """
    Ruta para descargar la aplicación Android (APK).
    Busca el archivo APK en android-app/app/build/outputs/apk/ y lo sirve.
    """
    from flask import send_file, Response
    
    # Buscar el APK en diferentes ubicaciones posibles
    base_dir = os.path.dirname(os.path.dirname(__file__))
    android_dir = os.path.join(base_dir, 'android-app', 'app', 'build', 'outputs', 'apk')
    
    # Prioridad: release primero, luego debug
    possible_files = [
        os.path.join(android_dir, 'release', 'app-release.apk'),
        os.path.join(android_dir, 'debug', 'app-debug.apk'),
        os.path.join(android_dir, 'debug', 'app-debug.apk'),  # Fallback
    ]
    
    print(f"Buscando APK en: {android_dir}")
    for file_path in possible_files:
        print(f"  - Verificando: {file_path} (existe: {os.path.exists(file_path)})")
        if os.path.exists(file_path):
            try:
                filename = os.path.basename(file_path)
                print(f"  ✓ Encontrado: {filename}")
                
                return send_file(
                    file_path,
                    as_attachment=True,
                    download_name='DangoAuto.apk',
                    mimetype='application/vnd.android.package-archive'
                )
            except Exception as e:
                print(f"Error al servir APK: {e}")
                return Response(
                    f"Error al acceder al APK: {str(e)}",
                    status=500,
                    mimetype='text/plain'
                )
    
    # Si no se encuentra el archivo
    error_msg = (
        "El APK aún no está compilado.\n\n"
        f"Buscado en: {android_dir}\n\n"
        "Para generar el APK:\n"
        "1. Abre Android Studio\n"
        "2. Build > Build Bundle(s) / APK(s) > Build APK(s)\n"
        "O desde terminal: cd android-app && ./gradlew assembleDebug"
    )
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
