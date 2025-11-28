package com.dangoauto.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
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

            ImageView imageViewCar = findViewById(R.id.imageViewCar);
            TextView textViewCarName = findViewById(R.id.textViewCarName);
            TextView textViewPrice = findViewById(R.id.textViewPrice);
            TextView textViewSpecs = findViewById(R.id.textViewSpecs);
            TextView textViewDescription = findViewById(R.id.textViewDescription);
            TextView textViewFeatures = findViewById(R.id.textViewFeatures);

            // Cargar imagen
            if (imageViewCar != null) {
                List<String> images = car.getImages();
                if (images != null && !images.isEmpty()) {
                    Glide.with(this)
                        .load(images.get(0))
                        .placeholder(R.color.background_card)
                        .error(R.color.background_card)
                        .into(imageViewCar);
                }
            }

            // Nombre completo
            if (textViewCarName != null) {
                textViewCarName.setText(car.getFullName());
            }

            // Precio
            if (textViewPrice != null) {
                textViewPrice.setText(car.getFormattedPrice());
            }

            // Especificaciones
            if (textViewSpecs != null) {
                StringBuilder specs = new StringBuilder();
                specs.append("Año: ").append(car.getYear()).append("\n");
                specs.append("Kilómetros: ").append(String.format("%,d", car.getKm())).append(" km\n");
                specs.append("Combustible: ").append(car.getFuel()).append("\n");
                if (car.getPower() != null && !car.getPower().isEmpty()) {
                    specs.append("Potencia: ").append(car.getPower()).append("\n");
                }
                if (car.getTransmission() != null && !car.getTransmission().isEmpty()) {
                    specs.append("Transmisión: ").append(car.getTransmission());
                }
                textViewSpecs.setText(specs.toString());
            }

            // Descripción
            if (textViewDescription != null) {
                textViewDescription.setText(car.getDescription() != null ? car.getDescription() : "Sin descripción disponible.");
            }

            // Características
            if (textViewFeatures != null) {
                List<String> features = car.getFeatures();
                if (features != null && !features.isEmpty()) {
                    StringBuilder featuresText = new StringBuilder();
                    for (String feature : features) {
                        featuresText.append("• ").append(feature).append("\n");
                    }
                    textViewFeatures.setText(featuresText.toString());
                } else {
                    textViewFeatures.setText("Sin características adicionales.");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("CarDetailActivity", "Error cargando detalles", e);
            finish();
        }
    }
}

