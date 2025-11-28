package com.dangoauto.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<String> carList;
    private List<String> filteredCarList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // No configuramos ActionBar porque usamos tema NoActionBar
        // El título y navegación se pueden añadir en el layout si es necesario

        // Inicializar lista de coches de ejemplo
        carList = new ArrayList<>();
        carList.add("Ford Focus 2020");
        carList.add("Seat León 2023");
        carList.add("BMW Serie 3 2021");
        carList.add("Audi A4 2022");
        carList.add("Mercedes Clase C 2023");
        carList.add("Toyota Corolla 2021");
        carList.add("Volkswagen Golf 2022");
        carList.add("Peugeot 308 2020");

        filteredCarList = new ArrayList<>(carList);

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carAdapter = new CarAdapter(filteredCarList);
        recyclerView.setAdapter(carAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menú deshabilitado por ahora ya que no hay ActionBar
        // Se puede implementar con un Toolbar personalizado más adelante
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void filterCars(String query) {
        filteredCarList.clear();
        if (query == null || query.isEmpty()) {
            filteredCarList.addAll(carList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (String car : carList) {
                if (car.toLowerCase().contains(lowerQuery)) {
                    filteredCarList.add(car);
                }
            }
        }
        carAdapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        // Por ahora solo muestra un Toast, se implementará el diálogo completo más adelante
        Toast.makeText(this, "Filtros avanzados próximamente", Toast.LENGTH_SHORT).show();
    }
}

