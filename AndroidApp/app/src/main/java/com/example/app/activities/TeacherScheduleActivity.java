package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class TeacherScheduleActivity extends AppCompatActivity {
    private Button btnAutoGenerate, btnTeachingSchedule;
    private TableLayout table;
    private EditText etClass, etSubject;
    private ApiService api;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Lịch giảng dạy");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        btnAutoGenerate = findViewById(R.id.btnAutoGenerate);
        btnTeachingSchedule = findViewById(R.id.btnTeachingSchedule);
        table = findViewById(R.id.tableSchedule);
        etClass = findViewById(R.id.etClass);
        etSubject = findViewById(R.id.etSubject);
        api = ApiClient.getInstance().getApiService();
        token = SharedPrefsUtils.getToken(this);
        btnTeachingSchedule.setOnClickListener(v -> loadSchedule());
        btnAutoGenerate.setOnClickListener(v -> generate());
    }
    private void loadSchedule() {
        api.getScheduleByTeacher("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        renderTable(arr);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void generate() {
        java.util.Map<String,String> body = new java.util.HashMap<>();
        body.put("class_name", etClass.getText().toString().trim());
        body.put("subject_name", etSubject.getText().toString().trim());
        api.autoGenerateSchedule("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONObject o = new JSONObject(s);
                        JSONArray arr = o.optJSONArray("suggestions");
                        renderTable(arr);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void renderTable(JSONArray arr) {
        table.removeAllViews();
        TableRow header = new TableRow(this);
        addCell(header, "Lớp"); addCell(header, "Môn"); addCell(header, "Thứ"); addCell(header, "Tiết"); addCell(header, "Bắt đầu"); addCell(header, "Kết thúc");
        table.addView(header);
        if (arr==null) return;
        for (int i=0;i<arr.length();i++) {
            JSONObject r = arr.optJSONObject(i);
            TableRow row = new TableRow(this);
            addCell(row, r.optString("class"));
            addCell(row, r.optString("subject"));
            addCell(row, String.valueOf(r.optInt("day_of_week")));
            addCell(row, String.valueOf(r.optInt("period")));
            addCell(row, r.optString("start_time",""));
            addCell(row, r.optString("end_time",""));
            table.addView(row);
        }
    }
    private void addCell(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16,16,16,16);
        row.addView(tv);
    }
    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
