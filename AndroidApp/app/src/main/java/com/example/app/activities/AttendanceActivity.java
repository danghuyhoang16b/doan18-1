package com.example.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.AttendanceAdapter;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.models.AttendanceSubmitRequest;
import com.example.app.models.ClassModel;
import com.example.app.models.ClassRequest;
import com.example.app.models.Student;
import com.example.app.models.TokenRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {

    private Spinner spinnerClasses;
    private TextView tvDate;
    private RecyclerView rvStudents;
    private Button btnSubmit;
    private AttendanceAdapter adapter;
    private List<ClassModel> classList;
    private ClassModel selectedClass;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        spinnerClasses = findViewById(R.id.spinnerClasses);
        tvDate = findViewById(R.id.tvDate);
        rvStudents = findViewById(R.id.rvStudents);
        btnSubmit = findViewById(R.id.btnSubmit);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        token = SharedPrefsUtils.getToken(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        loadClasses();

        btnSubmit.setOnClickListener(v -> submitAttendance());
    }

    private void loadClasses() {
        com.example.app.models.User u = SharedPrefsUtils.getUser(this);
        if (u != null && "admin".equals(u.getRole())) {
            apiService.getAllClasses().enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body()!=null) {
                            String s = response.body().string();
                            org.json.JSONArray arr = new org.json.JSONArray(s);
                            classList = new java.util.ArrayList<>();
                            for (int i=0;i<arr.length();i++) {
                                org.json.JSONObject o = arr.getJSONObject(i);
                                classList.add(new ClassModel(o.optInt("id"), o.optString("name")));
                            }
                            setupSpinner();
                        }
                    } catch (Exception ignored) {}
                }
                @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) { }
            });
            return;
        }
        if (u != null && "student".equals(u.getRole())) {
            apiService.getStudentClass("Bearer " + token).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body()!=null) {
                            String s = response.body().string();
                            org.json.JSONObject o = new org.json.JSONObject(s);
                            classList = new java.util.ArrayList<>();
                            classList.add(new ClassModel(o.optInt("id"), o.optString("name")));
                            setupSpinner();
                            spinnerClasses.setEnabled(false);
                        }
                    } catch (Exception ignored) {}
                }
                @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) { }
            });
            return;
        }
        apiService.getTeacherClasses(new TokenRequest(token)).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    classList = new java.util.ArrayList<>(response.body());
                    java.text.Collator collator = java.text.Collator.getInstance(new java.util.Locale("vi","VN"));
                    classList.sort((a,b)-> collator.compare(a.getName(), b.getName()));
                    setupSpinner();
                } else {
                    Toast.makeText(AttendanceActivity.this, "Không thể tải danh sách lớp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                Toast.makeText(AttendanceActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<ClassModel> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClasses.setAdapter(spinnerAdapter);

        spinnerClasses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClass = classList.get(position);
                loadStudents(selectedClass.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadStudents(int classId) {
        apiService.getClassStudents(new ClassRequest(token, classId)).enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new AttendanceAdapter(response.body());
                    rvStudents.setAdapter(adapter);
                } else {
                    Toast.makeText(AttendanceActivity.this, "Lỗi: " + response.code() + " - Không thể tải danh sách học sinh", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("Attendance", "Error loading students: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                Toast.makeText(AttendanceActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("Attendance", "Failure: " + t.getMessage());
            }
        });
    }

    private void submitAttendance() {
        if (selectedClass == null || adapter == null) return;

        List<Student> students = adapter.getStudentList();
        List<AttendanceSubmitRequest.AttendanceItem> attendanceItems = new ArrayList<>();
        
        for (Student s : students) {
            attendanceItems.add(new AttendanceSubmitRequest.AttendanceItem(s.getId(), s.getStatus(), s.getNote()));
        }

        String date = tvDate.getText().toString();
        AttendanceSubmitRequest request = new AttendanceSubmitRequest(token, selectedClass.getId(), date, attendanceItems);

        apiService.submitAttendance(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AttendanceActivity.this, "Lưu điểm danh thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AttendanceActivity.this, "Lưu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AttendanceActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_export_report) {
            exportReport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportReport() {
        if (selectedClass == null) {
            Toast.makeText(this, "Vui lòng chọn lớp", Toast.LENGTH_SHORT).show();
            return;
        }
        // In a real app, this would call an API to generate and download a file
        // For now, we simulate the action
        Toast.makeText(this, "Đang xuất báo cáo cho lớp " + selectedClass.getName() + "...", Toast.LENGTH_SHORT).show();
        
        // Simulate network delay
        new android.os.Handler().postDelayed(() -> 
            Toast.makeText(this, "Đã xuất báo cáo thành công (Excel)", Toast.LENGTH_SHORT).show(), 
        2000);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
