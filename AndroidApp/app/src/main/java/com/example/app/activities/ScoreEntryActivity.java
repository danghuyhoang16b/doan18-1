package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.app.R;
import com.example.app.models.ClassModel;
import com.example.app.models.SubjectModel;
import com.example.app.network.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.network.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreEntryActivity extends AppCompatActivity {
    private Spinner spClass, spSubject;
    private EditText etTerm;
    private android.widget.LinearLayout listContainer;
    private Button btnLoad, btnSave;
    private List<JSONObject> students = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_entry);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Chấm điểm theo lớp");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        spClass = findViewById(R.id.spClass);
        spSubject = findViewById(R.id.spSubject);
        etTerm = findViewById(R.id.etTerm);
        listContainer = findViewById(R.id.listContainer);
        btnLoad = findViewById(R.id.btnLoad);
        btnSave = findViewById(R.id.btnSave);
        loadClassSubject();
        btnLoad.setOnClickListener(v -> loadStudents());
        btnSave.setOnClickListener(v -> saveScores());
    }
    private void loadClassSubject() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        api.getAllClasses().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        List<ClassModel> classes = new ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            JSONObject o = arr.getJSONObject(i);
                            classes.add(new ClassModel(o.optInt("id"), o.optString("name")));
                        }
                        ArrayAdapter<ClassModel> adapter = new ArrayAdapter<>(ScoreEntryActivity.this, android.R.layout.simple_spinner_item, classes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spClass.setAdapter(adapter);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
        api.getAllSubjects().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        List<SubjectModel> subjects = new ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            JSONObject o = arr.getJSONObject(i);
                            subjects.add(new SubjectModel(o.optInt("id"), o.optString("name")));
                        }
                        ArrayAdapter<SubjectModel> adapter = new ArrayAdapter<>(ScoreEntryActivity.this, android.R.layout.simple_spinner_item, subjects);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spSubject.setAdapter(adapter);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void loadStudents() {
        if (spClass.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn lớp", Toast.LENGTH_SHORT).show();
            return;
        }
        int classId = ((ClassModel) spClass.getSelectedItem()).getId();

        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        Map<String,Integer> body = new HashMap<>();
        body.put("class_id", classId);
        
        api.listStudentsForScore("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        students.clear();
                        listContainer.removeAllViews();
                        for (int i=0;i<arr.length();i++) {
                            JSONObject o = arr.getJSONObject(i);
                            students.add(o);
                            android.widget.LinearLayout row = new android.widget.LinearLayout(ScoreEntryActivity.this);
                            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                            android.widget.TextView tv = new android.widget.TextView(ScoreEntryActivity.this);
                            tv.setText(o.optString("full_name")+" - "+o.optString("code"));
                            tv.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            EditText et = new EditText(ScoreEntryActivity.this);
                            et.setHint("Điểm");
                            et.setTag("score_" + o.optInt("id"));
                            row.addView(tv);
                            row.addView(et);
                            listContainer.addView(row);
                        }
                        if (students.isEmpty()) {
                            Toast.makeText(ScoreEntryActivity.this, "Không có học sinh trong lớp này", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                        Toast.makeText(ScoreEntryActivity.this, "Lỗi tải: " + response.code() + " - " + err, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) { 
                    e.printStackTrace();
                    Toast.makeText(ScoreEntryActivity.this,"Lỗi dữ liệu: " + e.getMessage(),Toast.LENGTH_SHORT).show(); 
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { 
                t.printStackTrace();
                Toast.makeText(ScoreEntryActivity.this,"Lỗi kết nối: " + t.getMessage(),Toast.LENGTH_SHORT).show(); 
            }
        });
    }
    private void saveScores() {
        if (spClass.getSelectedItem() == null || spSubject.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn lớp và môn học", Toast.LENGTH_SHORT).show();
            return;
        }
        int classId = ((ClassModel) spClass.getSelectedItem()).getId();
        int subjectId = ((SubjectModel) spSubject.getSelectedItem()).getId();

        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        Map<String,Object> body = new HashMap<>();
        body.put("class_id", classId);
        body.put("subject_id", subjectId);
        body.put("term", etTerm.getText().toString().trim().isEmpty() ? "HK1" : etTerm.getText().toString().trim());
        List<Map<String,Object>> items = new ArrayList<>();
        for (JSONObject o: students) {
            int sid = o.optInt("id");
            EditText etScore = listContainer.findViewWithTag("score_" + sid);
            if (etScore!=null) {
                String val = etScore.getText().toString().trim();
                if (!val.isEmpty()) {
                    Map<String,Object> it = new HashMap<>();
                    it.put("student_id", sid);
                    it.put("score", Float.parseFloat(val));
                    items.add(it);
                }
            }
        }
        body.put("items", items);
        api.submitScores("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(ScoreEntryActivity.this,"Đã lưu điểm",Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { Toast.makeText(ScoreEntryActivity.this,"Lỗi lưu điểm",Toast.LENGTH_SHORT).show(); }
        });
    }
    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
