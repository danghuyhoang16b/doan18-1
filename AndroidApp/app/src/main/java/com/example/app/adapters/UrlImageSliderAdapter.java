package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.app.R;

import java.util.List;

public class UrlImageSliderAdapter extends RecyclerView.Adapter<UrlImageSliderAdapter.ViewHolder> {
    private final List<String> urls;
    private final List<String> titles;
    private final List<String> ctas;
    public interface OnBannerClickListener {
        void onBannerClick(View view, int position);
    }

    private final OnBannerClickListener ctaListener;

    public UrlImageSliderAdapter(List<String> urls, List<String> titles, List<String> ctas, OnBannerClickListener ctaListener) {
        this.urls = urls;
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
        String url = urls.get(position);
        holder.title.setText(titles != null && titles.size() > position ? titles.get(position) : "");
        holder.cta.setText(ctas != null && ctas.size() > position ? ctas.get(position) : holder.itemView.getContext().getString(R.string.view_more));
        
        holder.cta.setOnClickListener(v -> {
            if (ctaListener != null) {
                ctaListener.onBannerClick(v, position);
            }
        });
        
        // Also make the whole image clickable
        holder.itemView.setOnClickListener(v -> {
             if (ctaListener != null) {
                 ctaListener.onBannerClick(v, position);
             }
        });
        
        Glide.with(holder.itemView.getContext())
             .load(url)
             .placeholder(R.drawable.ic_launcher_background) // Add a placeholder if you have one, or remove
             .error(android.R.drawable.stat_notify_error) // Show error icon if load fails
             .centerCrop()
             .listener(new RequestListener<Drawable>() {
                 @Override
                 public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                     android.util.Log.e("UrlImageSliderAdapter", "Load failed: " + model, e);
                     return false;
                 }
                 @Override
                 public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                     android.util.Log.d("UrlImageSliderAdapter", "Loaded: " + model);
                     return false;
                 }
             })
             .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return urls != null ? urls.size() : 0;
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
