package com.dangoauto.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DangoAutoPrefs";
    private static final String KEY_USERNAME = "username";
    
    private Button btnBuscarFiltrar;
    private Button btnVenderCoche;
    private Button btnComprarCoche;
    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Verificar si hay sesión ANTES de cargar la vista
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String username = prefs.getString(KEY_USERNAME, null);
            
            if (username == null || username.isEmpty()) {
                // No hay sesión, ir a LoginActivity
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            
            setContentView(R.layout.activity_main);
            
            // Mostrar nombre de usuario si está disponible
            textViewWelcome = findViewById(R.id.textViewWelcome);
            if (textViewWelcome != null) {
                textViewWelcome.setText("Bienvenido, " + username);
            }

            // Inicializar botones
            btnBuscarFiltrar = findViewById(R.id.btnBuscarFiltrar);
            btnVenderCoche = findViewById(R.id.btnVenderCoche);
            btnComprarCoche = findViewById(R.id.btnComprarCoche);

            // Configurar listeners
            if (btnBuscarFiltrar != null) {
                btnBuscarFiltrar.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        android.util.Log.e("MainActivity", "Error abriendo SearchActivity", e);
                        Toast.makeText(MainActivity.this, "Error al abrir búsqueda", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnVenderCoche != null) {
                btnVenderCoche.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, SellActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        android.util.Log.e("MainActivity", "Error abriendo SellActivity", e);
                        Toast.makeText(MainActivity.this, "Error al abrir venta", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnComprarCoche != null) {
                btnComprarCoche.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, 
                        "Funcionalidad de Asistente IA próximamente", 
                        Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error crítico en onCreate", e);
            Toast.makeText(this, "Error al cargar la aplicación", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
