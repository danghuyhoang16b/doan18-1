package com.example.app.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.NewsAdapter;
import com.example.app.network.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.models.News;
import com.example.app.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView rvAllNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvAllNews = findViewById(R.id.rvAllNews);
        rvAllNews.setLayoutManager(new LinearLayoutManager(this));
        rvAllNews.setAdapter(new NewsAdapter(java.util.Collections.emptyList()));

        loadAllNews();
    }

    private void loadAllNews() {
        String customBaseUrl = SharedPrefsUtils.getBaseUrl(this);
        if (customBaseUrl != null && !customBaseUrl.isEmpty()) {
            RetrofitClient.overrideForTesting(customBaseUrl);
        }
        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getAllNews().enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NewsAdapter adapter = new NewsAdapter(response.body());
                    rvAllNews.setAdapter(adapter);
                } else {
                    String msg = "Không thể tải tin tức (" + response.code() + ")";
                    Toast.makeText(NewsActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                Toast.makeText(NewsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
