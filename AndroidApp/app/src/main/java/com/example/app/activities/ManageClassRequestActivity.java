package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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

public class ManageClassRequestActivity extends AppCompatActivity {
    private LinearLayout listContainer;
    private LinearLayout listRequests;
    private Button btnSend;
    private List<JSONObject> classes = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_class_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Yêu cầu quản lý lớp");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        listContainer = findViewById(R.id.listContainer);
        listRequests = findViewById(R.id.listRequests);
        btnSend = findViewById(R.id.btnSend);
        loadClasses();
        loadRequests();
        btnSend.setOnClickListener(v -> sendRequest());
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_class_request, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_confirm) {
            sendRequest();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadClasses() {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getAllClasses().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        classes.clear();
                        listContainer.removeAllViews();
                        for (int i=0;i<arr.length();i++) {
                            JSONObject o = arr.getJSONObject(i);
                            classes.add(o);
                            CheckBox cb = new CheckBox(ManageClassRequestActivity.this);
                            cb.setText(o.optString("name"));
                            cb.setTag("cls_"+o.optInt("id"));
                            listContainer.addView(cb);
                        }
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void sendRequest() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        List<Integer> ids = new ArrayList<>();
        for (JSONObject o : classes) {
            int id = o.optInt("id");
            CheckBox cb = listContainer.findViewWithTag("cls_"+id);
            if (cb!=null && cb.isChecked()) ids.add(id);
        }
        if (ids.isEmpty()) {
            android.widget.Toast.makeText(this, "Vui lòng chọn lớp", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Integer id: ids) {
            for (JSONObject o: classes) if (o.optInt("id")==id) { sb.append(o.optString("name")).append(", "); break; }
        }
        String summary = sb.length()>2 ? sb.substring(0, sb.length()-2) : "";
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận gửi yêu cầu")
                .setMessage("Bạn sẽ gửi yêu cầu quản lý các lớp: " + summary)
                .setPositiveButton("Gửi", (d,w) -> {
        Map<String,Object> body = new HashMap<>();
        body.put("class_ids", ids);
        api.requestTeacherClasses("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { finish(); }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void loadRequests() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getTeacherRequests("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        listRequests.removeAllViews();
                        for (int i=0;i<arr.length();i++) {
                            JSONObject o = arr.getJSONObject(i);
                            android.widget.TextView tv = new android.widget.TextView(ManageClassRequestActivity.this);
                            tv.setText("- " + o.optString("class_name") + " [" + o.optString("status") + "] " + o.optString("requested_at"));
                            listRequests.addView(tv);
                        }
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
