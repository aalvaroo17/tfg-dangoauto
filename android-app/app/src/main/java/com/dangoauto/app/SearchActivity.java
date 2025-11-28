package com.dangoauto.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button btnFiltrar;
    private Button btnLimpiar;
    
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
                                    carList.add(car);
                                }
                                
                                filteredCarList.clear();
                                filteredCarList.addAll(carList);
                                if (carAdapter != null) {
                                    carAdapter.notifyDataSetChanged();
                                }
                                
                                if (carList.isEmpty()) {
                                    Toast.makeText(SearchActivity.this, "No hay coches disponibles", Toast.LENGTH_SHORT).show();
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
            String searchText = editTextSearch != null ? editTextSearch.getText().toString().trim().toLowerCase() : "";
            String marca = editTextMarca != null ? editTextMarca.getText().toString().trim().toLowerCase() : "";
            String precioMinStr = editTextPrecioMin != null ? editTextPrecioMin.getText().toString().trim() : "";
            String precioMaxStr = editTextPrecioMax != null ? editTextPrecioMax.getText().toString().trim() : "";
            String combustible = editTextCombustible != null ? editTextCombustible.getText().toString().trim().toLowerCase() : "";
            
            double precioMin = precioMinStr.isEmpty() ? 0 : Double.parseDouble(precioMinStr);
            double precioMax = precioMaxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(precioMaxStr);
            
            filteredCarList.clear();
            
            for (Car car : carList) {
                boolean matches = true;
                
                // Filtro de búsqueda (nombre, marca, modelo)
                if (!searchText.isEmpty()) {
                    String fullName = car.getFullName().toLowerCase();
                    String description = car.getDescription() != null ? car.getDescription().toLowerCase() : "";
                    if (!fullName.contains(searchText) && !description.contains(searchText)) {
                        matches = false;
                    }
                }
                
                // Filtro de marca
                if (matches && !marca.isEmpty()) {
                    String carBrand = car.getBrand() != null ? car.getBrand().toLowerCase() : "";
                    String carName = car.getName() != null ? car.getName().toLowerCase() : "";
                    if (!carBrand.contains(marca) && !carName.contains(marca)) {
                        matches = false;
                    }
                }
                
                // Filtro de precio
                if (matches) {
                    if (car.getPrice() < precioMin || car.getPrice() > precioMax) {
                        matches = false;
                    }
                }
                
                // Filtro de combustible
                if (matches && !combustible.isEmpty()) {
                    String carFuel = car.getFuel() != null ? car.getFuel().toLowerCase() : "";
                    if (!carFuel.contains(combustible)) {
                        matches = false;
                    }
                }
                
                if (matches) {
                    filteredCarList.add(car);
                }
            }
            
            if (carAdapter != null) {
                carAdapter.notifyDataSetChanged();
            }
            
            if (filteredCarList.isEmpty()) {
                Toast.makeText(this, "No se encontraron coches con esos filtros", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error: Precio inválido", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("SearchActivity", "Error aplicando filtros", e);
            Toast.makeText(this, "Error al aplicar filtros", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilters() {
        if (editTextSearch != null) editTextSearch.setText("");
        if (editTextMarca != null) editTextMarca.setText("");
        if (editTextPrecioMin != null) editTextPrecioMin.setText("");
        if (editTextPrecioMax != null) editTextPrecioMax.setText("");
        if (editTextCombustible != null) editTextCombustible.setText("");
        
        filteredCarList.clear();
        filteredCarList.addAll(carList);
        if (carAdapter != null) {
            carAdapter.notifyDataSetChanged();
        }
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
