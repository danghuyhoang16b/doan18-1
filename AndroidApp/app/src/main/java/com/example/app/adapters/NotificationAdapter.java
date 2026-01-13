package com.example.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Object> items;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.items = buildSectioned(notificationList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_notification_header, parent, false);
            return new HeaderHolder(v);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            return new ItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object obj = items.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).tvHeader.setText((String) obj);
        } else {
            Notification notification = (Notification) obj;
            ItemHolder ih = (ItemHolder) holder;
            ih.tvTitle.setText(notification.getTitle());
            ih.tvContent.setText(notification.getContent());
            ih.tvTime.setText(notification.getCreatedAt());
            String priority = notification.getPriority();
            if (priority != null) {
                ih.tvPriority.setText(priority.toUpperCase());
                if ("urgent".equals(priority)) {
                    ih.tvPriority.setBackgroundColor(Color.RED);
                    ih.tvPriority.setTextColor(Color.WHITE);
                } else if ("high".equals(priority)) {
                    ih.tvPriority.setBackgroundColor(Color.parseColor("#FF9800"));
                    ih.tvPriority.setTextColor(Color.WHITE);
                } else {
                    ih.tvPriority.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    ih.tvPriority.setTextColor(Color.BLACK);
                }
            } else {
                ih.tvPriority.setText("NORMAL");
                ih.tvPriority.setBackgroundColor(Color.parseColor("#E0E0E0"));
                ih.tvPriority.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    private List<Object> buildSectioned(List<Notification> source) {
        List<Object> result = new ArrayList<>();
        List<Notification> urgent = new ArrayList<>();
        List<Notification> high = new ArrayList<>();
        List<Notification> normal = new ArrayList<>();
        if (source != null) {
            for (Notification n : source) {
                String p = n.getPriority();
                if ("urgent".equals(p)) urgent.add(n);
                else if ("high".equals(p)) high.add(n);
                else normal.add(n);
            }
        }
        Comparator<Notification> byCreatedDesc = (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt());
        Collections.sort(urgent, byCreatedDesc);
        Collections.sort(high, byCreatedDesc);
        Collections.sort(normal, byCreatedDesc);
        if (!urgent.isEmpty()) {
            result.add("Khẩn cấp");
            result.addAll(urgent);
        }
        if (!high.isEmpty()) {
            result.add("Mới nhất");
            result.addAll(high);
        }
        if (!normal.isEmpty()) {
            result.add("Bình thường");
            result.addAll(normal);
        }
        return result;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime, tvPriority;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }
}
