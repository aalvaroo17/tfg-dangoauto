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
import java.io.IOException;
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
            // Establecer el tema antes de setContentView
            setTheme(R.style.Theme_DangoAuto);
            setContentView(R.layout.activity_login);
            
            // Inicializar cliente HTTP
            httpClient = new OkHttpClient();
            executorService = Executors.newSingleThreadExecutor();
            
            // Inicializar vistas con verificaciones
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
                Toast.makeText(this, "Error inicializando interfaz", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Verificar si ya hay sesión guardada
            try {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String savedUsername = prefs.getString(KEY_USERNAME, null);
                if (savedUsername != null && !savedUsername.isEmpty()) {
                    // Ya hay sesión, ir directamente a MainActivity
                    goToMainActivity();
                    return;
                }
            } catch (Exception e) {
                android.util.Log.e("LoginActivity", "Error verificando sesión: " + e.getMessage(), e);
                // Continuar con el login normal
            }
            
            // Configurar listeners
            if (btnSubmit != null) {
                btnSubmit.setOnClickListener(v -> {
                    try {
                        handleAuth();
                    } catch (Exception e) {
                        android.util.Log.e("LoginActivity", "Error en handleAuth: " + e.getMessage(), e);
                        showError("Error al procesar la solicitud");
                    }
                });
            }
            if (textViewToggleLink != null) {
                textViewToggleLink.setOnClickListener(v -> {
                    try {
                        toggleAuthMode();
                    } catch (Exception e) {
                        android.util.Log.e("LoginActivity", "Error en toggleAuthMode: " + e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            // Si hay un error crítico, mostrar mensaje y cerrar
            android.util.Log.e("LoginActivity", "Error crítico en onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar la aplicación. Por favor, reinstala la app.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    
    private void toggleAuthMode() {
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
    }
    
    private void handleAuth() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, completa todos los campos");
            return;
        }
        
        if (isRegisterMode && email.isEmpty()) {
            showError("El email es requerido para registrarse");
            return;
        }
        
        // Deshabilitar botón mientras se procesa
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Procesando...");
        
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
                
                Request request = new Request.Builder()
                    .url(API_BASE_URL + endpoint)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                        
                        if (jsonResponse.optBoolean("success", false)) {
                            JSONObject user = jsonResponse.optJSONObject("user");
                            if (user != null) {
                                // Guardar sesión
                                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(KEY_USERNAME, user.optString("username"));
                                editor.putString(KEY_EMAIL, user.optString("email", ""));
                                editor.apply();
                                
                                Toast.makeText(this, 
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
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (btnSubmit != null) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText(isRegisterMode ? "Registrarse" : "Iniciar Sesión");
                    }
                    showError("Error de conexión. Verifica tu internet.");
                    e.printStackTrace();
                });
            }
        });
    }
    
    private void showError(String message) {
        if (textViewError != null) {
            textViewError.setText(message);
            textViewError.setVisibility(View.VISIBLE);
        }
    }
    
    private void goToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error yendo a MainActivity: " + e.getMessage(), e);
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

