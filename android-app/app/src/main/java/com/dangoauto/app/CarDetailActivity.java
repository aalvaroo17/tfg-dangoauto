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
        
        android.util.Log.d("CarDetailActivity", "=== onCreate iniciado ===");
        
        try {
            // Verificar que el intent tenga datos
            if (getIntent() == null) {
                android.util.Log.e("CarDetailActivity", "Intent es null");
                android.widget.Toast.makeText(this, "Error: No se recibieron datos del coche", android.widget.Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            setContentView(R.layout.activity_car_detail);
            android.util.Log.d("CarDetailActivity", "Layout cargado");

            // Obtener el ID del coche (método principal y más confiable)
            String carId = getIntent().getStringExtra("carId");
            android.util.Log.d("CarDetailActivity", "ID del coche recibido: " + (carId != null ? carId : "null"));
            
            // Intentar obtener el objeto Car del intent (método secundario)
            Car car = null;
            try {
                Object carObj = getIntent().getSerializableExtra("car");
                if (carObj instanceof Car) {
                    car = (Car) carObj;
                    android.util.Log.d("CarDetailActivity", "Coche deserializado desde intent: " + (car.getName() != null ? car.getName() : "sin nombre"));
                    
                    // Asegurar que el ID esté presente
                    if ((car.getId() == null || car.getId().isEmpty()) && carId != null && !carId.isEmpty()) {
                        car.setId(carId);
                        android.util.Log.d("CarDetailActivity", "ID asignado al coche desde intent: " + carId);
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("CarDetailActivity", "No se pudo deserializar el coche desde intent: " + e.getMessage());
                // No es crítico, continuamos con la carga desde API
            }
            
            // Si tenemos el coche completo y válido, usarlo directamente
            if (car != null && car.getId() != null && !car.getId().isEmpty()) {
                android.util.Log.d("CarDetailActivity", "Usando coche del intent directamente");
                loadCarData(car);
                return;
            }
            
            // Si no tenemos el coche completo pero tenemos el ID, cargar desde API
            if (carId != null && !carId.isEmpty()) {
                android.util.Log.d("CarDetailActivity", "Cargando coche desde API con ID: " + carId);
                loadCarFromApi(carId);
                return;
            }
            
            // Si no tenemos ni coche ni ID, error
            android.util.Log.e("CarDetailActivity", "ERROR: No hay coche ni ID disponible");
            android.widget.Toast.makeText(this, "Error: No se pudo cargar la información del coche. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show();
            finish();

        } catch (Exception e) {
            android.util.Log.e("CarDetailActivity", "ERROR CRÍTICO en onCreate", e);
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error al cargar los detalles del coche", android.widget.Toast.LENGTH_LONG).show();
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                finish();
            }, 2000);
        }
    }
    
    private void loadCarFromApi(String carId) {
        android.util.Log.d("CarDetailActivity", "=== Iniciando carga desde API para ID: " + carId + " ===");
        
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
                
                String apiUrl = "https://tfg-dangoauto.onrender.com/api/cars";
                android.util.Log.d("CarDetailActivity", "Realizando petición GET a: " + apiUrl);
                
                okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(apiUrl)
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();
                
                try (okhttp3.Response response = client.newCall(request).execute()) {
                    int statusCode = response.code();
                    android.util.Log.d("CarDetailActivity", "Código de respuesta HTTP: " + statusCode);
                    
                    String responseBody = response.body() != null ? response.body().string() : "";
                    android.util.Log.d("CarDetailActivity", "Respuesta recibida, longitud: " + responseBody.length());
                    
                    if (responseBody.isEmpty()) {
                        throw new Exception("Respuesta vacía del servidor (HTTP " + statusCode + ")");
                    }
                    
                    if (statusCode != 200) {
                        throw new Exception("Error HTTP " + statusCode + ": " + responseBody);
                    }
                    
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);
                    boolean success = jsonResponse.optBoolean("success", false);
                    android.util.Log.d("CarDetailActivity", "Success en respuesta: " + success);
                    
                    if (success) {
                        org.json.JSONArray carsArray = jsonResponse.getJSONArray("cars");
                        android.util.Log.d("CarDetailActivity", "Total de coches recibidos: " + carsArray.length());
                        
                        Car foundCar = null;
                        for (int i = 0; i < carsArray.length(); i++) {
                            try {
                                org.json.JSONObject carJson = carsArray.getJSONObject(i);
                                String jsonId = carJson.optString("id", "");
                                
                                android.util.Log.d("CarDetailActivity", "Coche " + i + ": ID=" + jsonId + ", buscando: " + carId);
                                
                                // Comparar IDs (case-sensitive, sin espacios)
                                if (jsonId != null && !jsonId.isEmpty() && jsonId.trim().equals(carId.trim())) {
                                    android.util.Log.d("CarDetailActivity", "✓ ¡Coche encontrado en posición " + i + "!");
                                    foundCar = new Car(carJson);
                                    
                                    // Asegurar que el ID esté asignado
                                    if (foundCar.getId() == null || foundCar.getId().isEmpty()) {
                                        foundCar.setId(jsonId);
                                    }
                                    
                                    android.util.Log.d("CarDetailActivity", "Coche parseado: " + foundCar.getFullName() + " (ID: " + foundCar.getId() + ")");
                                    break;
                                }
                            } catch (Exception e) {
                                android.util.Log.w("CarDetailActivity", "Error parseando coche " + i + ": " + e.getMessage());
                                continue;
                            }
                        }
                        
                        if (foundCar != null) {
                            final Car car = foundCar;
                            android.util.Log.d("CarDetailActivity", "Coche encontrado, cargando en UI...");
                            
                            // Cargar en el hilo principal
                            runOnUiThread(() -> {
                                try {
                                    android.util.Log.d("CarDetailActivity", "Ejecutando loadCarData para: " + car.getFullName());
                                    loadCarData(car);
                                    android.util.Log.d("CarDetailActivity", "✓ loadCarData completado exitosamente");
                                } catch (Exception e) {
                                    android.util.Log.e("CarDetailActivity", "ERROR en loadCarData", e);
                                    e.printStackTrace();
                                    android.widget.Toast.makeText(this, "Error al mostrar los detalles: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                            return;
                        } else {
                            android.util.Log.w("CarDetailActivity", "⚠ Coche NO encontrado con ID: " + carId);
                            android.util.Log.w("CarDetailActivity", "Total de coches revisados: " + carsArray.length());
                        }
                    } else {
                        String errorMsg = jsonResponse.optString("message", "Error desconocido");
                        android.util.Log.e("CarDetailActivity", "API devolvió success=false: " + errorMsg);
                    }
                    
                    // Si no se encontró el coche
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Coche no encontrado. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            } catch (org.json.JSONException e) {
                android.util.Log.e("CarDetailActivity", "Error parseando JSON de la API", e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Error al procesar los datos del servidor", android.widget.Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (Exception e) {
                android.util.Log.e("CarDetailActivity", "ERROR cargando coche desde API", e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Error de conexión: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }
    
    private void loadCarData(Car car) {
        android.util.Log.d("CarDetailActivity", "=== loadCarData iniciado para: " + (car != null ? car.getFullName() : "null") + " ===");
        
        if (car == null) {
            android.util.Log.e("CarDetailActivity", "ERROR: car es null en loadCarData");
            android.widget.Toast.makeText(this, "Error: Datos del coche no disponibles", android.widget.Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        try {
            // Toolbar
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
                toolbar.setNavigationOnClickListener(v -> {
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
                });
                android.util.Log.d("CarDetailActivity", "Toolbar configurado");
            } else {
                android.util.Log.w("CarDetailActivity", "Toolbar no encontrado en layout");
            }

            // CollapsingToolbarLayout
            com.google.android.material.appbar.CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(car.getFullName() != null ? car.getFullName() : "Detalles del coche");
                android.util.Log.d("CarDetailActivity", "CollapsingToolbar configurado");
            }

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

                android.util.Log.d("CarDetailActivity", "Imagen configurada");
            } else {
                android.util.Log.w("CarDetailActivity", "ImageViewCar no encontrado en layout");
            }

            // Nombre y precio
            TextView textViewCarName = findViewById(R.id.textViewCarName);
            if (textViewCarName != null) {
                textViewCarName.setText(car.getFullName() != null ? car.getFullName() : "Sin nombre");
                android.util.Log.d("CarDetailActivity", "Nombre configurado");
            }

            TextView textViewPrice = findViewById(R.id.textViewPrice);
            if (textViewPrice != null) {
                textViewPrice.setText(car.getFormattedPrice() != null ? car.getFormattedPrice() : "0€");
                android.util.Log.d("CarDetailActivity", "Precio configurado");
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
                textViewFuel.setText(car.getFuel() != null ? car.getFuel() : "");
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
            
            android.util.Log.d("CarDetailActivity", "=== loadCarData completado exitosamente ===");
            
        } catch (Exception e) {
            android.util.Log.e("CarDetailActivity", "ERROR en loadCarData", e);
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error al cargar los datos: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
