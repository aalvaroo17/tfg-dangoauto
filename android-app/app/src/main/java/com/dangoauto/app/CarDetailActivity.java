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
        setContentView(R.layout.activity_car_detail);

        try {
            Car car = (Car) getIntent().getSerializableExtra("car");
            if (car == null) {
                finish();
                return;
            }

            // Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
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
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
            collapsingToolbar.setTitle(car.getFullName());

            // Imagen principal
            ImageView imageViewCar = findViewById(R.id.imageViewCar);
            if (imageViewCar != null) {
                List<String> images = car.getImages();
                if (images != null && !images.isEmpty()) {
                    Glide.with(this)
                        .load(images.get(0))
                        .placeholder(R.color.background_card)
                        .error(R.color.background_card)
                        .centerCrop()
                        .into(imageViewCar);
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
                    // Aquí puedes agregar la lógica para contactar
                    android.widget.Toast.makeText(this, "Función de contacto próximamente", android.widget.Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            android.util.Log.e("CarDetailActivity", "Error cargando detalles", e);
            finish();
        }
    }
}
