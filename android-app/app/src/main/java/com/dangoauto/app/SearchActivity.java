package com.dangoauto.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final String API_URL = "https://tfg-dangoauto.onrender.com/api/cars";
    
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> filteredCarList;
    
    private TextInputEditText editTextSearch;
    private TextInputEditText editTextMarca;
    private TextInputEditText editTextPrecioMin;
    private TextInputEditText editTextPrecioMax;
    private TextInputEditText editTextCombustible;
    private MaterialButton btnFiltrar;
    private MaterialButton btnLimpiar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateView;
    
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(TAG, "=== SearchActivity onCreate ===");
        
        try {
            setContentView(R.layout.activity_search);

            // Inicializar cliente HTTP
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

            // Inicializar vistas
            initViews();

            // Inicializar listas
            carList = new ArrayList<>();
            filteredCarList = new ArrayList<>();

            // Configurar RecyclerView
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                carAdapter = new CarAdapter(filteredCarList);
                recyclerView.setAdapter(carAdapter);
            }

            // Configurar listeners
            setupListeners();

            // Configurar Pull to Refresh
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setColorSchemeColors(
                    getResources().getColor(R.color.accent_gold, null),
                    getResources().getColor(R.color.accent_gold, null)
                );
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    loadCars();
                });
            }

            // Cargar coches
            loadCars();
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error en onCreate", e);
            Toast.makeText(this, "Error al cargar la pantalla", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCars);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateView = findViewById(R.id.emptyStateView);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextMarca = findViewById(R.id.editTextMarca);
        editTextPrecioMin = findViewById(R.id.editTextPrecioMin);
        editTextPrecioMax = findViewById(R.id.editTextPrecioMax);
        editTextCombustible = findViewById(R.id.editTextCombustible);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
    }
    
    private void setupListeners() {
        if (btnFiltrar != null) {
            btnFiltrar.setOnClickListener(v -> applyFilters());
        }
        if (btnLimpiar != null) {
            btnLimpiar.setOnClickListener(v -> clearFilters());
        }
    }

    private void loadCars() {
        android.util.Log.d(TAG, "Cargando coches desde API...");
        
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                    .url(API_URL)
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = httpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";
                
                android.util.Log.d(TAG, "Respuesta HTTP: " + response.code());
                
                if (!response.isSuccessful()) {
                    throw new Exception("Error HTTP: " + response.code());
                }
                
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                runOnUiThread(() -> {
                    try {
                        if (jsonResponse.optBoolean("success", false)) {
                            JSONArray carsArray = jsonResponse.getJSONArray("cars");
                            carList.clear();
                            
                            android.util.Log.d(TAG, "Total de coches en respuesta: " + carsArray.length());
                            
                            for (int i = 0; i < carsArray.length(); i++) {
                                try {
                                    JSONObject carJson = carsArray.getJSONObject(i);
                                    
                                    // Obtener el ID
                                    String carId = carJson.optString("id", "");
                                    if (carId.isEmpty()) {
                                        android.util.Log.w(TAG, "Coche " + i + " sin ID, asignando temporal");
                                        carId = "car_" + i;
                                        carJson.put("id", carId);
                                    }
                                    
                                    Car car = new Car(carJson);
                                    
                                    // Asegurar que el ID esté asignado
                                    if (car.getId() == null || car.getId().isEmpty()) {
                                        car.setId(carId);
                                    }
                                    
                                    carList.add(car);
                                    android.util.Log.d(TAG, "Coche " + i + ": " + car.getFullName() + " (ID: " + car.getId() + ")");
                                    
                                } catch (Exception e) {
                                    android.util.Log.e(TAG, "Error parseando coche " + i, e);
                                }
                            }
                            
                            // Mostrar todos los coches
                            applyFilters();
                            
                            android.util.Log.d(TAG, "Coches cargados: " + carList.size());
                            
                            // Detener el refresh
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        } else {
                            Toast.makeText(this, "Error al cargar coches", Toast.LENGTH_SHORT).show();
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error procesando respuesta", e);
                        Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error de conexión", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void applyFilters() {
        if (carList == null || carList.isEmpty()) {
            return;
        }
        
        String searchText = getText(editTextSearch).toLowerCase();
        String marca = getText(editTextMarca).toLowerCase();
        String precioMinStr = getText(editTextPrecioMin);
        String precioMaxStr = getText(editTextPrecioMax);
        String combustible = getText(editTextCombustible).toLowerCase();
        
        // Si no hay filtros, mostrar todos
        boolean hasFilters = !searchText.isEmpty() || !marca.isEmpty() || 
                            !precioMinStr.isEmpty() || !precioMaxStr.isEmpty() || 
                            !combustible.isEmpty();
        
        filteredCarList.clear();
        
        if (!hasFilters) {
            filteredCarList.addAll(carList);
        } else {
            double precioMin = parseDouble(precioMinStr, 0);
            double precioMax = parseDouble(precioMaxStr, Double.MAX_VALUE);
            
            for (Car car : carList) {
                if (matchesFilters(car, searchText, marca, precioMin, precioMax, combustible)) {
                    filteredCarList.add(car);
                }
            }
            
            if (filteredCarList.isEmpty()) {
                Toast.makeText(this, "No se encontraron coches con esos filtros", Toast.LENGTH_SHORT).show();
            }
        }
        
        if (carAdapter != null) {
            carAdapter.notifyDataSetChanged();
        }
        
        // Mostrar/ocultar empty state
        updateEmptyState();
        
        android.util.Log.d(TAG, "Mostrando " + filteredCarList.size() + " coches");
    }
    
    private boolean matchesFilters(Car car, String search, String marca, 
                                   double precioMin, double precioMax, String combustible) {
        // Filtro de búsqueda
        if (!search.isEmpty()) {
            String fullName = car.getFullName().toLowerCase();
            String desc = car.getDescription() != null ? car.getDescription().toLowerCase() : "";
            if (!fullName.contains(search) && !desc.contains(search)) {
                return false;
            }
        }
        
        // Filtro de marca
        if (!marca.isEmpty()) {
            String carBrand = car.getBrand() != null ? car.getBrand().toLowerCase() : "";
            String carName = car.getName() != null ? car.getName().toLowerCase() : "";
            if (!carBrand.contains(marca) && !carName.contains(marca)) {
                return false;
            }
        }
        
        // Filtro de precio
        double carPrice = car.getPrice();
        if (carPrice < precioMin || carPrice > precioMax) {
            return false;
        }
        
        // Filtro de combustible
        if (!combustible.isEmpty()) {
            String carFuel = car.getFuel() != null ? car.getFuel().toLowerCase() : "";
            if (!carFuel.contains(combustible)) {
                return false;
            }
        }
        
        return true;
    }
    
    private String getText(TextInputEditText editText) {
        if (editText != null && editText.getText() != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }
    
    private double parseDouble(String str, double defaultValue) {
        try {
            if (!str.isEmpty()) {
                return Double.parseDouble(str);
            }
        } catch (NumberFormatException e) {
            // Ignorar
        }
        return defaultValue;
    }

    private void updateEmptyState() {
        if (emptyStateView != null && recyclerView != null) {
            boolean isEmpty = filteredCarList == null || filteredCarList.isEmpty();
            emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }
    
    private void clearFilters() {
        if (editTextSearch != null) editTextSearch.setText("");
        if (editTextMarca != null) editTextMarca.setText("");
        if (editTextPrecioMin != null) editTextPrecioMin.setText("");
        if (editTextPrecioMax != null) editTextPrecioMax.setText("");
        if (editTextCombustible != null) editTextCombustible.setText("");
        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar coches cuando se vuelve a esta actividad
        loadCars();
    }
}
