package com.example.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app.R;
import com.example.app.network.ApiService;
import com.example.app.models.ClassModel;
import com.example.app.models.ClassRequest;
import com.example.app.models.ConductRule;
import com.example.app.models.Student;
import com.example.app.models.TokenRequest;
import com.example.app.models.ViolationRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViolationActivity extends AppCompatActivity {

    private Spinner spinnerClasses, spinnerStudents, spinnerRules;
    private EditText etNote;
    private Button btnSubmit;
    private android.widget.TextView tvPoints, tvStudentInfo;

    private ApiService apiService;
    private String token;

    private List<ClassModel> classList;
    private List<Student> studentList;
    private List<ConductRule> ruleList;

    private ClassModel selectedClass;
    private Student selectedStudent;
    private ConductRule selectedRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chấm nề nếp");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerClasses = findViewById(R.id.spinnerClasses);
        spinnerStudents = findViewById(R.id.spinnerStudents);
        spinnerRules = findViewById(R.id.spinnerRules);
        etNote = findViewById(R.id.etNote);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvPoints = findViewById(R.id.tvPoints);
        tvStudentInfo = findViewById(R.id.tvStudentInfo);

        token = SharedPrefsUtils.getToken(this);
        apiService = ApiClient.getInstance().getApiService();

        loadClasses();
        loadRules();

        btnSubmit.setOnClickListener(v -> submitViolation());
    }

    private void loadClasses() {
        apiService.getTeacherClasses("Bearer " + token).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    classList = response.body();
                    ArrayAdapter<ClassModel> adapter = new ArrayAdapter<>(ViolationActivity.this, android.R.layout.simple_spinner_item, classList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerClasses.setAdapter(adapter);

                    spinnerClasses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedClass = classList.get(position);
                            loadStudents(selectedClass.getId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi tải lớp: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi tải lớp: " + t.getMessage());
            }
        });
    }

    private void loadStudents(int classId) {
        java.util.Map<String,Integer> body = new java.util.HashMap<>();
        body.put("class_id", classId);
        apiService.listStudentsByClass("Bearer " + token, selectedClass.getId()).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        studentList = new ArrayList<>();
                        List<String> labels = new ArrayList<>();
                        java.text.Collator collator = java.text.Collator.getInstance(new java.util.Locale("vi", "VN"));
                        java.util.List<org.json.JSONObject> objs = new ArrayList<>();
                        for (int i=0;i<arr.length();i++) objs.add(arr.getJSONObject(i));
                        objs.sort((a,b)-> collator.compare(a.optString("full_name"), b.optString("full_name")));
                        for (org.json.JSONObject o : objs) {
                            Student st = new Student(o.optInt("id"), o.optString("full_name"), o.optString("code"));
                            studentList.add(st);
                            labels.add(o.optString("full_name") + " - " + o.optString("code"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ViolationActivity.this, android.R.layout.simple_spinner_item, labels);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerStudents.setAdapter(adapter);
                        spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedStudent = studentList.get(position);
                                try {
                                    org.json.JSONObject o = objs.get(position);
                                    String cls = o.optString("class");
                                    int cnt = o.optInt("violations_count", 0);
                                    tvStudentInfo.setText("Lớp: " + cls + ", Số lần vi phạm: " + cnt);
                                } catch (Exception ignored) {}
                                fetchPoints(selectedStudent.getId());
                            }
                            @Override public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    } else {
                        com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi tải học sinh: " + response.code());
                    }
                } catch (Exception e) {
                    com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi dữ liệu học sinh");
                }
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi tải học sinh");
            }
        });
    }

    private void loadRules() {
        apiService.getConductRules("Bearer " + token).enqueue(new Callback<List<ConductRule>>() {
            @Override
            public void onResponse(Call<List<ConductRule>> call, Response<List<ConductRule>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ruleList = response.body();
                    ArrayAdapter<ConductRule> adapter = new ArrayAdapter<>(ViolationActivity.this, android.R.layout.simple_spinner_item, ruleList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRules.setAdapter(adapter);

                    spinnerRules.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedRule = ruleList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ConductRule>> call, Throwable t) {
                com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi tải quy định");
            }
        });
    }

    private void submitViolation() {
        if (selectedStudent == null || selectedRule == null) {
            com.example.app.utils.ToastManager.show(this, "Vui lòng chọn đầy đủ thông tin");
            return;
        }

        String note = etNote.getText().toString();
        ViolationRequest request = new ViolationRequest(selectedStudent.getId(), selectedRule.getId(), note);

        apiService.submitViolation("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    com.example.app.utils.ToastManager.show(ViolationActivity.this, "Ghi nhận vi phạm thành công");
                    if (selectedStudent != null) fetchPoints(selectedStudent.getId());
                    finish();
                } else {
                    if (response.code() == 403) {
                        com.example.app.utils.ToastManager.show(ViolationActivity.this, "Bạn không được phân công lớp này");
                    } else {
                        com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi khi lưu vi phạm");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                com.example.app.utils.ToastManager.show(ViolationActivity.this, "Lỗi kết nối");
            }
        });
    }
    private void fetchPoints(int studentId) {
        java.util.Map<String,Integer> body = new java.util.HashMap<>();
        body.put("student_id", studentId);
        apiService.getPoints("Bearer " + token, studentId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        int points = o.optInt("points", 100);
                        tvPoints.setText("Điểm nề nếp: " + points);
                    }
                } catch (Exception ignored) {}
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) { }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
