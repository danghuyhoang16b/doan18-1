package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.ScheduleAdapter;
import com.example.app.network.ApiService;
import com.example.app.models.ScheduleItem;
import com.example.app.models.TokenRequest;
import com.example.app.models.User;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.viewpager2.widget.ViewPager2;
import com.example.app.adapters.ImageSliderAdapter;
import com.example.app.adapters.NewsAdapter;
import com.example.app.models.News;
import com.example.app.network.ApiClient;
import java.util.Arrays;
import java.util.ArrayList;

public class TeacherHomeActivity extends AppCompatActivity {

    private TextView tvFullName, tvRole, tvEmptySchedule, tvError;
    private ImageView imgAvatar;
    private ImageButton btnLogout;
    private RecyclerView rvSchedule, rvNews;
    private ProgressBar progressBar;
    private View btnManageRedStar, btnManageStudentParent, btnStudentRanking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        // Bind Views
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        rvSchedule = findViewById(R.id.rvSchedule);
        rvNews = findViewById(R.id.rvNews);
        tvEmptySchedule = findViewById(R.id.tvEmptySchedule);
        progressBar = findViewById(R.id.progressBar);
        
        btnManageRedStar = findViewById(R.id.btnManageRedStar);
        btnManageStudentParent = findViewById(R.id.btnManageStudentParent);
        btnStudentRanking = findViewById(R.id.btnStudentRanking);

        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvNews.setLayoutManager(new LinearLayoutManager(this));

        // Load User Data
        User user = SharedPrefsUtils.getUser(this);
        if (user != null) {
            tvFullName.setText(user.getFullName());
            tvRole.setText("Giáo viên");
            
            // Load avatar with Glide
            String avatar = user.getAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                String fullUrl = com.example.app.utils.UrlUtils.getFullUrl(this, avatar);
                com.bumptech.glide.Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .into(imgAvatar);
            }
        }

        setupActions();
        loadNews();
        loadSchedule();
    }
    
    private void loadNews() {
        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getLatestNews().enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NewsAdapter adapter = new NewsAdapter(response.body());
                    rvNews.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                // Fail silently
            }
        });
    }

    private void setupActions() {
        btnLogout.setOnClickListener(v -> finish());

        imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, TeacherProfileActivity.class)));

        if (btnManageRedStar != null) {
            btnManageRedStar.setOnClickListener(v -> startActivity(new Intent(this, TeacherRedCommitteeActivity.class)));
        }
        if (btnManageStudentParent != null) {
            btnManageStudentParent.setOnClickListener(v -> startActivity(new Intent(this, TeacherStudentManagerActivity.class)));
        }
        if (btnStudentRanking != null) {
            btnStudentRanking.setOnClickListener(v -> startActivity(new Intent(this, TeacherStudentRankingActivity.class)));
        }
    }

    private void loadSchedule() {
        progressBar.setVisibility(View.VISIBLE);
        String token = SharedPrefsUtils.getToken(this);
        if (token == null) return;

        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getWeeklySchedule("Bearer " + token, null).enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(Call<List<ScheduleItem>> call, Response<List<ScheduleItem>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ScheduleAdapter adapter = new ScheduleAdapter(response.body());
                    rvSchedule.setAdapter(adapter);
                    rvSchedule.setVisibility(View.VISIBLE);
                    tvEmptySchedule.setVisibility(View.GONE);
                } else {
                    rvSchedule.setVisibility(View.GONE);
                    tvEmptySchedule.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleItem>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                rvSchedule.setVisibility(View.GONE);
                tvEmptySchedule.setText("Lỗi tải lịch dạy");
                tvEmptySchedule.setVisibility(View.VISIBLE);
            }
        });
    }
}
