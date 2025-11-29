package com.dangoauto.app;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;

    public CarAdapter(List<Car> carList) {
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        
        // Nombre
        holder.textViewCarName.setText(car.getFullName());
        
        // Precio
        holder.textViewCarPrice.setText(car.getFormattedPrice());
        
        // Especificaciones
        StringBuilder specs = new StringBuilder();
        specs.append(car.getYear()).append(" • ");
        specs.append(String.format("%,d", car.getKm())).append(" km • ");
        specs.append(car.getFuel());
        holder.textViewCarSpecs.setText(specs.toString());
        
        // Imagen
        List<String> images = car.getImages();
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0);
            Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.color.background_card)
                .error(R.color.background_card)
                .fallback(R.color.background_card)
                .centerCrop()
                .into(holder.imageViewCar);
        } else {
            // Si no hay imágenes, mostrar placeholder
            holder.imageViewCar.setImageResource(R.color.background_card);
        }
        
        // Click listener con animación y manejo de errores
        holder.itemView.setOnClickListener(v -> {
            try {
                // Validar que el coche no sea null
                if (car == null) {
                    android.util.Log.e("CarAdapter", "Coche es null en onClick");
                    android.widget.Toast.makeText(v.getContext(), "Error: Coche no disponible", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String carId = car.getId();
                android.util.Log.d("CarAdapter", "Abriendo detalles del coche: " + car.getFullName() + " (ID: " + carId + ")");
                
                // SIEMPRE pasar el ID primero (método más confiable)
                Intent intent = new Intent(v.getContext(), CarDetailActivity.class);
                
                // Pasar el ID como string (siempre funciona)
                if (carId != null && !carId.isEmpty()) {
                    intent.putExtra("carId", carId);
                    android.util.Log.d("CarAdapter", "ID pasado al intent: " + carId);
                } else {
                    android.util.Log.w("CarAdapter", "Advertencia: El coche no tiene ID");
                }
                
                // Intentar pasar el objeto completo como backup (puede fallar en algunos casos)
                try {
                    intent.putExtra("car", car);
                    android.util.Log.d("CarAdapter", "Objeto Car pasado al intent");
                } catch (Exception serializationException) {
                    android.util.Log.w("CarAdapter", "No se pudo serializar el objeto Car, usando solo ID: " + serializationException.getMessage());
                    // Continuar solo con el ID
                }
                
                // Verificar que el contexto sea válido
                if (v.getContext() != null) {
                    v.getContext().startActivity(intent);
                    // Animación de transición
                    if (v.getContext() instanceof android.app.Activity) {
                        ((android.app.Activity) v.getContext()).overridePendingTransition(
                            R.anim.slide_in_right, R.anim.fade_out);
                    }
                } else {
                    android.util.Log.e("CarAdapter", "Context es null");
                }
            } catch (Exception e) {
                android.util.Log.e("CarAdapter", "Error crítico al abrir detalles del coche", e);
                e.printStackTrace();
                android.widget.Toast.makeText(v.getContext(), "Error al abrir detalles. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList != null ? carList.size() : 0;
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCar;
        TextView textViewCarName;
        TextView textViewCarPrice;
        TextView textViewCarSpecs;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCar = itemView.findViewById(R.id.imageViewCar);
            textViewCarName = itemView.findViewById(R.id.textViewCarName);
            textViewCarPrice = itemView.findViewById(R.id.textViewCarPrice);
            textViewCarSpecs = itemView.findViewById(R.id.textViewCarSpecs);
        }
    }
}
