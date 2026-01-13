package com.example.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.activities.ChatActivity;
import com.example.app.models.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> contactList;
    private Context context;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.tvContactName.setText(contact.getFullName());
        holder.tvContactRole.setText(contact.getRole());
        android.view.View llActions = holder.itemView.findViewById(R.id.llAdminActions);
        android.widget.ImageButton btnApprove = holder.itemView.findViewById(R.id.btnApproveAll);
        android.widget.ImageButton btnReject = holder.itemView.findViewById(R.id.btnRejectAll);
        String role = com.example.app.utils.SharedPrefsUtils.getUser(context).getRole();
        if ("admin".equalsIgnoreCase(role)) {
            llActions.setVisibility(android.view.View.VISIBLE);
            btnApprove.setOnClickListener(v -> {
                android.content.Intent it = new android.content.Intent(context, com.example.app.activities.AdminRequestListActivity.class);
                it.putExtra("teacher_id", contact.getId());
                it.putExtra("action", "approve");
                context.startActivity(it);
            });
            btnReject.setOnClickListener(v -> {
                android.content.Intent it = new android.content.Intent(context, com.example.app.activities.AdminRequestListActivity.class);
                it.putExtra("teacher_id", contact.getId());
                it.putExtra("action", "reject");
                context.startActivity(it);
            });
        } else {
            if (llActions != null) llActions.setVisibility(android.view.View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("contact_id", contact.getId());
            intent.putExtra("contact_name", contact.getFullName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contactList != null ? contactList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvContactRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactRole = itemView.findViewById(R.id.tvContactRole);
        }
    }
}
