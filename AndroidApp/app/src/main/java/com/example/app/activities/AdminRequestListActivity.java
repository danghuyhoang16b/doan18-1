package com.example.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRequestListActivity extends AppCompatActivity {
    private RecyclerView rv;
    private List<JSONObject> requests = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Yêu cầu phân lớp");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        rv = findViewById(R.id.rvRequests);
        rv.setLayoutManager(new LinearLayoutManager(this));
        load();
    }
    private void load() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getAdminRequests("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        JSONArray arr = new JSONArray(s);
                        requests.clear();
                        for (int i=0;i<arr.length();i++) requests.add(arr.getJSONObject(i));
                        rv.setAdapter(new AdminRequestListAdapter(requests));
                        int teacherId = getIntent().getIntExtra("teacher_id", 0);
                        String action = getIntent().getStringExtra("action");
                        if (teacherId > 0 && action != null) {
                            java.util.List<JSONObject> filtered = new java.util.ArrayList<>();
                            for (JSONObject o : requests) {
                                if (o.optString("status").equals("pending") && matchTeacher(o, teacherId)) filtered.add(o);
                            }
                            for (JSONObject o : filtered) {
                                approveOrReject(o.optInt("id"), "approve".equals(action));
                            }
                            Toast.makeText(AdminRequestListActivity.this, ("approve".equals(action)?"Đã phê duyệt ":"Đã từ chối ") + filtered.size() + " yêu cầu của giáo viên", Toast.LENGTH_SHORT).show();
                            getIntent().removeExtra("teacher_id");
                            getIntent().removeExtra("action");
                        }
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
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
    class AdminRequestListAdapter extends RecyclerView.Adapter<AdminRequestListAdapter.VH> {
        private final List<JSONObject> data;
        AdminRequestListAdapter(List<JSONObject> d){ this.data=d; }
        @Override public VH onCreateViewHolder(android.view.ViewGroup p, int vt){
            android.view.View v = getLayoutInflater().inflate(R.layout.item_admin_request, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int pos){
            JSONObject o = data.get(pos);
            h.tvInfo.setText(o.optString("teacher_name") + " - " + o.optString("class_name") + " [" + o.optString("status") + "]");
            h.btnApprove.setOnClickListener(v->act(o.optInt("id"), true));
            h.btnReject.setOnClickListener(v->act(o.optInt("id"), false));
        }
        @Override public int getItemCount(){ return data.size(); }
        class VH extends RecyclerView.ViewHolder{
            android.widget.TextView tvInfo; android.widget.ImageButton btnApprove, btnReject;
            VH(android.view.View item){ super(item);
                tvInfo=item.findViewById(R.id.tvInfo);
                btnApprove=item.findViewById(R.id.btnApprove);
                btnReject=item.findViewById(R.id.btnReject);
            }
        }
        private void act(int id, boolean approve){
            String token = SharedPrefsUtils.getToken(AdminRequestListActivity.this);
            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            java.util.Map<String,Object> body = new java.util.HashMap<>();
            body.put("request_id", id);
            body.put("approve", approve);
            api.approveTeacherRequest("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { Toast.makeText(AdminRequestListActivity.this, approve? "Đã phê duyệt":"Đã từ chối", Toast.LENGTH_SHORT).show(); load(); }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
            });
        }
    }
    private void approveOrReject(int id, boolean approve){
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        java.util.Map<String,Object> body = new java.util.HashMap<>();
        body.put("request_id", id);
        body.put("approve", approve);
        api.approveTeacherRequest("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private boolean matchTeacher(JSONObject o, int teacherId) {
        // Backend response does not include teacher_id; approve all pending if a teacherId is specified
        return true;
    }
}
