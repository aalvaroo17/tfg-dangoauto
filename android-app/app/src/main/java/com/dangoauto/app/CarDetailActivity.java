package com.dangoauto.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class CarDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Verificar que el intent tenga datos
            if (getIntent() == null) {
                android.util.Log.e("CarDetailActivity", "Intent es null");
                finish();
                return;
            }
            
            setContentView(R.layout.activity_car_detail);

            Car car = null;
            
            // Intentar obtener el coche del intent
            String carId = getIntent().getStringExtra("carId");
            android.util.Log.d("CarDetailActivity", "ID del coche recibido: " + (carId != null ? carId : "null"));
            
            try {
                car = (Car) getIntent().getSerializableExtra("car");
                if (car != null) {
                    android.util.Log.d("CarDetailActivity", "Coche cargado desde intent: " + car.getName());
                    // Asegurar que el ID esté presente
                    if (car.getId() == null || car.getId().isEmpty()) {
                        if (carId != null && !carId.isEmpty()) {
                            car.setId(carId);
                            android.util.Log.d("CarDetailActivity", "ID asignado al coche: " + carId);
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("CarDetailActivity", "Error deserializando coche: " + e.getMessage());
                e.printStackTrace();
                android.util.Log.e("CarDetailActivity", "Stack trace completo:", e);
            }
            
            // Si no se pudo cargar desde el intent, intentar cargar desde API usando el ID
            if (car == null) {
                if (carId != null && !carId.isEmpty()) {
                    android.util.Log.d("CarDetailActivity", "Coche null, intentando cargar desde API con ID: " + carId);
                    loadCarFromApi(carId);
                    return;
                } else {
                    android.util.Log.e("CarDetailActivity", "Coche es null y no hay ID disponible");
                    android.widget.Toast.makeText(this, "Error: No se pudo cargar la información del coche. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }

            // Cargar los datos del coche
            loadCarData(car);

        } catch (Exception e) {
            android.util.Log.e("CarDetailActivity", "Error cargando detalles", e);
            android.widget.Toast.makeText(this, "Error al cargar los detalles del coche", android.widget.Toast.LENGTH_LONG).show();
            // Esperar un poco antes de cerrar para que el usuario vea el mensaje
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                finish();
            }, 2000);
        }
    }
    
    private void loadCarFromApi(String carId) {
        android.util.Log.d("CarDetailActivity", "Iniciando carga desde API para ID: " + carId);
        
        // Mostrar un mensaje de carga al usuario
        runOnUiThread(() -> {
            android.widget.Toast.makeText(this, "Cargando detalles del coche...", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();
                
                okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://tfg-dangoauto.onrender.com/api/cars")
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();
                
                android.util.Log.d("CarDetailActivity", "Realizando petición a API...");
                
                try (okhttp3.Response response = client.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    android.util.Log.d("CarDetailActivity", "Respuesta recibida, longitud: " + responseBody.length());
                    
                    if (responseBody.isEmpty()) {
                        throw new Exception("Respuesta vacía del servidor");
                    }
                    
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);
                    
                    if (jsonResponse.optBoolean("success", false)) {
                        org.json.JSONArray carsArray = jsonResponse.getJSONArray("cars");
                        android.util.Log.d("CarDetailActivity", "Total de coches recibidos: " + carsArray.length());
                        
                        Car foundCar = null;
                        for (int i = 0; i < carsArray.length(); i++) {
                            org.json.JSONObject carJson = carsArray.getJSONObject(i);
                            String jsonId = carJson.optString("id", "");
                            android.util.Log.d("CarDetailActivity", "Comparando ID: " + jsonId + " con " + carId);
                            
                            // Comparar IDs (case-sensitive)
                            if (jsonId != null && jsonId.equals(carId)) {
                                android.util.Log.d("CarDetailActivity", "¡Coche encontrado!");
                                foundCar = new Car(carJson);
                                // Asegurar que el ID esté asignado
                                if (foundCar.getId() == null || foundCar.getId().isEmpty()) {
                                    foundCar.setId(jsonId);
                                }
                                break;
                            }
                        }
                        
                        if (foundCar != null) {
                            final Car car = foundCar;
                            // Cargar en el hilo principal
                            runOnUiThread(() -> {
                                try {
                                    android.util.Log.d("CarDetailActivity", "Cargando datos del coche en UI: " + car.getFullName());
                                    loadCarData(car);
                                } catch (Exception e) {
                                    android.util.Log.e("CarDetailActivity", "Error cargando datos del coche en UI", e);
                                    e.printStackTrace();
                                    android.widget.Toast.makeText(this, "Error al mostrar los detalles", android.widget.Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                            return;
                        } else {
                            android.util.Log.w("CarDetailActivity", "Coche no encontrado con ID: " + carId);
                        }
                    } else {
                        android.util.Log.e("CarDetailActivity", "API devolvió success=false");
                    }
                    
                    // Si no se encontró el coche
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Coche no encontrado. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("CarDetailActivity", "Error cargando coche desde API", e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Error de conexión. Verifica tu internet.", android.widget.Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }
    
    private void loadCarData(Car car) {
        // Este método contiene toda la lógica de carga de datos que estaba en onCreate
        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
        });

        // CollapsingToolbarLayout
        com.google.android.material.appbar.CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setTitle(car.getFullName());

        // Imagen principal
        ImageView imageViewCar = findViewById(R.id.imageViewCar);
        if (imageViewCar != null) {
            List<String> images = car.getImages();
            if (images != null && !images.isEmpty()) {
                String imageUrl = images.get(0);
                android.util.Log.d("CarDetailActivity", "Cargando imagen: " + imageUrl);
                
                // Si la imagen es de uploads y puede no existir, usar placeholder mejorado
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.color.background_card)
                    .error(R.color.background_card) // Usar color de fondo en lugar de icono
                    .fallback(R.color.background_card) // Fallback si la URL es null
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            android.util.Log.w("CarDetailActivity", "Error cargando imagen: " + imageUrl);
                            if (e != null && e.getMessage() != null) {
                                android.util.Log.w("CarDetailActivity", "Mensaje de error: " + e.getMessage());
                            }
                            // Si la imagen falla, mostrar un placeholder
                            runOnUiThread(() -> {
                                imageViewCar.setImageResource(R.color.background_card);
                            });
                            return false; // Permitir que Glide muestre el error placeholder
                        }
                        
                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            android.util.Log.d("CarDetailActivity", "Imagen cargada exitosamente");
                            return false;
                        }
                    })
                    .into(imageViewCar);
            } else {
                // Si no hay imágenes, mostrar placeholder
                android.util.Log.w("CarDetailActivity", "El coche no tiene imágenes");
                imageViewCar.setImageResource(R.color.background_card);
            }
        }

        // Nombre y precio
        TextView textViewCarName = findViewById(R.id.textViewCarName);
        if (textViewCarName != null) {
            textViewCarName.setText(car.getFullName());
        }

        TextView textViewPrice = findViewById(R.id.textViewPrice);
        if (textViewPrice != null) {
            textViewPrice.setText(car.getFormattedPrice());
        }

        // Especificaciones individuales
        TextView textViewYear = findViewById(R.id.textViewYear);
        if (textViewYear != null) {
            textViewYear.setText(String.valueOf(car.getYear()));
        }

        TextView textViewKm = findViewById(R.id.textViewKm);
        if (textViewKm != null) {
            textViewKm.setText(String.format("%,d km", car.getKm()));
        }

        TextView textViewFuel = findViewById(R.id.textViewFuel);
        if (textViewFuel != null) {
            textViewFuel.setText(car.getFuel());
        }

        TextView textViewPower = findViewById(R.id.textViewPower);
        if (textViewPower != null) {
            if (car.getPower() != null && !car.getPower().isEmpty()) {
                textViewPower.setText(car.getPower());
                textViewPower.setVisibility(View.VISIBLE);
            } else {
                textViewPower.setVisibility(View.GONE);
            }
        }

        TextView textViewTransmission = findViewById(R.id.textViewTransmission);
        if (textViewTransmission != null) {
            if (car.getTransmission() != null && !car.getTransmission().isEmpty()) {
                textViewTransmission.setText(car.getTransmission());
                textViewTransmission.setVisibility(View.VISIBLE);
            } else {
                textViewTransmission.setVisibility(View.GONE);
            }
        }

        // Descripción
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        if (textViewDescription != null) {
            textViewDescription.setText(car.getDescription() != null ? car.getDescription() : "Sin descripción disponible.");
        }

        // Características
        TextView textViewFeatures = findViewById(R.id.textViewFeatures);
        if (textViewFeatures != null) {
            List<String> features = car.getFeatures();
            if (features != null && !features.isEmpty()) {
                StringBuilder featuresText = new StringBuilder();
                for (String feature : features) {
                    featuresText.append("✓ ").append(feature).append("\n");
                }
                textViewFeatures.setText(featuresText.toString());
            } else {
                textViewFeatures.setText("Sin características adicionales.");
            }
        }

        // FAB de contacto
        FloatingActionButton fabContact = findViewById(R.id.fabContact);
        if (fabContact != null) {
            fabContact.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Función de contacto próximamente", android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }
}
