"""
Firebase Functions - Backend DangoAuto
Adaptado para funcionar con Firebase Functions HTTP
"""
from firebase_functions import https_fn, options
from firebase_admin import initialize_app
import sys
import os
import json

# Agregar el directorio backend al path para importar módulos
backend_path = os.path.join(os.path.dirname(__file__), '..', 'backend')
sys.path.insert(0, os.path.abspath(backend_path))

# Inicializar Firebase Admin (necesario para Firestore más adelante)
initialize_app()

# Importar el bot y las funciones del backend
from app import DangoAutoBot

# Inicializar el bot (más adelante usará Firestore)
bot = DangoAutoBot()

# Crear función HTTP para la API
@https_fn.on_request(
    cors=options.CorsOptions(
        cors_origins=[
            "https://tfg-front-cb1b2.web.app",
            "https://tfg-front-cb1b2.firebaseapp.com",
            "https://tfg-front.web.app",
            "https://tfg-front.firebaseapp.com",
            "http://localhost:5000",
            "http://127.0.0.1:5000"
        ],
        cors_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        cors_allow_headers=["Content-Type", "Authorization"],
        cors_max_age=3600
    )
)
def api(req: https_fn.Request) -> https_fn.Response:
    """
    Función HTTP que maneja todas las peticiones del backend
    """
    path = req.path
    method = req.method
    
    # Manejar rutas de la API
    if path == '/api/appointments' or path.startswith('/api/appointments'):
        if method == 'OPTIONS':
            # Preflight CORS
            return https_fn.Response(
                "",
                status=200,
                headers={
                    "Access-Control-Allow-Origin": "*",
                    "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
                    "Access-Control-Allow-Headers": "Content-Type, Authorization"
                }
            )
        elif method == 'GET':
            # Obtener citas
            date_filter = req.args.get('date')
            appointments = bot.get_appointments(date_filter)
            return https_fn.Response(
                json.dumps({"success": True, "appointments": appointments, "total": len(appointments)}),
                status=200,
                headers={"Content-Type": "application/json"}
            )
        elif method == 'POST':
            # Crear nueva cita
            data = req.get_json(silent=True)
            if not data:
                return https_fn.Response(
                    json.dumps({"success": False, "message": "No se recibieron datos"}),
                    status=400,
                    headers={"Content-Type": "application/json"}
                )
            for key in ['name', 'phone', 'date', 'time']:
                if key not in data:
                    return https_fn.Response(
                        json.dumps({"success": False, "message": "Faltan campos", "error_code": "MISSING_FIELDS"}),
                        status=400,
                        headers={"Content-Type": "application/json"}
                    )
            result = bot.create_appointment(
                data['name'],
                data['phone'],
                data['date'],
                data['time']
            )
            status_code = 200 if result.get('success') else 400
            return https_fn.Response(
                json.dumps(result),
                status=status_code,
                headers={"Content-Type": "application/json"}
            )
        elif path.startswith('/api/appointments/') and method == 'GET':
            # Obtener cita por referencia
            reference = path.split('/')[-1]
            apt = bot.get_appointment_by_reference(reference)
            if apt:
                return https_fn.Response(
                    json.dumps({"success": True, "appointment": apt}),
                    status=200,
                    headers={"Content-Type": "application/json"}
                )
            else:
                return https_fn.Response(
                    json.dumps({"success": False, "message": "Cita no encontrada"}),
                    status=404,
                    headers={"Content-Type": "application/json"}
                )
        elif path.endswith('/cancel') and method == 'POST':
            # Cancelar cita
            reference = path.split('/')[-2]
            result = bot.cancel_appointment(reference)
            status_code = 200 if result.get('success') else 404
            return https_fn.Response(
                json.dumps(result),
                status=status_code,
                headers={"Content-Type": "application/json"}
            )
    
    elif path == '/api/available-slots' and method == 'GET':
        date_str = req.args.get('date')
        if not date_str:
            return https_fn.Response(
                json.dumps({"success": False, "message": "Fecha requerida"}),
                status=400,
                headers={"Content-Type": "application/json"}
            )
        slots = bot.get_available_slots(date_str)
        return https_fn.Response(
            json.dumps({"success": True, "date": date_str, "slots": slots, "total_available": len(slots)}),
            status=200,
            headers={"Content-Type": "application/json"}
        )
    
    # Ruta no encontrada
    return https_fn.Response(
        json.dumps({"success": False, "message": "Ruta no encontrada"}),
        status=404,
        headers={"Content-Type": "application/json"}
    )

