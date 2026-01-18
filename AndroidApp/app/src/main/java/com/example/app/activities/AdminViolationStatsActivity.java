package com.example.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.network.ApiClient;
import com.example.app.network.ApiService;
import com.example.app.models.StatItem;
import com.example.app.utils.SharedPrefsUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViolationStatsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvStats;
    private ApiService apiService;
    private List<StatItem> originalList; // Danh sách gốc để lọc
    private StatsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_violation_stats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thống kê vi phạm");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tabLayout = findViewById(R.id.tabLayout);
        rvStats = findViewById(R.id.rvStats);
        rvStats.setLayoutManager(new LinearLayoutManager(this));

        tabLayout.addTab(tabLayout.newTab().setText("Theo Ngày"));
        tabLayout.addTab(tabLayout.newTab().setText("Theo Tuần"));
        tabLayout.addTab(tabLayout.newTab().setText("Theo Tháng"));

        apiService = ApiClient.getInstance().getApiService();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadStats(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadStats(0); // Load Day stats by default
    }

    private void showDetailDialog(StatItem item) {
        String type = "";
        int tabPos = tabLayout.getSelectedTabPosition();
        if (tabPos == 0) type = "day";
        else if (tabPos == 1) type = "week";
        else if (tabPos == 2) type = "month";

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_violation_details, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Chi tiết vi phạm: " + item.label);
        
        RecyclerView rvDetails = view.findViewById(R.id.rvDetails);
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        
        android.widget.ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Build request body with type and label
        Map<String, String> body = new HashMap<>();
        body.put("type", type);
        body.put("label", item.label);

        String token = SharedPrefsUtils.getToken(this);
        apiService.getViolationDetails("Bearer " + token, body).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                progressBar.setVisibility(android.view.View.GONE);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String s = response.body().string();
                        try {
                            org.json.JSONArray arr = new org.json.JSONArray(s);
                            List<ViolationDetail> details = new ArrayList<>();
                            for (int i=0; i<arr.length(); i++) {
                                org.json.JSONObject o = arr.getJSONObject(i);
                                details.add(new ViolationDetail(
                                    o.optString("student_name"),
                                    o.optString("class_name"),
                                    o.optString("rule_name"),
                                    o.optString("note")
                                ));
                            }
                            
                            if (details.isEmpty()) {
                                tvEmpty.setVisibility(android.view.View.VISIBLE);
                            } else {
                                DetailAdapter detailAdapter = new DetailAdapter(details);
                                rvDetails.setAdapter(detailAdapter);
                            }
                        } catch (org.json.JSONException e) {
                            // Try to parse as error object
                            try {
                                org.json.JSONObject err = new org.json.JSONObject(s);
                                String msg = err.optString("message", "Lỗi dữ liệu không xác định");
                                tvEmpty.setText(msg);
                            } catch (Exception ex) {
                                tvEmpty.setText("Lỗi định dạng: " + s); // Show raw response for debug if needed
                            }
                            tvEmpty.setVisibility(android.view.View.VISIBLE);
                        }
                    } else {
                         tvEmpty.setText("Lỗi tải dữ liệu: " + response.code());
                         tvEmpty.setVisibility(android.view.View.VISIBLE);
                    }
                } catch (Exception e) {
                    tvEmpty.setText("Lỗi xử lý: " + e.getMessage());
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                tvEmpty.setText("Lỗi kết nối");
                tvEmpty.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    static class ViolationDetail {
        String studentName, className, ruleName, note;
        ViolationDetail(String s, String c, String r, String n) {
            this.studentName = s; this.className = c; this.ruleName = r; this.note = n;
        }
    }

    class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.VH> {
        List<ViolationDetail> list;
        DetailAdapter(List<ViolationDetail> l) { this.list = l; }
        @Override
        public VH onCreateViewHolder(android.view.ViewGroup p, int t) {
            android.view.View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, p, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int i) {
            ViolationDetail d = list.get(i);
            h.t1.setText(d.studentName + " - " + d.className);
            h.t2.setText(d.ruleName + (d.note.isEmpty() ? "" : " (" + d.note + ")"));
        }
        @Override
        public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(android.view.View v) { super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2); }
        }
    }

    private void loadStats(int position) {
        String type = "day";
        if (position == 1) type = "week";
        else if (position == 2) type = "month";

        String token = SharedPrefsUtils.getToken(this);
        apiService.getViolationStats("Bearer " + token, type).enqueue(new Callback<List<StatItem>>() {
            @Override
            public void onResponse(Call<List<StatItem>> call, Response<List<StatItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalList = response.body();
                    if (originalList.isEmpty()) {
                        Toast.makeText(AdminViolationStatsActivity.this, "Chưa có dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                    }
                    adapter = new StatsAdapter(originalList);
                    rvStats.setAdapter(adapter);
                } else {
                    originalList = new ArrayList<>(); // Tránh null pointer
                    adapter = new StatsAdapter(originalList);
                    rvStats.setAdapter(adapter);
                    Toast.makeText(AdminViolationStatsActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<StatItem>> call, Throwable t) {
                Toast.makeText(AdminViolationStatsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        
        return true;
    }

    private void filter(String text) {
        if (originalList == null) return;
        
        List<StatItem> filteredList = new ArrayList<>();
        for (StatItem item : originalList) {
            if (item.label != null && item.label.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
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

    class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.VH> {
        private List<StatItem> data;
        
        StatsAdapter(List<StatItem> data) { this.data = data; }

        public void updateList(List<StatItem> newList) {
            this.data = newList;
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            StatItem item = data.get(position);
            holder.text1.setText(item.label);
            holder.text2.setText("Số lượng vi phạm: " + item.count);
            holder.itemView.setOnClickListener(v -> showDetailDialog(item));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(android.view.View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}