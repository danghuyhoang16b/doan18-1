package com.example.app.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.Student;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<Student> studentList;

    public AttendanceAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_student, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvStudentName.setText(student.getFullName());
        holder.tvStudentCode.setText(student.getCode());
        holder.etNote.setText(student.getNote());
        if (holder.ivAvatar != null && student.getAvatar() != null) {
            loadImage(holder.ivAvatar, student.getAvatar());
        }

        // Reset listener to prevent triggering when binding
        holder.rgStatus.setOnCheckedChangeListener(null);
        
        switch (student.getStatus()) {
            case "present":
                holder.rbPresent.setChecked(true);
                break;
            case "absent":
                holder.rbAbsent.setChecked(true);
                break;
            case "late":
                holder.rbLate.setChecked(true);
                break;
        }

        holder.rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPresent) student.setStatus("present");
            else if (checkedId == R.id.rbAbsent) student.setStatus("absent");
            else if (checkedId == R.id.rbLate) student.setStatus("late");
        });

        // Use a simple TextWatcher, careful with recycling
        // Ideally remove old watcher before adding new, but for simplicity here:
        if (holder.etNote.getTag() instanceof TextWatcher) {
            holder.etNote.removeTextChangedListener((TextWatcher) holder.etNote.getTag());
        }
        
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                student.setNote(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        holder.etNote.addTextChangedListener(watcher);
        holder.etNote.setTag(watcher);
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentCode;
        RadioGroup rgStatus;
        RadioButton rbPresent, rbAbsent, rbLate;
        EditText etNote;
        android.widget.ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            rgStatus = itemView.findViewById(R.id.rgStatus);
            rbPresent = itemView.findViewById(R.id.rbPresent);
            rbAbsent = itemView.findViewById(R.id.rbAbsent);
            rbLate = itemView.findViewById(R.id.rbLate);
            etNote = itemView.findViewById(R.id.etNote);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }

    private void loadImage(android.widget.ImageView target, String avatar) {
        // String base = "http://103.252.136.73:8080/uploads/avatars/";
        String base = "http://10.0.2.2/Backend/uploads/avatars/";
        String url = avatar.startsWith("http") ? avatar : base + avatar;
        new Thread(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setConnectTimeout(5000); conn.setReadTimeout(7000); conn.connect();
                try (java.io.InputStream is = conn.getInputStream()) {
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
                    android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                    h.post(() -> target.setImageBitmap(bmp));
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
