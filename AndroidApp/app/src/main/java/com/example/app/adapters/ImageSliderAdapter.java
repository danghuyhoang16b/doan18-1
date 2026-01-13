package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
    private final List<Integer> imageResIds;
    private final List<String> titles;
    private final List<String> ctas;
    private final View.OnClickListener ctaListener;

    public ImageSliderAdapter(List<Integer> imageResIds, List<String> titles, List<String> ctas, View.OnClickListener ctaListener) {
        this.imageResIds = imageResIds;
        this.titles = titles;
        this.ctas = ctas;
        this.ctaListener = ctaListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.image.setImageResource(imageResIds.get(position));
        String title = titles != null && titles.size() > position ? titles.get(position) : "";
        String cta = ctas != null && ctas.size() > position ? ctas.get(position) : "";
        holder.title.setText(title);
        holder.cta.setText(cta.isEmpty() ? holder.itemView.getContext().getString(R.string.view_more) : cta);
        holder.cta.setOnClickListener(ctaListener);
    }

    @Override
    public int getItemCount() {
        return imageResIds != null ? imageResIds.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        Button cta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.sliderImage);
            title = itemView.findViewById(R.id.sliderTitle);
            cta = itemView.findViewById(R.id.sliderCta);
        }
    }
}
