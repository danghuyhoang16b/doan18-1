package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.app.R;
import com.example.app.network.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.network.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class AdminScheduleActivity extends AppCompatActivity {
    private Spinner spClass, spSubject;
    private Button btnAuto, btnSave;
    private TableLayout table;
    private JSONArray suggestions;
    private java.util.List<org.json.JSONObject> classes = new java.util.ArrayList<>();
    private java.util.List<org.json.JSONObject> subjects = new java.util.ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Quản lý thời khóa biểu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        spClass = findViewById(R.id.spClass);
        spSubject = findViewById(R.id.spSubject);
        btnAuto = findViewById(R.id.btnAuto);
        btnSave = findViewById(R.id.btnSave);
        table = findViewById(R.id.tableSchedule);
        loadClassSubject();
        btnAuto.setOnClickListener(v -> auto());
        btnSave.setOnClickListener(v -> save());
    }
    private void loadClassSubject() {
        ApiService api = ApiClient.getInstance().getApiService();
        String token = SharedPrefsUtils.getToken(this);
        api.getAllClasses().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        classes.clear();
                        java.util.List<String> names = new java.util.ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i); classes.add(o); names.add(o.optString("name"));
                        }
                        spClass.setAdapter(new ArrayAdapter<>(AdminScheduleActivity.this, android.R.layout.simple_spinner_item, names));
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
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        subjects.clear();
                        java.util.List<String> names = new java.util.ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i); subjects.add(o); names.add(o.optString("name"));
                        }
                        spSubject.setAdapter(new ArrayAdapter<>(AdminScheduleActivity.this, android.R.layout.simple_spinner_item, names));
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void auto() {
        ApiService api = ApiClient.getInstance().getApiService();
        String token = SharedPrefsUtils.getToken(this);
        java.util.Map<String,String> body = new java.util.HashMap<>();
        body.put("class_name", spClass.getSelectedItem()!=null? spClass.getSelectedItem().toString(): "");
        body.put("subject_name", spSubject.getSelectedItem()!=null? spSubject.getSelectedItem().toString(): "");
        api.autoGenerateSchedule("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONObject o = new JSONObject(s);
                        suggestions = o.optJSONArray("suggestions");
                        render(suggestions);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void render(JSONArray arr) {
        table.removeAllViews();
        TableRow header = new TableRow(this);
        addCell(header,"Lớp"); addCell(header,"Môn"); addCell(header,"Thứ"); addCell(header,"Tiết"); addCell(header,"Bắt đầu"); addCell(header,"Kết thúc");
        table.addView(header);
        if (arr==null) return;
        for (int i=0;i<arr.length();i++) {
            JSONObject r = arr.optJSONObject(i);
            TableRow row = new TableRow(this);
            addCell(row, r.optString("class")); addCell(row, r.optString("subject"));
            addCell(row, String.valueOf(r.optInt("day_of_week"))); addCell(row, String.valueOf(r.optInt("period")));
            addCell(row, r.optString("start_time")); addCell(row, r.optString("end_time"));
            table.addView(row);
        }
    }
    private void addCell(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text); tv.setPadding(16,16,16,16);
        row.addView(tv);
    }
    private void save() {
        if (suggestions==null || suggestions.length()==0) return;
        ApiService api = ApiClient.getInstance().getApiService();
        String token = SharedPrefsUtils.getToken(this);
        for (int i=0;i<suggestions.length();i++) {
            JSONObject r = suggestions.optJSONObject(i);
            java.util.Map<String,Object> body = new java.util.HashMap<>();
            body.put("class_name", r.optString("class"));
            body.put("subject_name", r.optString("subject"));
            body.put("weekday", r.optInt("day_of_week"));
            body.put("start_time", r.optString("start_time"));
            body.put("end_time", r.optString("end_time"));
            api.setSchedule("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
            });
        }
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isTaskRoot()) {
                startActivity(new android.content.Intent(this, AdminHomeActivity.class));
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
