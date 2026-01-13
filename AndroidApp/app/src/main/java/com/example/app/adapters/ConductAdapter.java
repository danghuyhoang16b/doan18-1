package com.example.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.Conduct;

import java.util.List;

public class ConductAdapter extends RecyclerView.Adapter<ConductAdapter.ViewHolder> {

    private List<Conduct> conductList;

    public ConductAdapter(List<Conduct> conductList) {
        this.conductList = conductList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conduct, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conduct conduct = conductList.get(position);
        holder.tvContent.setText(conduct.getContent());
        holder.tvDate.setText(conduct.getDate());

        if ("reward".equals(conduct.getType())) {
            holder.tvType.setText("KHEN THƯỞNG");
            holder.tvType.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvType.setText("KỶ LUẬT");
            holder.tvType.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return conductList != null ? conductList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
