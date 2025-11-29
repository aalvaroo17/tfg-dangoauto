package com.dangoauto.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CarImagesAdapter extends RecyclerView.Adapter<CarImagesAdapter.ImageViewHolder> {
    
    private List<String> imageUrls;
    
    public CarImagesAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_car_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        
        Glide.with(holder.itemView.getContext())
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.color.background_card)
            .error(R.color.background_card)
            .fallback(R.color.background_card)
            .into(holder.imageView);
    }
    
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewCarImage);
        }
    }
}

