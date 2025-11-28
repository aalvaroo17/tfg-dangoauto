package com.dangoauto.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class SellActivity extends AppCompatActivity {

    private TextInputEditText editTextDni;
    private TextInputEditText editTextTelefono;
    private TextInputEditText editTextEmail;
    private Button btnSubirInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        // No configuramos ActionBar porque usamos tema NoActionBar
        // El título ya está en el layout

        // Inicializar vistas
        editTextDni = findViewById(R.id.editTextDni);
        editTextTelefono = findViewById(R.id.editTextTelefono);
        editTextEmail = findViewById(R.id.editTextEmail);
        btnSubirInfo = findViewById(R.id.btnSubirInfo);

        // Configurar listener del botón
        btnSubirInfo.setOnClickListener(v -> {
            if (validateFields()) {
                // Por ahora solo muestra un Toast de confirmación
                Toast.makeText(SellActivity.this, 
                    getString(R.string.info_subida), 
                    Toast.LENGTH_SHORT).show();
                
                // Limpiar campos después de enviar
                editTextDni.setText("");
                editTextTelefono.setText("");
                editTextEmail.setText("");
            } else {
                Toast.makeText(SellActivity.this, 
                    getString(R.string.campos_vacios), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navegación de vuelta se maneja con el botón de sistema o programáticamente

    private boolean validateFields() {
        String dni = editTextDni.getText().toString().trim();
        String telefono = editTextTelefono.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        return !dni.isEmpty() && !telefono.isEmpty() && !email.isEmpty();
    }
}

