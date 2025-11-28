package com.dangoauto.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnBuscarFiltrar;
    private Button btnVenderCoche;
    private Button btnComprarCoche;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar botones
        btnBuscarFiltrar = findViewById(R.id.btnBuscarFiltrar);
        btnVenderCoche = findViewById(R.id.btnVenderCoche);
        btnComprarCoche = findViewById(R.id.btnComprarCoche);

        // Configurar listeners
        btnBuscarFiltrar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        btnVenderCoche.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SellActivity.class);
            startActivity(intent);
        });

        btnComprarCoche.setOnClickListener(v -> {
            // Por ahora solo muestra un mensaje, se implementará más adelante
            android.widget.Toast.makeText(MainActivity.this, 
                "Funcionalidad de Asistente IA próximamente", 
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }
}

