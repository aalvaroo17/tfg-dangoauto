package com.dangoauto.app;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String API_BASE_URL = "https://tfg-dangoauto.onrender.com";
    
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> filteredCarList;
    
    private TextInputLayout textInputLayoutSearch;
    private TextInputEditText editTextSearch;
    private TextInputLayout textInputLayoutMarca;
    private TextInputEditText editTextMarca;
    private TextInputLayout textInputLayoutPrecioMin;
    private TextInputEditText editTextPrecioMin;
    private TextInputLayout textInputLayoutPrecioMax;
    private TextInputEditText editTextPrecioMax;
    private TextInputLayout textInputLayoutCombustible;
    private TextInputEditText editTextCombustible;
    private com.google.android.material.button.MaterialButton btnFiltrar;
    private com.google.android.material.button.MaterialButton btnLimpiar;
    
    private ExecutorService executorService;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_search);

            // Inicializar cliente HTTP
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
            executorService = Executors.newSingleThreadExecutor();

            // Inicializar vistas
            recyclerView = findViewById(R.id.recyclerViewCars);
            textInputLayoutSearch = findViewById(R.id.textInputLayoutSearch);
            editTextSearch = findViewById(R.id.editTextSearch);
            textInputLayoutMarca = findViewById(R.id.textInputLayoutMarca);
            editTextMarca = findViewById(R.id.editTextMarca);
            textInputLayoutPrecioMin = findViewById(R.id.textInputLayoutPrecioMin);
            editTextPrecioMin = findViewById(R.id.editTextPrecioMin);
            textInputLayoutPrecioMax = findViewById(R.id.textInputLayoutPrecioMax);
            editTextPrecioMax = findViewById(R.id.editTextPrecioMax);
            textInputLayoutCombustible = findViewById(R.id.textInputLayoutCombustible);
            editTextCombustible = findViewById(R.id.editTextCombustible);
            btnFiltrar = findViewById(R.id.btnFiltrar);
            btnLimpiar = findViewById(R.id.btnLimpiar);

            carList = new ArrayList<>();
            filteredCarList = new ArrayList<>();

            // Configurar RecyclerView
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                carAdapter = new CarAdapter(filteredCarList);
                recyclerView.setAdapter(carAdapter);
            }

            // Configurar listeners
            if (btnFiltrar != null) {
                btnFiltrar.setOnClickListener(v -> applyFilters());
            }
            if (btnLimpiar != null) {
                btnLimpiar.setOnClickListener(v -> clearFilters());
            }
            if (editTextSearch != null) {
                editTextSearch.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        applyFilters();
                    }
                });
            }

            // Cargar coches desde el backend
            loadCars();
        } catch (Exception e) {
            android.util.Log.e("SearchActivity", "Error crítico en onCreate", e);
            Toast.makeText(this, "Error al cargar la pantalla", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadCars() {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(API_BASE_URL + "/api/cars")
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    runOnUiThread(() -> {
                        try {
                            if (jsonResponse.optBoolean("success", false)) {
                                JSONArray carsArray = jsonResponse.getJSONArray("cars");
                                carList.clear();
                                
                                for (int i = 0; i < carsArray.length(); i++) {
                                    JSONObject carJson = carsArray.getJSONObject(i);
                                    Car car = new Car(carJson);
                                    
                                    // Asegurar que el ID esté presente (puede venir del documento de Firestore)
                                    String carId = carJson.optString("id", "");
                                    if (carId.isEmpty() && car.getId() == null) {
                                        // Si no hay ID, generar uno temporal o usar el índice
                                        android.util.Log.w("SearchActivity", "Coche sin ID, usando índice temporal");
                                        carId = "temp_" + i;
                                    }
                                    if (car.getId() == null || car.getId().isEmpty()) {
                                        car.setId(carId);
                                    }
                                    
                                    carList.add(car);
                                    android.util.Log.d("SearchActivity", "Coche cargado: " + car.getFullName() + " (ID: " + car.getId() + ")");
                                }
                                
                                // Aplicar filtros automáticamente después de cargar
                                applyFilters();
                                
                                if (carList.isEmpty()) {
                                    Toast.makeText(SearchActivity.this, "No hay coches disponibles", Toast.LENGTH_SHORT).show();
                                } else {
                                    android.util.Log.d("SearchActivity", "Coches cargados: " + carList.size());
                                }
                            } else {
                                Toast.makeText(SearchActivity.this, "Error al cargar coches", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            android.util.Log.e("SearchActivity", "Error parseando coches", e);
                            Toast.makeText(SearchActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    android.util.Log.e("SearchActivity", "Error cargando coches", e);
                    Toast.makeText(SearchActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void applyFilters() {
        try {
            // Si no hay coches cargados, no hacer nada
            if (carList == null || carList.isEmpty()) {
                android.util.Log.d("SearchActivity", "No hay coches cargados aún");
                return;
            }
            
            String searchText = editTextSearch != null ? editTextSearch.getText().toString().trim().toLowerCase() : "";
            String marca = editTextMarca != null ? editTextMarca.getText().toString().trim().toLowerCase() : "";
            String precioMinStr = editTextPrecioMin != null ? editTextPrecioMin.getText().toString().trim() : "";
            String precioMaxStr = editTextPrecioMax != null ? editTextPrecioMax.getText().toString().trim() : "";
            String combustible = editTextCombustible != null ? editTextCombustible.getText().toString().trim().toLowerCase() : "";
            
            // Verificar si hay algún filtro activo
            boolean hasFilters = !searchText.isEmpty() || !marca.isEmpty() || 
                                !precioMinStr.isEmpty() || !precioMaxStr.isEmpty() || 
                                !combustible.isEmpty();
            
            // Si no hay filtros, mostrar todos los coches
            if (!hasFilters) {
                filteredCarList.clear();
                filteredCarList.addAll(carList);
                if (carAdapter != null) {
                    carAdapter.notifyDataSetChanged();
                }
                android.util.Log.d("SearchActivity", "Sin filtros, mostrando todos los coches: " + carList.size());
                return;
            }
            
            // Parsear precios con manejo de errores
            double precioMin = 0;
            double precioMax = Double.MAX_VALUE;
            
            try {
                if (!precioMinStr.isEmpty()) {
                    precioMin = Double.parseDouble(precioMinStr);
                }
            } catch (NumberFormatException e) {
                android.util.Log.e("SearchActivity", "Error parseando precio mínimo", e);
            }
            
            try {
                if (!precioMaxStr.isEmpty()) {
                    precioMax = Double.parseDouble(precioMaxStr);
                }
            } catch (NumberFormatException e) {
                android.util.Log.e("SearchActivity", "Error parseando precio máximo", e);
            }
            
            filteredCarList.clear();
            int matchesCount = 0;
            
            for (Car car : carList) {
                boolean matches = true;
                
                // Filtro de búsqueda (nombre, marca, modelo, descripción)
                if (!searchText.isEmpty()) {
                    String fullName = car.getFullName() != null ? car.getFullName().toLowerCase() : "";
                    String description = car.getDescription() != null ? car.getDescription().toLowerCase() : "";
                    String carBrand = car.getBrand() != null ? car.getBrand().toLowerCase() : "";
                    String carModel = car.getModel() != null ? car.getModel().toLowerCase() : "";
                    
                    if (!fullName.contains(searchText) && 
                        !description.contains(searchText) && 
                        !carBrand.contains(searchText) && 
                        !carModel.contains(searchText)) {
                        matches = false;
                    }
                }
                
                // Filtro de marca
                if (matches && !marca.isEmpty()) {
                    String carBrand = car.getBrand() != null ? car.getBrand().toLowerCase() : "";
                    String carName = car.getName() != null ? car.getName().toLowerCase() : "";
                    String fullName = car.getFullName() != null ? car.getFullName().toLowerCase() : "";
                    
                    if (!carBrand.contains(marca) && 
                        !carName.contains(marca) && 
                        !fullName.contains(marca)) {
                        matches = false;
                    }
                }
                
                // Filtro de precio
                if (matches) {
                    double carPrice = car.getPrice();
                    if (carPrice < precioMin || carPrice > precioMax) {
                        matches = false;
                    }
                }
                
                // Filtro de combustible
                if (matches && !combustible.isEmpty()) {
                    String carFuel = car.getFuel() != null ? car.getFuel().toLowerCase() : "";
                    if (carFuel.isEmpty() || !carFuel.contains(combustible)) {
                        matches = false;
                    }
                }
                
                if (matches) {
                    filteredCarList.add(car);
                    matchesCount++;
                }
            }
            
            android.util.Log.d("SearchActivity", "Filtros aplicados. Resultados: " + matchesCount + " de " + carList.size());
            
            if (carAdapter != null) {
                carAdapter.notifyDataSetChanged();
            }
            
            if (filteredCarList.isEmpty() && hasFilters) {
                Toast.makeText(this, "No se encontraron coches con esos filtros", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("SearchActivity", "Error aplicando filtros", e);
            Toast.makeText(this, "Error al aplicar filtros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilters() {
        if (editTextSearch != null) editTextSearch.setText("");
        if (editTextMarca != null) editTextMarca.setText("");
        if (editTextPrecioMin != null) editTextPrecioMin.setText("");
        if (editTextPrecioMax != null) editTextPrecioMax.setText("");
        if (editTextCombustible != null) editTextCombustible.setText("");
        
        // Aplicar filtros (que ahora mostrará todos al estar vacíos)
        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar coches cuando se vuelve a esta actividad (por si se añadió uno nuevo)
        loadCars();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
