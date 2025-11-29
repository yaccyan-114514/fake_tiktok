package com.example.fake_tiktok;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_ADD = 0;
    private static final int TYPE_IMAGE = 1;
    
    private List<Uri> images;
    private OnImageClickListener listener;
    
    public interface OnImageClickListener {
        void onAddImageClick();
        void onDeleteImageClick(int position);
    }
    
    public ImageAdapter(List<Uri> images, OnImageClickListener listener) {
        this.images = images;
        this.listener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        return position == images.size() ? TYPE_ADD : TYPE_IMAGE;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_image, parent, false);
            return new AddImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            Uri imageUri = images.get(position);
            imageHolder.imageView.setImageURI(imageUri);
            int finalPosition = position;
            imageHolder.deleteButton.setOnClickListener(v -> {
                if (listener != null && finalPosition < images.size()) {
                    listener.onDeleteImageClick(finalPosition);
                }
            });
        } else if (holder instanceof AddImageViewHolder) {
            AddImageViewHolder addHolder = (AddImageViewHolder) holder;
            addHolder.addButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddImageClick();
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return images.size() + 1; // +1 for add button
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;
        
        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
    
    static class AddImageViewHolder extends RecyclerView.ViewHolder {
        ImageView addButton;
        
        AddImageViewHolder(View itemView) {
            super(itemView);
            addButton = itemView.findViewById(R.id.addButton);
        }
    }
}

