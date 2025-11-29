package com.example.fake_tiktok;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @对象选择适配器
 */
public class MentionOptionAdapter extends RecyclerView.Adapter<MentionOptionAdapter.ViewHolder> {
    
    public interface OnItemClickListener {
        void onItemClick(String option);
    }
    
    private List<String> options;
    private OnItemClickListener listener;
    
    public MentionOptionAdapter(List<String> options, OnItemClickListener listener) {
        this.options = options != null ? options : new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mention_option, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String option = options.get(position);
        holder.textViewOption.setText(option);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(option);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return options.size();
    }
    
    public void updateData(List<String> newOptions) {
        this.options = newOptions != null ? newOptions : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOption;
        
        ViewHolder(View itemView) {
            super(itemView);
            textViewOption = itemView.findViewById(R.id.textViewOption);
        }
    }
}

