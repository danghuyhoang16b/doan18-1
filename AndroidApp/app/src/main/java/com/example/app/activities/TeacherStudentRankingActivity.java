package com.example.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.network.ApiClient;
import com.example.app.network.ApiService;
import com.example.app.utils.SharedPrefsUtils;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherStudentRankingActivity extends AppCompatActivity {
    private TextInputEditText etStartDate, etEndDate;
    private Button btnLoadStats;
    private ProgressBar progressBar;
    private RecyclerView rvStudentRanking;
    private TextView tvEmpty;
    private ApiService apiService;
    private final Calendar calendar = Calendar.getInstance();
    private int classId = 0; // Default classId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_student_ranking);

        // Get classId from intent if passed, otherwise default to 0 (backend will auto-detect)
        classId = getIntent().getIntExtra("class_id", 0);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnLoadStats = findViewById(R.id.btnLoadStats);
        progressBar = findViewById(R.id.progressBar);
        rvStudentRanking = findViewById(R.id.rvStudentRanking);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        rvStudentRanking.setLayoutManager(new LinearLayoutManager(this));
        apiService = ApiClient.getInstance().getApiService();

        // Setup DatePickers
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // Default dates: Start of month to today
        updateDateLabel(etEndDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        updateDateLabel(etStartDate);

        btnLoadStats.setOnClickListener(v -> loadStudentRanking());
        
        // Auto load first time
        loadStudentRanking();
    }

    private void showDatePicker(EditText target) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            updateDateLabel(target);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel(EditText target) {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        target.setText(sdf.format(calendar.getTime()));
    }

    private void loadStudentRanking() {
        String start = etStartDate.getText().toString();
        String end = etEndDate.getText().toString();
        String token = SharedPrefsUtils.getToken(this);

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvStudentRanking.setVisibility(View.GONE);

        apiService.getStudentRanking("Bearer " + token, classId, start, end).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONObject resObj = new JSONObject(json);
                        
                        // Check if success
                        if (resObj.optBoolean("success", false)) {
                            JSONArray data = resObj.optJSONArray("data");
                            if (data != null && data.length() > 0) {
                                displayRanking(data);
                            } else {
                                tvEmpty.setText("Không có dữ liệu vi phạm trong khoảng thời gian này");
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            String msg = resObj.optString("message", "Lỗi tải dữ liệu");
                            Toast.makeText(TeacherStudentRankingActivity.this, msg, Toast.LENGTH_SHORT).show();
                            tvEmpty.setText(msg);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(TeacherStudentRankingActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(TeacherStudentRankingActivity.this, "Lỗi xử lý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TeacherStudentRankingActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRanking(JSONArray arr) {
        List<StudentRankItem> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null) {
                list.add(new StudentRankItem(
                    o.optInt("student_id"),
                    o.optString("full_name"),
                    o.optString("student_code"),
                    o.optInt("total_deducted"),
                    o.optInt("violation_count")
                ));
            }
        }
        
        rvStudentRanking.setAdapter(new StudentRankAdapter(list));
        rvStudentRanking.setVisibility(View.VISIBLE);
    }

    // Models & Adapter
    static class StudentRankItem {
        int id, deducted, count;
        String name, code;
        StudentRankItem(int id, String name, String code, int deducted, int count) {
            this.id = id; this.name = name; this.code = code; 
            this.deducted = deducted; this.count = count;
        }
    }

    class StudentRankAdapter extends RecyclerView.Adapter<StudentRankAdapter.VH> {
        List<StudentRankItem> list;
        StudentRankAdapter(List<StudentRankItem> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            StudentRankItem item = list.get(position);
            holder.t1.setText("#" + (position + 1) + " " + item.name + " (" + item.code + ")");
            
            // Format text color based on deducted points
            String details = "Điểm trừ: " + item.deducted + " | Số lần vi phạm: " + item.count;
            holder.t2.setText(details);
            
            if (item.deducted > 0) {
                holder.t2.setTextColor(android.graphics.Color.RED);
            } else {
                holder.t2.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}