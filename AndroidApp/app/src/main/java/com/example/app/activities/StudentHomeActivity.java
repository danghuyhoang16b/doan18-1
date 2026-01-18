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
import androidx.viewpager2.widget.ViewPager2;

import com.example.app.R;
import com.example.app.adapters.NewsAdapter;
import com.example.app.adapters.ScheduleAdapter;
import com.example.app.api.ApiService;
import com.example.app.models.News;
import com.example.app.models.ScheduleItem;
import com.example.app.models.TokenRequest;
import com.example.app.models.User;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.app.adapters.ImageSliderAdapter;
import java.util.Arrays;
import java.util.ArrayList;

public class StudentHomeActivity extends AppCompatActivity {

    private TextView tvFullName, tvRole, tvEmptyNews, tvEmptySchedule, tvError;
    private ImageView imgAvatar;
    private ImageButton btnLogout;
    private RecyclerView rvNews, rvSchedule;
    private ProgressBar progressBar;
    private View cardRedStar;
    private android.widget.Button btnViolation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        // Bind Views
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        rvNews = findViewById(R.id.rvNews);
        rvSchedule = findViewById(R.id.rvSchedule);
        tvEmptyNews = findViewById(R.id.tvEmptyNews);
        tvEmptySchedule = findViewById(R.id.tvEmptySchedule);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        
        cardRedStar = findViewById(R.id.cardRedStar);
        btnViolation = findViewById(R.id.btnViolation);

        // Setup RecyclerViews
        rvNews.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));

        // Load User Data
        User user = SharedPrefsUtils.getUser(this);
        if (user != null) {
            tvFullName.setText(user.getFullName());
            tvRole.setText("Học sinh");
            
            if (user.isRedStar()) {
                cardRedStar.setVisibility(View.VISIBLE);
                btnViolation.setOnClickListener(v -> startActivity(new Intent(this, ViolationActivity.class)));
            } else {
                cardRedStar.setVisibility(View.GONE);
            }
        }

        btnLogout.setOnClickListener(v -> finish());

        imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, StudentProfileActivity.class)));

        // Load Data
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        
        loadNews();
        loadSchedule();
        
        // Simulating data loading completion for progress bar
        // In real app, you might want to wait for all calls or show progress per section
        new android.os.Handler().postDelayed(() -> progressBar.setVisibility(View.GONE), 1000);
    }

    private void loadNews() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getLatestNews().enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    NewsAdapter adapter = new NewsAdapter(response.body());
                    rvNews.setAdapter(adapter);
                    rvNews.setVisibility(View.VISIBLE);
                    tvEmptyNews.setVisibility(View.GONE);
                } else {
                    rvNews.setVisibility(View.GONE);
                    tvEmptyNews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                rvNews.setVisibility(View.GONE);
                tvEmptyNews.setText("Lỗi tải tin tức");
                tvEmptyNews.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadSchedule() {
        String token = SharedPrefsUtils.getToken(this);
        if (token == null) return;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getWeeklySchedule("Bearer " + token, null).enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(Call<List<ScheduleItem>> call, Response<List<ScheduleItem>> response) {
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
                rvSchedule.setVisibility(View.GONE);
                tvEmptySchedule.setText("Lỗi tải lịch học");
                tvEmptySchedule.setVisibility(View.VISIBLE);
            }
        });
    }
}