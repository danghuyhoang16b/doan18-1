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
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
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
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getAllClasses().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        List<String> names = new ArrayList<>();
                        for (int i=0;i<arr.length();i++) names.add(arr.getJSONObject(i).optString("name"));
                        spClass.setAdapter(new ArrayAdapter<>(ScoreEntryActivity.this, android.R.layout.simple_spinner_item, names));
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
                        List<String> names = new ArrayList<>();
                        for (int i=0;i<arr.length();i++) names.add(arr.getJSONObject(i).optString("name"));
                        spSubject.setAdapter(new ArrayAdapter<>(ScoreEntryActivity.this, android.R.layout.simple_spinner_item, names));
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void loadStudents() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Map<String,Integer> map = new HashMap<>();
        // simple fetch id by name call; for demo assume id=1 if backend lacks resolver
        map.put("class_id", 1);
        api.listStudentsForScore("Bearer " + token, map).enqueue(new Callback<ResponseBody>() {
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
                    }
                } catch (Exception e) { Toast.makeText(ScoreEntryActivity.this,"Lỗi dữ liệu",Toast.LENGTH_SHORT).show(); }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { Toast.makeText(ScoreEntryActivity.this,"Lỗi tải học sinh",Toast.LENGTH_SHORT).show(); }
        });
    }
    private void saveScores() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Map<String,Object> body = new HashMap<>();
        body.put("class_id", 1);
        body.put("subject_id", 1);
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
