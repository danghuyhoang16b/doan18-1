package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRankingActivity extends AppCompatActivity {
    private EditText etClassId, etLabel, etSemester, etYear;
    private Button btnLoadWeek, btnLoadMonth, btnLoadSemester, btnExportCsv;
    private RecyclerView rvRanking;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ranking);
        etClassId = findViewById(R.id.etClassId);
        etLabel = findViewById(R.id.etLabel);
        etSemester = findViewById(R.id.etSemester);
        etYear = findViewById(R.id.etYear);
        btnLoadWeek = findViewById(R.id.btnLoadWeek);
        btnLoadMonth = findViewById(R.id.btnLoadMonth);
        btnLoadSemester = findViewById(R.id.btnLoadSemester);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        rvRanking = findViewById(R.id.rvRanking);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnLoadWeek.setOnClickListener(v -> loadWeek());
        btnLoadMonth.setOnClickListener(v -> loadMonth());
        btnLoadSemester.setOnClickListener(v -> loadSemester());
        btnExportCsv.setOnClickListener(v -> exportCsv());
    }

    private void loadWeek() {
        String bearer = "Bearer " + SharedPrefsUtils.getToken(this);
        int classId = parseInt(etClassId.getText().toString());
        String label = etLabel.getText().toString().trim();
        if (classId <= 0 || label.isEmpty()) { Toast.makeText(this, "Nhập class_id và label YYYY/WW", Toast.LENGTH_SHORT).show(); return; }
        progressBar.setVisibility(android.view.View.VISIBLE);
        apiService.getWeeklyRanking(bearer, classId, label).enqueue(handler("week"));
    }

    private void loadMonth() {
        String bearer = "Bearer " + SharedPrefsUtils.getToken(this);
        int classId = parseInt(etClassId.getText().toString());
        String label = etLabel.getText().toString().trim();
        if (classId <= 0 || label.isEmpty()) { Toast.makeText(this, "Nhập class_id và label YYYY/MM", Toast.LENGTH_SHORT).show(); return; }
        progressBar.setVisibility(android.view.View.VISIBLE);
        apiService.getMonthlyRanking(bearer, classId, label).enqueue(handler("month"));
    }

    private void loadSemester() {
        String bearer = "Bearer " + SharedPrefsUtils.getToken(this);
        int classId = parseInt(etClassId.getText().toString());
        String sem = etSemester.getText().toString().trim();
        int year = parseInt(etYear.getText().toString());
        if (classId <= 0 || year <= 0 || sem.isEmpty()) { Toast.makeText(this, "Nhập class_id, year, semester HK1/HK2", Toast.LENGTH_SHORT).show(); return; }
        progressBar.setVisibility(android.view.View.VISIBLE);
        apiService.getSemesterRanking(bearer, classId, year, sem).enqueue(handler("semester"));
    }

    private void exportCsv() {
        Toast.makeText(this, "Xuất CSV dùng trình duyệt/webview (tích hợp sau)", Toast.LENGTH_SHORT).show();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private Callback<ResponseBody> handler(String type) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(android.view.View.GONE);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String s = response.body().string();
                        try {
                            org.json.JSONArray arr = new org.json.JSONArray(s);
                            List<RankingItem> items = new ArrayList<>();
                            for (int i=0; i<arr.length(); i++) {
                                org.json.JSONObject o = arr.getJSONObject(i);
                                items.add(new RankingItem(
                                        o.optInt("rank"),
                                        o.optString("student_code"),
                                        o.optString("student_name"),
                                        o.optInt("violations_count"),
                                        o.optInt("points_lost"),
                                        type.equals("week") ? o.optInt("weekly_score") :
                                                type.equals("month") ? o.optInt("monthly_score") :
                                                        o.optInt("semester_score"),
                                        o.optString("grade")
                                ));
                            }
                            if (items.isEmpty()) {
                                tvEmpty.setVisibility(android.view.View.VISIBLE);
                                rvRanking.setAdapter(null);
                            } else {
                                tvEmpty.setVisibility(android.view.View.GONE);
                                rvRanking.setAdapter(new RankingAdapter(items));
                            }
                        } catch (org.json.JSONException e) {
                            tvEmpty.setText("Lỗi dữ liệu: " + e.getMessage());
                            tvEmpty.setVisibility(android.view.View.VISIBLE);
                        }
                    } else {
                        tvEmpty.setText("Lỗi: " + response.code());
                        tvEmpty.setVisibility(android.view.View.VISIBLE);
                    }
                } catch (Exception e) {
                    tvEmpty.setText("Lỗi xử lý: " + e.getMessage());
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                tvEmpty.setText("Lỗi kết nối");
                tvEmpty.setVisibility(android.view.View.VISIBLE);
            }
        };
    }

    static class RankingItem {
        int rank, violations, lost, score;
        String code, name, grade;
        RankingItem(int r, String c, String n, int v, int l, int s, String g) {
            rank=r; code=c; name=n; violations=v; lost=l; score=s; grade=g;
        }
    }
    class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.VH> {
        List<RankingItem> list;
        RankingAdapter(List<RankingItem> l) { list=l; }
        @Override
        public VH onCreateViewHolder(android.view.ViewGroup p, int t) {
            android.view.View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, p, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int i) {
            RankingItem it = list.get(i);
            h.t1.setText("#"+it.rank+" "+it.name+" ("+it.code+")");
            h.t2.setText("Điểm: "+it.score+" | Trừ: "+it.lost+" | Vi phạm: "+it.violations+" | "+it.grade);
        }
        @Override
        public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t1,t2;
            VH(android.view.View v){ super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2); }
        }
    }
}
