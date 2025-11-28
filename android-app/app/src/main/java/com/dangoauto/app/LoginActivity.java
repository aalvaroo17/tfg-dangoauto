package com.dangoauto.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    
    private static final String API_BASE_URL = "https://tfg-dangoauto.onrender.com";
    private static final String PREFS_NAME = "DangoAutoPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private TextView textViewError;
    private TextView textViewAuthTitle;
    private TextView textViewToggle;
    private TextView textViewToggleLink;
    private Button btnSubmit;
    
    private boolean isRegisterMode = false;
    private ExecutorService executorService;
    private OkHttpClient httpClient;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Verificar sesión ANTES de cargar la vista
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedUsername = prefs.getString(KEY_USERNAME, null);
            if (savedUsername != null && !savedUsername.isEmpty()) {
                goToMainActivity();
                return;
            }
            
            setContentView(R.layout.activity_login);
            
            // Inicializar cliente HTTP con timeout
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
            executorService = Executors.newSingleThreadExecutor();
            
            // Inicializar vistas con try-catch individual
            try {
                textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
                textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
                textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
                editTextEmail = findViewById(R.id.editTextEmail);
                editTextUsername = findViewById(R.id.editTextUsername);
                editTextPassword = findViewById(R.id.editTextPassword);
                textViewError = findViewById(R.id.textViewError);
                textViewAuthTitle = findViewById(R.id.textViewAuthTitle);
                textViewToggle = findViewById(R.id.textViewToggle);
                textViewToggleLink = findViewById(R.id.textViewToggleLink);
                btnSubmit = findViewById(R.id.btnSubmit);
            } catch (Exception e) {
                android.util.Log.e("LoginActivity", "Error inicializando vistas: " + e.getMessage(), e);
                Toast.makeText(this, "Error cargando interfaz", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Configurar listeners
            if (btnSubmit != null) {
                btnSubmit.setOnClickListener(v -> {
                    try {
                        handleAuth();
                    } catch (Exception e) {
                        android.util.Log.e("LoginActivity", "Error en handleAuth", e);
                        showError("Error al procesar");
                    }
                });
            }
            if (textViewToggleLink != null) {
                textViewToggleLink.setOnClickListener(v -> {
                    try {
                        toggleAuthMode();
                    } catch (Exception e) {
                        android.util.Log.e("LoginActivity", "Error en toggleAuthMode", e);
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error crítico en onCreate", e);
            Toast.makeText(this, "Error al iniciar la aplicación", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void toggleAuthMode() {
        try {
            isRegisterMode = !isRegisterMode;
            
            if (isRegisterMode) {
                if (textViewAuthTitle != null) textViewAuthTitle.setText("Registrarse");
                if (btnSubmit != null) btnSubmit.setText("Registrarse");
                if (textViewToggle != null) textViewToggle.setText("¿Ya tienes cuenta? ");
                if (textViewToggleLink != null) textViewToggleLink.setText("Inicia sesión");
                if (textInputLayoutEmail != null) textInputLayoutEmail.setVisibility(View.VISIBLE);
            } else {
                if (textViewAuthTitle != null) textViewAuthTitle.setText("Iniciar Sesión");
                if (btnSubmit != null) btnSubmit.setText("Iniciar Sesión");
                if (textViewToggle != null) textViewToggle.setText("¿No tienes cuenta? ");
                if (textViewToggleLink != null) textViewToggleLink.setText("Regístrate");
                if (textInputLayoutEmail != null) textInputLayoutEmail.setVisibility(View.GONE);
            }
            
            if (textViewError != null) {
                textViewError.setVisibility(View.GONE);
                textViewError.setText("");
            }
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error en toggleAuthMode", e);
        }
    }
    
    private void handleAuth() {
        try {
            if (editTextUsername == null || editTextPassword == null) {
                showError("Error: Campos no inicializados");
                return;
            }
            
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String email = editTextEmail != null ? editTextEmail.getText().toString().trim() : "";
            
            if (username.isEmpty() || password.isEmpty()) {
                showError("Por favor, completa todos los campos");
                return;
            }
            
            if (isRegisterMode && email.isEmpty()) {
                showError("El email es requerido para registrarse");
                return;
            }
            
            // Deshabilitar botón mientras se procesa
            if (btnSubmit != null) {
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Procesando...");
            }
            
            executorService.execute(() -> {
                try {
                    String endpoint = isRegisterMode ? "/api/auth/register" : "/api/auth/login";
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("username", username);
                    jsonBody.put("password", password);
                    if (isRegisterMode) {
                        jsonBody.put("email", email);
                    }
                    
                    RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                    );
                    
                    String url = API_BASE_URL + endpoint;
                    android.util.Log.d("LoginActivity", "Intentando conectar a: " + url);
                    
                    Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        int statusCode = response.code();
                        android.util.Log.d("LoginActivity", "Respuesta recibida. Status: " + statusCode);
                        
                        String responseBody = response.body() != null ? response.body().string() : "";
                        android.util.Log.d("LoginActivity", "Response body: " + responseBody);
                        
                        if (responseBody.isEmpty()) {
                            runOnUiThread(() -> {
                                if (btnSubmit != null) {
                                    btnSubmit.setEnabled(true);
                                    btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                                }
                                showError("Error: Respuesta vacía del servidor (Status: " + statusCode + ")");
                            });
                            return;
                        }
                        
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        runOnUiThread(() -> {
                            if (btnSubmit != null) {
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                            }
                            
                            if (jsonResponse.optBoolean("success", false)) {
                                JSONObject user = jsonResponse.optJSONObject("user");
                                if (user != null) {
                                    // Guardar sesión
                                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(KEY_USERNAME, user.optString("username"));
                                    editor.putString(KEY_EMAIL, user.optString("email", ""));
                                    editor.apply();
                                    
                                    Toast.makeText(LoginActivity.this, 
                                        isRegisterMode ? "¡Registro exitoso!" : "¡Bienvenido de nuevo!",
                                        Toast.LENGTH_SHORT).show();
                                    
                                    goToMainActivity();
                                }
                            } else {
                                String message = jsonResponse.optString("message", "Error desconocido");
                                showError(message);
                            }
                        });
                    }
                } catch (java.net.SocketTimeoutException e) {
                    runOnUiThread(() -> {
                        if (btnSubmit != null) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                        }
                        showError("Tiempo de espera agotado. Verifica tu conexión a internet.");
                        android.util.Log.e("LoginActivity", "Timeout en handleAuth", e);
                    });
                } catch (java.net.UnknownHostException e) {
                    runOnUiThread(() -> {
                        if (btnSubmit != null) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                        }
                        showError("No se pudo conectar al servidor. Verifica tu conexión.");
                        android.util.Log.e("LoginActivity", "UnknownHostException en handleAuth", e);
                    });
                } catch (java.io.IOException e) {
                    runOnUiThread(() -> {
                        if (btnSubmit != null) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                        }
                        showError("Error de conexión: " + e.getMessage());
                        android.util.Log.e("LoginActivity", "IOException en handleAuth", e);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        if (btnSubmit != null) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                        }
                        showError("Error: " + e.getMessage());
                        android.util.Log.e("LoginActivity", "Error en handleAuth", e);
                    });
                }
            });
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error en handleAuth", e);
            showError("Error al procesar la solicitud");
        }
    }
    
    private void showError(String message) {
        try {
            if (textViewError != null) {
                textViewError.setText(message);
                textViewError.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error en showError", e);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void goToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error yendo a MainActivity", e);
            Toast.makeText(this, "Error al abrir la aplicación", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
