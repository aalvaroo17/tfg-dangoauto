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
        
        try {
            setContentView(R.layout.activity_sell);

            // Inicializar vistas
            editTextDni = findViewById(R.id.editTextDni);
            editTextTelefono = findViewById(R.id.editTextTelefono);
            editTextEmail = findViewById(R.id.editTextEmail);
            btnSubirInfo = findViewById(R.id.btnSubirInfo);

            // Configurar listener del botón
            if (btnSubirInfo != null) {
                btnSubirInfo.setOnClickListener(v -> {
                    try {
                        if (validateFields()) {
                            // Por ahora solo muestra un Toast de confirmación
                            Toast.makeText(SellActivity.this, 
                                getString(R.string.info_subida), 
                                Toast.LENGTH_SHORT).show();
                            
                            // Limpiar campos después de enviar
                            if (editTextDni != null) editTextDni.setText("");
                            if (editTextTelefono != null) editTextTelefono.setText("");
                            if (editTextEmail != null) editTextEmail.setText("");
                        } else {
                            Toast.makeText(SellActivity.this, 
                                getString(R.string.campos_vacios), 
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SellActivity", "Error en onClick", e);
                        Toast.makeText(SellActivity.this, "Error al procesar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("SellActivity", "Error crítico en onCreate", e);
            Toast.makeText(this, "Error al cargar la pantalla", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Navegación de vuelta se maneja con el botón de sistema o programáticamente

    private boolean validateFields() {
        try {
            String dni = editTextDni != null ? editTextDni.getText().toString().trim() : "";
            String telefono = editTextTelefono != null ? editTextTelefono.getText().toString().trim() : "";
            String email = editTextEmail != null ? editTextEmail.getText().toString().trim() : "";

            return !dni.isEmpty() && !telefono.isEmpty() && !email.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("SellActivity", "Error en validateFields", e);
            return false;
        }
    }
}

