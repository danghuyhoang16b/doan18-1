package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.User;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

import androidx.viewpager2.widget.ViewPager2;
import com.example.app.adapters.ImageSliderAdapter;
import java.util.Arrays;
import java.util.List;
import android.widget.Toast;

public class ParentHomeActivity extends AppCompatActivity {

    private TextView tvFullName, tvRole, tvEmptyChildren;
    private ImageView imgAvatar;
    private ImageButton btnLogout;
    private RecyclerView rvChildren;
    private View btnChildrenInfo;
    private View loadingBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        // Bind Views
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        rvChildren = findViewById(R.id.rvChildren);
        tvEmptyChildren = findViewById(R.id.tvEmptyChildren);
        btnChildrenInfo = findViewById(R.id.btnChildrenInfo);
        loadingBar = findViewById(R.id.progressBar);

        rvChildren.setLayoutManager(new LinearLayoutManager(this));

        // Load User Data
        User user = SharedPrefsUtils.getUser(this);
        if (user != null) {
            tvFullName.setText(user.getFullName());
            tvRole.setText("Phụ huynh");
        }

        btnLogout.setOnClickListener(v -> finish());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnChildrenInfo.setOnClickListener(v -> fetchChildrenInfo());
    }

    private void fetchChildrenInfo() {
        String bearer = "Bearer " + SharedPrefsUtils.getToken(this);
        loadingBar.setVisibility(View.VISIBLE);
        apiService.listChildren(bearer).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingBar.setVisibility(View.GONE);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        java.util.List<ChildItem> items = new java.util.ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);
                            items.add(new ChildItem(
                                    o.optString("name"),
                                    o.optString("birth_date"),
                                    o.optString("class_name")
                            ));
                        }
                        if (items.isEmpty()) {
                            tvEmptyChildren.setVisibility(View.VISIBLE);
                            rvChildren.setVisibility(View.GONE);
                        } else {
                            tvEmptyChildren.setVisibility(View.GONE);
                            rvChildren.setVisibility(View.VISIBLE);
                            rvChildren.setAdapter(new ChildrenAdapter(items));
                        }
                    } else {
                        tvEmptyChildren.setText("Không nhận được dữ liệu");
                        tvEmptyChildren.setVisibility(View.VISIBLE);
                        rvChildren.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    tvEmptyChildren.setText("Lỗi xử lý dữ liệu");
                    tvEmptyChildren.setVisibility(View.VISIBLE);
                    rvChildren.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingBar.setVisibility(View.GONE);
                android.widget.Toast.makeText(ParentHomeActivity.this, "Mất kết nối hệ thống, vui lòng kiểm tra lại mạng", android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    static class ChildItem {
        String name, dob, clazz;
        ChildItem(String n, String d, String c){ name=n; dob=d; clazz=c; }
    }
    class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.VH> {
        java.util.List<ChildItem> list;
        ChildrenAdapter(java.util.List<ChildItem> l){ list=l; }
        @Override public VH onCreateViewHolder(android.view.ViewGroup p, int t){
            android.view.View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h,int i){
            ChildItem c=list.get(i);
            h.t1.setText(c.name + " - " + (c.clazz.isEmpty()?"Chưa xếp lớp":c.clazz));
            h.t2.setText("Ngày sinh: " + (c.dob.isEmpty()?"Chưa cập nhật":c.dob));
        }
        @Override public int getItemCount(){ return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t1,t2;
            VH(android.view.View v){ super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2); }
        }
    }
}
