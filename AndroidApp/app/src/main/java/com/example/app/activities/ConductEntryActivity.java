package com.example.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

public class ConductEntryActivity extends AppCompatActivity {
    private Spinner spClass;
    private LinearLayout listContainer;
    private Button btnLoad, btnSave;
    private List<JSONObject> students = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduct_entry);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Chấm lề nếp");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        spClass = findViewById(R.id.spClass);
        listContainer = findViewById(R.id.listContainer);
        btnLoad = findViewById(R.id.btnLoad);
        btnSave = findViewById(R.id.btnSave);
        btnLoad.setEnabled(false);
        loadClasses();
        btnLoad.setOnClickListener(v -> loadStudents());
        btnSave.setOnClickListener(v -> saveResults());
    }
    private void loadClasses() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getAllClasses().enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> resp) {
                try {
                    if (resp.isSuccessful() && resp.body()!=null) {
                        String s = resp.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        java.util.List<com.example.app.models.ClassModel> list = new java.util.ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);
                            list.add(new com.example.app.models.ClassModel(o.optInt("id"), o.optString("name")));
                        }
                        ArrayAdapter<com.example.app.models.ClassModel> adapter = new ArrayAdapter<>(ConductEntryActivity.this, android.R.layout.simple_spinner_item, list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spClass.setAdapter(adapter);
                        boolean hasData = !list.isEmpty();
                        spClass.setEnabled(hasData);
                        btnLoad.setEnabled(hasData);
                        if (hasData) spClass.setSelection(0);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) { }
        });
    }
    private void loadStudents() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        com.example.app.models.ClassModel cls = (com.example.app.models.ClassModel) spClass.getSelectedItem();
        if (cls == null) {
            android.widget.Toast.makeText(this, "Vui lòng chọn lớp", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        java.util.Map<String,Integer> body = new java.util.HashMap<>();
        body.put("class_id", cls.getId());
        api.listStudentsByClass("Bearer " + token, cls.getId()).enqueue(new Callback<ResponseBody>() {
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
                            LinearLayout row = new LinearLayout(ConductEntryActivity.this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            TextView tv = new TextView(ConductEntryActivity.this);
                            tv.setText(o.optString("full_name")+" - "+o.optString("code"));
                            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
                            EditText etScore = new EditText(ConductEntryActivity.this);
                            etScore.setHint("Điểm (0-10)");
                            etScore.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            etScore.setTag("score_"+o.optInt("id"));
                            EditText etCmt = new EditText(ConductEntryActivity.this);
                            etCmt.setHint("Nhận xét");
                            etCmt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
                            etCmt.setTag("cmt_"+o.optInt("id"));
                            row.addView(tv); row.addView(etScore); row.addView(etCmt);
                            listContainer.addView(row);
                        }
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void saveResults() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        com.example.app.models.ClassModel cls = (com.example.app.models.ClassModel) spClass.getSelectedItem();
        Map<String,Object> body = new HashMap<>();
        body.put("class_id", cls.getId());
        List<Map<String,Object>> items = new ArrayList<>();
        for (JSONObject o : students) {
            int id = o.optInt("id");
            EditText etScore = listContainer.findViewWithTag("score_"+id);
            EditText etCmt = listContainer.findViewWithTag("cmt_"+id);
            if (etScore!=null) {
                String sScore = etScore.getText().toString().trim();
                if (!sScore.isEmpty()) {
                    Map<String,Object> it = new HashMap<>();
                    it.put("student_id", id);
                    it.put("score", Integer.parseInt(sScore));
                    it.put("comment", etCmt!=null? etCmt.getText().toString().trim() : "");
                    items.add(it);
                }
            }
        }
        body.put("items", items);
        api.submitConductResults("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { finish(); }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
