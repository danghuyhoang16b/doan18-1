package com.example.app.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messageList;
    private int currentUserId;
    private String currentUserRole;

    public MessageAdapter(List<Message> messageList, int currentUserId, String currentUserRole) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvMessageContent.setText(message.getContent());
        holder.tvMessageTime.setText(message.getCreatedAt());
        android.view.View llActions = holder.itemView.findViewById(R.id.llRequestActions);
        android.widget.ImageButton btnApprove = holder.itemView.findViewById(R.id.btnApproveReq);
        android.widget.ImageButton btnReject = holder.itemView.findViewById(R.id.btnRejectReq);
        if (llActions != null) {
            llActions.setVisibility(android.view.View.GONE);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[REQ:(\\d+)\\]").matcher(message.getContent());
            if ("admin".equalsIgnoreCase(currentUserRole) && m.find()) {
                int reqId = Integer.parseInt(m.group(1));
                llActions.setVisibility(android.view.View.VISIBLE);
                btnApprove.setOnClickListener(v -> act(v, reqId, true));
                btnReject.setOnClickListener(v -> act(v, reqId, false));
            }
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tvMessageContent.getLayoutParams();
        LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) holder.tvMessageTime.getLayoutParams();
        LinearLayout layout = (LinearLayout) holder.tvMessageContent.getParent();

        if (message.getSenderId() == currentUserId) {
            layout.setGravity(Gravity.END);
            holder.tvMessageContent.setBackgroundResource(R.drawable.border_gray); // Should differentiate color, but using existing drawable
            // Ideally use a different background for sent messages
        } else {
            layout.setGravity(Gravity.START);
            holder.tvMessageContent.setBackgroundResource(R.drawable.border_gray);
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent, tvMessageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }
    private void act(android.view.View v, int id, boolean approve) {
        String token = com.example.app.utils.SharedPrefsUtils.getToken(v.getContext());
        com.example.app.api.ApiService api = com.example.app.utils.RetrofitClient.getClient().create(com.example.app.api.ApiService.class);
        java.util.Map<String,Object> body = new java.util.HashMap<>();
        body.put("request_id", id);
        body.put("approve", approve);
        api.approveTeacherRequest("Bearer " + token, body).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                android.widget.Toast.makeText(v.getContext(), approve? "Đã phê duyệt":"Đã từ chối", android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) { }
        });
    }
}
