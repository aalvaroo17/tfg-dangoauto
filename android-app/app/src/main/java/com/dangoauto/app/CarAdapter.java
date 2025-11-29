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
            Glide.with(holder.itemView.getContext())
                .load(images.get(0))
                .placeholder(R.color.background_card)
                .error(R.color.background_card)
                .into(holder.imageViewCar);
        }
        
        // Click listener con animación
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CarDetailActivity.class);
            intent.putExtra("car", car);
            v.getContext().startActivity(intent);
            // Animación de transición
            if (v.getContext() instanceof android.app.Activity) {
                ((android.app.Activity) v.getContext()).overridePendingTransition(
                    R.anim.slide_in_right, R.anim.fade_out);
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
