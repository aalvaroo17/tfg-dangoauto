package com.dangoauto.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

public class CarDetailActivity extends AppCompatActivity {

    private static final String TAG = "CarDetailActivity";
    private static final String API_URL = "https://tfg-dangoauto.onrender.com/api/cars";
    
    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutIndicators;
    private TextView textViewPrice;
    private TextView textViewCarName;
    private TextView textViewYear;
    private TextView textViewKm;
    private TextView textViewFuel;
    private TextView textViewPower;
    private TextView textViewTransmission;
    private TextView textViewDescription;
    private TextView textViewFeatures;
    private CollapsingToolbarLayout collapsingToolbar;
    private FloatingActionButton fabContact;
    private Toolbar toolbar;
    
    private String carId;
    private OkHttpClient httpClient;
    private CarImagesAdapter imagesAdapter;
    private List<String> carImages;
    private Car currentCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(TAG, "=== CarDetailActivity onCreate ===");
        
        try {
            setContentView(R.layout.activity_car_detail);
            
            // Inicializar vistas
            initViews();
            
            // Inicializar cliente HTTP
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
            
            // Obtener el ID del coche del intent
            if (getIntent() != null) {
                carId = getIntent().getStringExtra("carId");
                android.util.Log.d(TAG, "ID recibido: " + (carId != null ? carId : "NULL"));
            }
            
            if (carId == null || carId.isEmpty()) {
                android.util.Log.e(TAG, "No se recibió ID del coche");
                showError("Error: No se recibió información del coche");
                return;
            }
            
            // Configurar toolbar
            setupToolbar();
            
            // Cargar datos del coche desde la API
            loadCarData();
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error en onCreate", e);
            showError("Error al inicializar: " + e.getMessage());
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tabLayoutIndicators = findViewById(R.id.tabLayoutIndicators);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewCarName = findViewById(R.id.textViewCarName);
        textViewYear = findViewById(R.id.textViewYear);
        textViewKm = findViewById(R.id.textViewKm);
        textViewFuel = findViewById(R.id.textViewFuel);
        textViewPower = findViewById(R.id.textViewPower);
        textViewTransmission = findViewById(R.id.textViewTransmission);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewFeatures = findViewById(R.id.textViewFeatures);
        fabContact = findViewById(R.id.fabContact);
        
        carImages = new ArrayList<>();
        imagesAdapter = new CarImagesAdapter(carImages);
        viewPagerImages.setAdapter(imagesAdapter);
        
        // Configurar FAB
        if (fabContact != null) {
            fabContact.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Función de contacto próximamente", android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_detail_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {
            shareCar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void shareCar() {
        if (currentCar == null) {
            android.widget.Toast.makeText(this, "Cargando información del coche...", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String shareText = buildShareText();
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Coche en venta: " + currentCar.getFullName());
            
            // Intent específico para WhatsApp si está disponible
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            
            Intent chooser = Intent.createChooser(shareIntent, "Compartir coche");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{whatsappIntent});
            
            startActivity(chooser);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error compartiendo coche", e);
            android.widget.Toast.makeText(this, "Error al compartir", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private String buildShareText() {
        if (currentCar == null) return "";
        
        StringBuilder text = new StringBuilder();
        text.append("🚗 ").append(currentCar.getFullName()).append("\n\n");
        text.append("💰 Precio: ").append(currentCar.getFormattedPrice()).append("\n");
        text.append("📅 Año: ").append(currentCar.getYear()).append("\n");
        text.append("📊 Kilómetros: ").append(String.format("%,d km", currentCar.getKm())).append("\n");
        text.append("⛽ Combustible: ").append(currentCar.getFuel()).append("\n");
        
        if (currentCar.getPower() != null && !currentCar.getPower().isEmpty()) {
            text.append("⚡ Potencia: ").append(currentCar.getPower()).append("\n");
        }
        if (currentCar.getTransmission() != null && !currentCar.getTransmission().isEmpty()) {
            text.append("🔧 Transmisión: ").append(currentCar.getTransmission()).append("\n");
        }
        
        if (currentCar.getDescription() != null && !currentCar.getDescription().isEmpty()) {
            text.append("\n📝 ").append(currentCar.getDescription()).append("\n");
        }
        
        text.append("\n👉 Ver más en DangoAuto");
        
        return text.toString();
    }
    
    private void loadCarData() {
        android.util.Log.d(TAG, "Cargando datos del coche con ID: " + carId);
        
        // Mostrar estado de carga
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle("Cargando...");
        }
        
        new Thread(() -> {
            try {
                android.util.Log.d(TAG, "Realizando petición a: " + API_URL);
                
                Request request = new Request.Builder()
                    .url(API_URL)
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();
                
                Response response = httpClient.newCall(request).execute();
                int statusCode = response.code();
                android.util.Log.d(TAG, "Código HTTP: " + statusCode);
                
                if (!response.isSuccessful()) {
                    throw new Exception("Error HTTP: " + statusCode);
                }
                
                String responseBody = response.body() != null ? response.body().string() : "";
                android.util.Log.d(TAG, "Respuesta recibida, longitud: " + responseBody.length());
                
                if (responseBody.isEmpty()) {
                    throw new Exception("Respuesta vacía del servidor");
                }
                
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                if (!jsonResponse.optBoolean("success", false)) {
                    throw new Exception("La API devolvió error");
                }
                
                JSONArray carsArray = jsonResponse.getJSONArray("cars");
                android.util.Log.d(TAG, "Coches recibidos: " + carsArray.length());
                
                // Buscar el coche por ID
                JSONObject foundCar = null;
                for (int i = 0; i < carsArray.length(); i++) {
                    JSONObject carJson = carsArray.getJSONObject(i);
                    String jsonId = carJson.optString("id", "");
                    
                    if (jsonId.equals(carId)) {
                        foundCar = carJson;
                        android.util.Log.d(TAG, "Coche encontrado en posición " + i);
                        break;
                    }
                }
                
                if (foundCar == null) {
                    android.util.Log.e(TAG, "Coche no encontrado con ID: " + carId);
                    runOnUiThread(() -> showError("Coche no encontrado"));
                    return;
                }
                
                // Extraer datos del JSON
                final String name = foundCar.optString("name", "");
                final String brand = foundCar.optString("brand", "");
                final String model = foundCar.optString("model", "");
                final double price = foundCar.optDouble("price", 0);
                final int year = foundCar.optInt("year", 0);
                final int km = foundCar.optInt("km", 0);
                final String fuel = foundCar.optString("fuel", "");
                final String power = foundCar.optString("power", "");
                final String transmission = foundCar.optString("transmission", "");
                final String description = foundCar.optString("description", "Sin descripción");
                
                // Obtener todas las imágenes
                final List<String> imagesList = new ArrayList<>();
                if (foundCar.has("images")) {
                    JSONArray imagesArray = foundCar.getJSONArray("images");
                    for (int i = 0; i < imagesArray.length(); i++) {
                        String imgUrl = imagesArray.optString(i, "");
                        if (!imgUrl.isEmpty()) {
                            imagesList.add(imgUrl);
                        }
                    }
                }
                // Si no hay imágenes, añadir una placeholder
                if (imagesList.isEmpty()) {
                    imagesList.add("");
                }
                
                // Obtener características
                final StringBuilder featuresText = new StringBuilder();
                if (foundCar.has("features")) {
                    JSONArray featuresArray = foundCar.getJSONArray("features");
                    for (int i = 0; i < featuresArray.length(); i++) {
                        String feature = featuresArray.optString(i, "");
                        if (!feature.isEmpty()) {
                            featuresText.append("✓ ").append(feature).append("\n");
                        }
                    }
                }
                if (featuresText.length() == 0) {
                    featuresText.append("Sin características adicionales.");
                }
                
                // Construir nombre completo
                final String fullName;
                if (!brand.isEmpty() && !model.isEmpty()) {
                    fullName = brand + " " + model + " " + year;
                } else if (!name.isEmpty()) {
                    fullName = name;
                } else {
                    fullName = "Coche";
                }
                
                // Formatear precio
                final String formattedPrice = String.format("%,.0f€", price);
                
                // Crear objeto Car para compartir
                final Car car = new Car(foundCar);
                if (car.getId() == null || car.getId().isEmpty()) {
                    car.setId(carId);
                }
                
                // Actualizar UI en el hilo principal
                runOnUiThread(() -> {
                    // Guardar coche para compartir
                    currentCar = car;
                    try {
                        android.util.Log.d(TAG, "Actualizando UI con datos del coche");
                        
                        // Título
                        if (collapsingToolbar != null) {
                            collapsingToolbar.setTitle(fullName);
                        }
                        
                        // Imágenes en ViewPager
                        if (viewPagerImages != null && imagesAdapter != null) {
                            carImages.clear();
                            carImages.addAll(imagesList);
                            imagesAdapter.notifyDataSetChanged();
                            
                            // Configurar indicadores de página
                            if (tabLayoutIndicators != null) {
                                if (imagesList.size() > 1) {
                                    tabLayoutIndicators.setVisibility(View.VISIBLE);
                                    new TabLayoutMediator(tabLayoutIndicators, viewPagerImages,
                                        (tab, position) -> {}).attach();
                                } else {
                                    tabLayoutIndicators.setVisibility(View.GONE);
                                }
                            }
                        }
                        
                        // Precio y nombre
                        if (textViewPrice != null) {
                            textViewPrice.setText(formattedPrice);
                        }
                        if (textViewCarName != null) {
                            textViewCarName.setText(fullName);
                        }
                        
                        // Especificaciones
                        if (textViewYear != null) {
                            textViewYear.setText(String.valueOf(year));
                        }
                        if (textViewKm != null) {
                            textViewKm.setText(String.format("%,d km", km));
                        }
                        if (textViewFuel != null) {
                            textViewFuel.setText(fuel);
                        }
                        if (textViewPower != null) {
                            if (!power.isEmpty()) {
                                textViewPower.setText(power);
                                textViewPower.setVisibility(View.VISIBLE);
                            } else {
                                textViewPower.setVisibility(View.GONE);
                            }
                        }
                        if (textViewTransmission != null) {
                            if (!transmission.isEmpty()) {
                                textViewTransmission.setText(transmission);
                                textViewTransmission.setVisibility(View.VISIBLE);
                            } else {
                                textViewTransmission.setVisibility(View.GONE);
                            }
                        }
                        
                        // Descripción
                        if (textViewDescription != null) {
                            textViewDescription.setText(description);
                        }
                        
                        // Características
                        if (textViewFeatures != null) {
                            textViewFeatures.setText(featuresText.toString().trim());
                        }
                        
                        android.util.Log.d(TAG, "UI actualizada correctamente");
                        
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error actualizando UI", e);
                        showError("Error mostrando datos: " + e.getMessage());
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error cargando datos", e);
                e.printStackTrace();
                runOnUiThread(() -> showError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }
    
    private void showError(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            finish();
        }, 2000);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
    }
}
