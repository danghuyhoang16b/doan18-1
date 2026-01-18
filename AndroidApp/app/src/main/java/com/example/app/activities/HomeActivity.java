package com.example.app.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.app.R;
import com.example.app.adapters.NewsAdapter;
import com.example.app.adapters.ScheduleAdapter;
import com.example.app.adapters.UrlImageSliderAdapter;
import com.example.app.network.ApiService;
import com.example.app.models.News;
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

public class HomeActivity extends AppCompatActivity {
    // --- 1. KHAI BÁO BIẾN ---
    private Toolbar toolbar;
    private TextView tvFullName, tvRole;
    private ImageView imgAvatar;
    private RecyclerView rvNews, rvSchedule;
    private ViewPager2 viewPagerSlider;
    private com.google.android.material.tabs.TabLayout tabDots;
    private android.os.Handler sliderHandler;

    // Các nút xem thêm
    private Button btnMoreNews, btnMoreSchedule, btnNotifications;

    // Các nút điều hướng dưới cùng (Bottom Nav)
    private android.widget.ImageButton btnNavHome, btnNavSearch, btnNavNotify, btnNavHelp, btnNavLogout;

    // Các nút chức năng
    private Button btnNavAttendance, btnNavMessages, btnNavViolation;
    private Button btnNavRedCommittee, btnNavScore, btnTeacherSchedule;

    // Slider controls
    private Button btnPrev, btnNext;

    private androidx.activity.result.ActivityResultLauncher<String> avatarPicker;
    private android.net.Uri selectedAvatar;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(android.content.Context context, Intent intent) {
            refreshProfileFromServer();
        }
    };

    // Biến cho Slider logic
    private java.util.List<com.example.app.models.Banner> currentBanners;
    private android.os.Handler pollingHandler = new android.os.Handler();
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            setupSlider();
            pollingHandler.postDelayed(this, 30000); // Poll every 30s
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trang chủ");
        }

        // --- 2. ÁNH XẠ VIEW (Bind Views) ---
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        imgAvatar = findViewById(R.id.imgAvatar);

        rvNews = findViewById(R.id.rvNews);
        rvSchedule = findViewById(R.id.rvSchedule);

        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        tabDots = findViewById(R.id.tabDots);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        btnMoreNews = findViewById(R.id.btnMoreNews);
        btnMoreSchedule = findViewById(R.id.btnMoreSchedule);
        btnNotifications = findViewById(R.id.btnNotifications);

        btnNavHome = findViewById(R.id.btnNavHome);
        btnNavSearch = findViewById(R.id.btnNavSearch);
        btnNavNotify = findViewById(R.id.btnNavNotify);
        btnNavHelp = findViewById(R.id.btnNavHelp);
        btnNavLogout = findViewById(R.id.btnNavLogout);

        btnNavAttendance = findViewById(R.id.btnNavAttendance);
        btnNavMessages = findViewById(R.id.btnNavMessages);
        btnNavViolation = findViewById(R.id.btnNavViolation);

        btnNavRedCommittee = findViewById(R.id.btnNavRedCommittee);
        btnNavScore = findViewById(R.id.btnNavScore);
        btnTeacherSchedule = findViewById(R.id.btnTeacherSchedule);

        // Setup RecyclerViews
        rvNews.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));

        // --- FIX LỖI NHẢY LAYOUT: Tắt cuộn lồng nhau ---
        rvNews.setNestedScrollingEnabled(false);
        rvSchedule.setNestedScrollingEnabled(false);

        rvNews.setAdapter(new NewsAdapter(java.util.Collections.emptyList()));
        rvSchedule.setAdapter(new ScheduleAdapter(java.util.Collections.emptyList()));

        // Load User Data
        User user = SharedPrefsUtils.getUser(this);
        if (user != null) {
            tvFullName.setText(user.getFullName());
            tvRole.setText(getRoleName(user.getRole()));

            // Logic hiển thị ban đầu
            if ("teacher".equals(user.getRole()) || "admin".equals(user.getRole())) {
                if (btnNavAttendance != null) btnNavAttendance.setVisibility(android.view.View.VISIBLE);
                if (btnNavViolation != null) btnNavViolation.setVisibility(android.view.View.VISIBLE);
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Load Data from API
        loadNews();
        loadSchedule();
        setupSlider();
        fetchBackgroundsAndApply();

        // --- FIX LỖI SLIDER NHẢY: Dùng chung hàm applyBanners ---
        com.example.app.utils.BannerManager.subscribeRealtime(this, banners -> {
            runOnUiThread(() -> {
                if (banners == null || banners.isEmpty()) return;
                applyBanners(banners); // Sử dụng logic chung đã được tối ưu
            });
        });

        // Avatar Picker Logic
        avatarPicker = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedAvatar = uri;
                uploadAvatarSelected();
            }
        });

        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> {
                com.example.app.models.User u = SharedPrefsUtils.getUser(this);
                if (u == null) return;
                android.content.Intent intent;
                switch (u.getRole()) {
                    case "student": intent = new android.content.Intent(this, StudentProfileActivity.class); break;
                    case "teacher": intent = new android.content.Intent(this, TeacherProfileActivity.class); break;
                    case "admin": intent = new android.content.Intent(this, AdminProfileActivity.class); break;
                    default: intent = new android.content.Intent(this, ProfileActivity.class);
                }
                startActivity(intent);
            });
            imgAvatar.setOnLongClickListener(v -> {
                avatarPicker.launch("image/*");
                return true;
            });
        }

        // Setup Navigation & Listeners
        setupNavigation();
        registerReceiver(receiver, new IntentFilter("com.example.app.PROFILE_UPDATED"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBackgroundsAndApply();
        loadNews();
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.post(pollingRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(receiver); } catch (Exception ignored) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pollingHandler != null) {
            pollingHandler.removeCallbacksAndMessages(null);
        }
    }

    private String getRoleName(String role) {
        if (role == null) return "";
        switch (role) {
            case "teacher": return "Giáo viên";
            case "student": return "Học sinh";
            case "parent": return "Phụ huynh";
            case "admin": return "Quản trị viên";
            case "red_star": return "Sao đỏ";
            default: return role;
        }
    }

    private void loadNews() {
        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getLatestNews().enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                if (response.isSuccessful() && response.body() != null && rvNews != null) {
                    NewsAdapter adapter = new NewsAdapter(response.body());
                    rvNews.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                // Toast.makeText(HomeActivity.this, "Lỗi tải tin tức", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshProfileFromServer() {
        String token = SharedPrefsUtils.getToken(this);
        if (token == null) return;
        ApiService api = ApiClient.getInstance().getApiService();
        api.getProfile("Bearer " + token).enqueue(new Callback<com.example.app.models.ProfileResponse>() {
            @Override
            public void onResponse(Call<com.example.app.models.ProfileResponse> call, Response<com.example.app.models.ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (tvFullName != null) tvFullName.setText(response.body().full_name);
                    if (tvRole != null) tvRole.setText(getRoleName(response.body().role));
                    if (response.body().avatar != null && response.body().avatar.length() > 0 && imgAvatar != null) {
                        String a = response.body().avatar;
                        String url = a.startsWith("http") ? a : com.example.app.network.ApiConstants.AVATAR_BASE_URL + a;
                        // Add timestamp to bypass cache
                        url = url + "?t=" + System.currentTimeMillis();
                        android.util.Log.d("HomeActivity", "Loading avatar: " + url);
                        com.bumptech.glide.Glide.with(HomeActivity.this)
                            .load(url)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.mipmap.ic_launcher_round)
                            .into(imgAvatar);
                    }
                }
            }
            @Override
            public void onFailure(Call<com.example.app.models.ProfileResponse> call, Throwable t) { }
        });
    }

    private void loadImage(ImageView target, String url) {
        new Thread(() -> {
            android.graphics.Bitmap bmp = null;
            java.net.HttpURLConnection conn = null;
            try {
                java.net.URL u = new java.net.URL(url);
                conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();
                java.io.InputStream is = conn.getInputStream();
                bmp = android.graphics.BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception ignored) { } finally {
                if (conn != null) conn.disconnect();
            }
            android.graphics.Bitmap finalBmp = bmp;
            runOnUiThread(() -> {
                if (finalBmp != null && target != null) target.setImageBitmap(finalBmp);
            });
        }).start();
    }

    private void uploadAvatarSelected() {
        try {
            String token = SharedPrefsUtils.getToken(this);
            if (token == null || selectedAvatar == null) return;
            ApiService api = ApiClient.getInstance().getApiService();
            String mime = getContentResolver().getType(selectedAvatar);
            if (mime == null) mime = "image/*";
            java.io.InputStream is = getContentResolver().openInputStream(selectedAvatar);
            byte[] bytes = readAll(is);
            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse(mime), bytes);
            okhttp3.MultipartBody.Part imagePart = okhttp3.MultipartBody.Part.createFormData("image", "avatar.jpg", fileBody);
            api.uploadAvatar("Bearer " + token, imagePart).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                    refreshProfileFromServer();
                }
                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) { }
            });
        } catch (Exception ignored) { }
    }

    private byte[] readAll(java.io.InputStream is) throws java.io.IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private void loadSchedule() {
        String token = SharedPrefsUtils.getToken(this);
        if (token == null) return;

        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getWeeklySchedule("Bearer " + token, null).enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(Call<List<ScheduleItem>> call, Response<List<ScheduleItem>> response) {
                if (response.isSuccessful() && response.body() != null && rvSchedule != null) {
                    ScheduleAdapter adapter = new ScheduleAdapter(response.body());
                    rvSchedule.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleItem>> call, Throwable t) {
                // Toast.makeText(HomeActivity.this, "Lỗi tải lịch học", Toast.LENGTH_SHORT).show();
                android.util.Log.e("HomeActivity", "Lỗi tải lịch học: " + t.getMessage());
            }
        });
    }

    private void setupNavigation() {
        if (btnNavHome != null) btnNavHome.setOnClickListener(v -> { /* reserved for future */ });

        if (btnNavSearch != null) btnNavSearch.setOnClickListener(v -> {
            User u = SharedPrefsUtils.getUser(this);
            if (u != null) {
                switch (u.getRole()) {
                    case "admin":
                        startActivity(new Intent(this, AdminHomeActivity.class));
                        break;
                    case "teacher":
                        startActivity(new Intent(this, TeacherHomeActivity.class));
                        break;
                    case "student":
                        startActivity(new Intent(this, StudentHomeActivity.class));
                        break;
                    case "parent":
                        startActivity(new Intent(this, ParentHomeActivity.class));
                        break;
                    default:
                        android.widget.Toast.makeText(this, "Không tìm thấy trang chức năng", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (btnNavNotify != null) btnNavNotify.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        if (btnNavHelp != null) btnNavHelp.setOnClickListener(v -> Toast.makeText(this,"Hỗ trợ sẽ sớm có",Toast.LENGTH_SHORT).show());
        if (btnNavLogout != null) btnNavLogout.setOnClickListener(v -> {
            SharedPrefsUtils.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        if (btnNavAttendance != null) btnNavAttendance.setOnClickListener(v -> startActivity(new Intent(this, AttendanceActivity.class)));
        if (btnNavViolation != null) btnNavViolation.setOnClickListener(v -> startActivity(new Intent(this, ViolationActivity.class)));

        if (btnNavMessages != null) btnNavMessages.setOnClickListener(v -> startActivity(new Intent(this, ContactListActivity.class)));
        if (btnNotifications != null) btnNotifications.setOnClickListener(v -> {
            User u = SharedPrefsUtils.getUser(this);
            if (u != null && ("admin".equals(u.getRole()) || "teacher".equals(u.getRole()))) {
                startActivity(new Intent(this, CreateNotificationActivity.class));
            } else {
                startActivity(new Intent(this, NotificationActivity.class));
            }
        });

        if (btnMoreNews != null) btnMoreNews.setOnClickListener(v -> startActivity(new Intent(this, NewsActivity.class)));
        if (btnMoreSchedule != null) btnMoreSchedule.setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));

        // Setup listener cho các nút mới thêm
        if (btnNavScore != null) btnNavScore.setOnClickListener(v -> startActivity(new Intent(this, ScoreEntryActivity.class)));
        if (btnNavRedCommittee != null) btnNavRedCommittee.setOnClickListener(v -> {
            User u = SharedPrefsUtils.getUser(this);
            if (u != null) {
                if ("teacher".equals(u.getRole())) {
                    startActivity(new Intent(this, TeacherRedCommitteeActivity.class));
                } else if ("admin".equals(u.getRole())) {
                    startActivity(new Intent(this, AdminRedCommitteeActivity.class));
                } else if ("student".equals(u.getRole()) && u.isRedStar()) {
                    // Logic cho học sinh Sao đỏ: Vào trang chấm nề nếp
                    // Tạm thời dẫn vào ViolationActivity (hoặc màn hình chấm điểm riêng nếu có)
                     startActivity(new Intent(this, ViolationActivity.class)); 
                }
            }
        });
        if (btnTeacherSchedule != null) btnTeacherSchedule.setOnClickListener(v -> startActivity(new Intent(this, TeacherScheduleActivity.class)));

        // Visibility Logic for Role-based Buttons
        User roleUser = SharedPrefsUtils.getUser(this);
        if (roleUser != null) {
            String role = roleUser.getRole();
            boolean isRedStar = roleUser.isRedStar();

            // Default: Hide all privileged buttons first
            if (btnTeacherSchedule != null) btnTeacherSchedule.setVisibility(android.view.View.GONE);
            if (btnNavScore != null) btnNavScore.setVisibility(android.view.View.GONE);
            if (btnNavRedCommittee != null) btnNavRedCommittee.setVisibility(android.view.View.GONE);
            if (btnNavAttendance != null) btnNavAttendance.setVisibility(android.view.View.GONE);
            if (btnNavViolation != null) btnNavViolation.setVisibility(android.view.View.GONE);

            // Apply Visibility based on Role
            if ("teacher".equals(role)) {
                if (btnTeacherSchedule != null) btnTeacherSchedule.setVisibility(android.view.View.VISIBLE);
                if (btnNavScore != null) btnNavScore.setVisibility(android.view.View.VISIBLE);
                if (btnNavRedCommittee != null) btnNavRedCommittee.setVisibility(android.view.View.VISIBLE);
                if (btnNavAttendance != null) btnNavAttendance.setVisibility(android.view.View.VISIBLE);
                if (btnNavViolation != null) btnNavViolation.setVisibility(android.view.View.VISIBLE);
            } else if ("admin".equals(role)) {
                if (btnNavRedCommittee != null) btnNavRedCommittee.setVisibility(android.view.View.VISIBLE);
                if (btnNavAttendance != null) btnNavAttendance.setVisibility(android.view.View.VISIBLE);
                if (btnNavViolation != null) btnNavViolation.setVisibility(android.view.View.VISIBLE);
            } else if (("student".equals(role) && isRedStar) || "red_star".equals(role)) {
                // Red Star Student Privileges
                if (btnNavViolation != null) btnNavViolation.setVisibility(android.view.View.VISIBLE);
                // Optional: If you want Red Star to see Red Committee management
                // if (btnNavRedCommittee != null) btnNavRedCommittee.setVisibility(android.view.View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        User u = SharedPrefsUtils.getUser(this);
        if (u != null && "admin".equals(u.getRole())) {
            getMenuInflater().inflate(R.menu.menu_home_admin, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_admin) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_requests) {
            startActivity(new Intent(this, AdminRequestListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSlider() {
        if (viewPagerSlider == null || tabDots == null) return;

        // 1. Check for Local Overrides first (Fast Display)
        java.util.List<com.example.app.models.Banner> localBanners = new java.util.ArrayList<>();
        String sub1 = SharedPrefsUtils.getString(this, "local_banner_sub1", null);
        String sub2 = SharedPrefsUtils.getString(this, "local_banner_sub2", null);
        String sub3 = SharedPrefsUtils.getString(this, "local_banner_sub3", null);

        if (sub1 != null) localBanners.add(new com.example.app.models.Banner(101, sub1, "Banner 1", "Xem", "", 1, 10));
        if (sub2 != null) localBanners.add(new com.example.app.models.Banner(102, sub2, "Banner 2", "Xem", "", 1, 9));
        if (sub3 != null) localBanners.add(new com.example.app.models.Banner(103, sub3, "Banner 3", "Xem", "", 1, 8));

        if (!localBanners.isEmpty()) {
            applyBanners(localBanners);
            // Keep loading from API in background to update cache, but don't block
        }

        com.example.app.utils.BannerManager.loadBanners(this, banners -> {
            if (banners == null || banners.isEmpty()) {
                return;
            }
            applyBanners(banners);
        });
    }

    private void applyBanners(java.util.List<com.example.app.models.Banner> banners) {
        // Check if banners changed
        if (currentBanners != null && currentBanners.size() == banners.size()) {
            boolean changed = false;
            for (int i = 0; i < banners.size(); i++) {
                if (banners.get(i).getId() != currentBanners.get(i).getId() ||
                        !banners.get(i).getImageUrl().equals(currentBanners.get(i).getImageUrl())) {
                    changed = true;
                    break;
                }
            }
            if (!changed) return;
        }
        currentBanners = banners;

        java.util.List<String> urls = new java.util.ArrayList<>();
        java.util.List<String> titles = new java.util.ArrayList<>();
        java.util.List<String> ctas = new java.util.ArrayList<>();
        java.util.List<String> links = new java.util.ArrayList<>();

        for (com.example.app.models.Banner b : banners) {
            String url = b.getImageUrl();
            if (!url.startsWith("content://") && !url.startsWith("file://")) {
                url = com.example.app.utils.UrlUtils.getFullUrl(this, b.getImageUrl());
            }
            urls.add(url);
            titles.add(b.getTitle());
            ctas.add(b.getCtaText());
            links.add(b.getLinkUrl());
        }

        UrlImageSliderAdapter adapter = new UrlImageSliderAdapter(urls, titles, ctas, (view, position) -> {
            if (links != null && links.size() > position) {
                String link = links.get(position);
                if (link != null && !link.isEmpty()) {
                    try {
                        if (!link.startsWith("http")) link = "http://" + link;
                        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (viewPagerSlider != null) {
            viewPagerSlider.setAdapter(adapter);
            viewPagerSlider.setOffscreenPageLimit(1);

            if (tabDots != null) {
                try {
                    new com.google.android.material.tabs.TabLayoutMediator(tabDots, viewPagerSlider, (tab, position) -> {}).attach();
                } catch(Exception e) { /* Ignore if already attached */ }
            }
            viewPagerSlider.setPageTransformer(null);

            if (sliderHandler != null) sliderHandler.removeCallbacksAndMessages(null);
            sliderHandler = new android.os.Handler();
            Runnable autoSlide = new Runnable() {
                @Override
                public void run() {
                    if (adapter.getItemCount() > 0) {
                        int next = (viewPagerSlider.getCurrentItem() + 1) % adapter.getItemCount();
                        viewPagerSlider.setCurrentItem(next, true);
                        sliderHandler.postDelayed(this, 4000);
                    }
                }
            };
            sliderHandler.postDelayed(autoSlide, 4000);
            viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    sliderHandler.removeCallbacks(autoSlide);
                    sliderHandler.postDelayed(autoSlide, 4000);
                }
            });

            if (btnPrev != null) {
                btnPrev.setOnClickListener(v -> {
                    int prev = viewPagerSlider.getCurrentItem() - 1;
                    if (prev < 0) prev = adapter.getItemCount() - 1;
                    viewPagerSlider.setCurrentItem(prev, true);
                });
            }
            if (btnNext != null) {
                btnNext.setOnClickListener(v -> {
                    int next = (viewPagerSlider.getCurrentItem() + 1) % adapter.getItemCount();
                    viewPagerSlider.setCurrentItem(next, true);
                });
            }
            viewPagerSlider.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        sliderHandler.removeCallbacks(autoSlide);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        sliderHandler.postDelayed(autoSlide, 4000);
                        break;
                }
                return false;
            });
        }
    }

    private void fetchBackgroundsAndApply() {
        // Disabled to prevent conflicts with setupSlider logic
    }
}