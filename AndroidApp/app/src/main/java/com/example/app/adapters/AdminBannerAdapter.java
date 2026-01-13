package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.R;
import com.example.app.models.Banner;
import com.example.app.utils.RetrofitClient;

import java.util.List;

public class AdminBannerAdapter extends RecyclerView.Adapter<AdminBannerAdapter.ViewHolder> {

    private List<Banner> banners;
    private final OnBannerDeleteListener deleteListener;
    private final OnBannerToggleListener toggleListener;

    public interface OnBannerDeleteListener {
        void onDelete(Banner banner);
    }

    public interface OnBannerToggleListener {
        void onToggle(Banner banner, boolean isActive);
    }

    public AdminBannerAdapter(List<Banner> banners, OnBannerDeleteListener deleteListener, OnBannerToggleListener toggleListener) {
        this.banners = banners;
        this.deleteListener = deleteListener;
        this.toggleListener = toggleListener;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Banner banner = banners.get(position);
        holder.tvTitle.setText(banner.getTitle());
        holder.tvCta.setText(banner.getCtaText());
        
        String url = com.example.app.utils.UrlUtils.getFullUrl(holder.itemView.getContext(), banner.getImageUrl());
        
        Glide.with(holder.itemView.getContext())
                .load(url)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgBanner);
                
        holder.swActive.setOnCheckedChangeListener(null);
        holder.swActive.setChecked(banner.isActive());
        holder.swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (toggleListener != null) {
                toggleListener.onToggle(banner, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(banner);
            }
        });
    }

    @Override
    public int getItemCount() {
        return banners != null ? banners.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView tvTitle, tvCta;
        ImageButton btnDelete;
        androidx.appcompat.widget.SwitchCompat swActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCta = itemView.findViewById(R.id.tvCta);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            swActive = itemView.findViewById(R.id.swActive);
        }
    }
}
