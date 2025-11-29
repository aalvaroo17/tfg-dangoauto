package com.dangoauto.app;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class SelectedPhotosAdapter extends RecyclerView.Adapter<SelectedPhotosAdapter.PhotoViewHolder> {
    
    private List<Uri> photoUris;
    private OnPhotoRemoveListener removeListener;
    
    public interface OnPhotoRemoveListener {
        void onPhotoRemove(int position);
    }
    
    public SelectedPhotosAdapter(List<Uri> photoUris, OnPhotoRemoveListener removeListener) {
        this.photoUris = photoUris;
        this.removeListener = removeListener;
    }
    
    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_selected_photo, parent, false);
        return new PhotoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);
        
        Glide.with(holder.itemView.getContext())
            .load(photoUri)
            .centerCrop()
            .placeholder(R.color.background_secondary)
            .error(R.color.background_secondary)
            .into(holder.imageViewPhoto);
        
        holder.fabRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onPhotoRemove(holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return photoUris != null ? photoUris.size() : 0;
    }
    
    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPhoto;
        FloatingActionButton fabRemove;
        
        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            fabRemove = itemView.findViewById(R.id.fabRemove);
        }
    }
}

