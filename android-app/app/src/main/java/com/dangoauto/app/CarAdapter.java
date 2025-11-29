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

    private static final String TAG = "CarAdapter";
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
        
        if (car == null) {
            android.util.Log.e(TAG, "Car es null en posicion " + position);
            return;
        }
        
        // Nombre
        String fullName = car.getFullName();
        holder.textViewCarName.setText(fullName != null ? fullName : "Sin nombre");
        
        // Precio en badge
        String price = car.getFormattedPrice();
        holder.textViewCarPrice.setText(price != null ? price : "0 EUR");
        
        // Especificaciones en una linea
        StringBuilder specs = new StringBuilder();
        specs.append(car.getYear()).append(" | ");
        specs.append(String.format("%,d", car.getKm())).append(" km | ");
        specs.append(car.getFuel() != null ? car.getFuel() : "N/A");
        holder.textViewCarSpecs.setText(specs.toString());
        
        // Imagen
        List<String> images = car.getImages();
        if (images != null && !images.isEmpty() && images.get(0) != null && !images.get(0).isEmpty()) {
            String imageUrl = images.get(0);
            Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.color.background_secondary)
                .error(R.color.background_secondary)
                .centerCrop()
                .into(holder.imageViewCar);
        } else {
            holder.imageViewCar.setBackgroundResource(R.color.background_secondary);
        }
        
        // Click listener - Solo pasar el ID
        holder.itemView.setOnClickListener(v -> {
            String carId = car.getId();
            android.util.Log.d(TAG, "Click en coche: " + fullName + " (ID: " + carId + ")");
            
            if (carId == null || carId.isEmpty()) {
                android.util.Log.e(TAG, "El coche no tiene ID valido");
                android.widget.Toast.makeText(v.getContext(), "Error: Coche sin identificador", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                Intent intent = new Intent(v.getContext(), CarDetailActivity.class);
                intent.putExtra("carId", carId);
                v.getContext().startActivity(intent);
                
                // Animacion de transicion
                if (v.getContext() instanceof android.app.Activity) {
                    ((android.app.Activity) v.getContext()).overridePendingTransition(
                        R.anim.slide_in_right, R.anim.fade_out);
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error abriendo detalles", e);
                android.widget.Toast.makeText(v.getContext(), "Error al abrir detalles", android.widget.Toast.LENGTH_SHORT).show();
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
