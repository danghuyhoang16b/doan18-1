package com.example.app.activities;

import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.models.AdminBackgroundRequest;
import com.example.app.models.AdminBackgroundResponse;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {
    private Spinner spMobileBg, spPcBg;
    private Button btnSaveBg, btnPickMobile, btnPickPc;
    private android.net.Uri pickedMobileUri, pickedPcUri;
    private ActivityResultLauncher<String> launcherGetContent;
    private ActivityResultLauncher<String[]> launcherOpenDocument;
    private ActivityResultLauncher<String> launcherSubPick;
    private android.widget.ImageView ivPreviewMobile, ivPreviewPc;
    private android.widget.ImageView ivSub1, ivSub2, ivSub3;
    private android.net.Uri uriSub1, uriSub2, uriSub3;
    private Button btnPickSub1, btnPickSub2, btnPickSub3, btnSaveSub1, btnSaveSub2, btnSaveSub3;
    private Button btnImportImages;
    private Button btnManageBanners;
    private int currentSub = 0;
    private boolean panelExpanded = false;
    private android.widget.TextView tvSystemHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản trị hệ thống");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spMobileBg = findViewById(R.id.spMobileBg);
        spPcBg = findViewById(R.id.spPcBg);
        btnSaveBg = findViewById(R.id.btnSaveBg);
        btnPickMobile = findViewById(R.id.btnPickMobile);
        btnPickPc = findViewById(R.id.btnPickPc);
        ivPreviewMobile = findViewById(R.id.ivPreviewMobile);
        ivPreviewPc = findViewById(R.id.ivPreviewPc);
        ivSub1 = findViewById(R.id.ivSub1);
        ivSub2 = findViewById(R.id.ivSub2);
        ivSub3 = findViewById(R.id.ivSub3);
        btnPickSub1 = findViewById(R.id.btnPickSub1);
        btnPickSub2 = findViewById(R.id.btnPickSub2);
        btnPickSub3 = findViewById(R.id.btnPickSub3);
        btnSaveSub1 = findViewById(R.id.btnSaveSub1);
        btnSaveSub2 = findViewById(R.id.btnSaveSub2);
        btnSaveSub3 = findViewById(R.id.btnSaveSub3);
        btnImportImages = findViewById(R.id.btnImportImages);
        tvSystemHeader = findViewById(R.id.tvSystemHeader);

        if (btnImportImages != null) {
            btnImportImages.setOnClickListener(v -> startActivity(new android.content.Intent(this, ImageImportActivity.class)));
        }

        String[] options = new String[]{"banner_1", "banner_2", "banner_3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMobileBg.setAdapter(adapter);
        spPcBg.setAdapter(adapter);

        loadCurrentBackground();

        launcherGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                pickedMobileUri = uri;
                try { ivPreviewMobile.setImageURI(uri); } catch (Exception ignored) {}
                Toast.makeText(this, "Đã chọn ảnh Mobile", Toast.LENGTH_SHORT).show();
            }
        });
        launcherOpenDocument = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                pickedPcUri = uri;
                try { getContentResolver().takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
                try { ivPreviewPc.setImageURI(uri); } catch (Exception ignored) {}
                Toast.makeText(this, "Đã chọn ảnh PC", Toast.LENGTH_SHORT).show();
            }
        });
        launcherSubPick = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                if (currentSub == 1) { uriSub1 = uri; ivSub1.setImageURI(uri); }
                else if (currentSub == 2) { uriSub2 = uri; ivSub2.setImageURI(uri); }
                else if (currentSub == 3) { uriSub3 = uri; ivSub3.setImageURI(uri); }
            }
        });

        btnPickMobile.setOnClickListener(v -> launcherGetContent.launch("image/*"));
        btnPickPc.setOnClickListener(v -> {
            String[] types = new String[]{"image/*"};
            launcherOpenDocument.launch(types);
        });
        btnPickSub1.setOnClickListener(v -> { currentSub = 1; launcherSubPick.launch("image/*"); });
        btnPickSub2.setOnClickListener(v -> { currentSub = 2; launcherSubPick.launch("image/*"); });
        btnPickSub3.setOnClickListener(v -> { currentSub = 3; launcherSubPick.launch("image/*"); });
        btnSaveSub1.setOnClickListener(v -> { if (uriSub1 != null) { saveLocalBanner("sub1", uriSub1); uploadSelected("sub1", uriSub1); } });
        btnSaveSub2.setOnClickListener(v -> { if (uriSub2 != null) { saveLocalBanner("sub2", uriSub2); uploadSelected("sub2", uriSub2); } });
        btnSaveSub3.setOnClickListener(v -> { if (uriSub3 != null) { saveLocalBanner("sub3", uriSub3); uploadSelected("sub3", uriSub3); } });

        btnSaveBg.setOnClickListener(v -> {
            String token = SharedPrefsUtils.getToken(this);
            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            // If user picked files, upload them; otherwise save selection keys
            if (pickedMobileUri != null) {
                uploadSelected("mobile", pickedMobileUri);
            } else {
                api.setBackground("Bearer " + token, new AdminBackgroundRequest("mobile", spMobileBg.getSelectedItem().toString()))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                if (pickedPcUri != null) {
                                    uploadSelected("pc", pickedPcUri);
                                } else {
                                    api.setBackground("Bearer " + token, new AdminBackgroundRequest("pc", spPcBg.getSelectedItem().toString()))
                                        .enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                if (response.isSuccessful()) {
                                                    Toast.makeText(AdminDashboardActivity.this, "Đã lưu ảnh nền", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(AdminDashboardActivity.this, "Lưu ảnh nền thất bại (PC)", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Toast.makeText(AdminDashboardActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                }
                            } else {
                                Toast.makeText(AdminDashboardActivity.this, "Lưu ảnh nền thất bại (Mobile)", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(AdminDashboardActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        if (tvSystemHeader != null) {
            tvSystemHeader.setOnClickListener(v -> togglePanel());
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

    private void saveLocalBanner(String key, android.net.Uri uri) {
        SharedPrefsUtils.putString(this, "local_banner_" + key, uri.toString());
        Toast.makeText(this, "Đã lưu banner (offline mode)", Toast.LENGTH_SHORT).show();
    }

    private void uploadSelected(String target, android.net.Uri uri) {
        try {
            String token = SharedPrefsUtils.getToken(this);
            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            String mime = getContentResolver().getType(uri);
            if (mime == null) mime = "image/*";
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            byte[] bytes = readAll(is);
            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse(mime), bytes);
            
            // Unified logic: Upload everything to 'banners' table
            String title = "Banner";
            int priority = 1;
            if (target.equals("mobile")) {
                title = "Banner Chính";
                priority = 10;
            } else if (target.startsWith("sub")) {
                title = "Banner Phụ " + target.replace("sub", "");
                priority = 5;
            }

            okhttp3.MultipartBody.Part imagePart = okhttp3.MultipartBody.Part.createFormData("image", "banner_" + target + ".jpg", fileBody);
            okhttp3.RequestBody titleBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), title);
            okhttp3.RequestBody ctaBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), "Xem ngay");
            okhttp3.RequestBody linkBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), "");
            okhttp3.RequestBody priorityBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), String.valueOf(priority));
            
            // Use uploadBannerV2 (Header Auth) for better security and consistency
            api.uploadBannerV2("Bearer " + token, imagePart, titleBody, ctaBody, linkBody, priorityBody).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminDashboardActivity.this, "Đã thêm banner mới: " + target, Toast.LENGTH_SHORT).show();
                        // Trigger a refresh on HomeActivity if possible, or just let the user navigate
                    } else {
                        Toast.makeText(AdminDashboardActivity.this, "Lỗi thêm banner (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                    Toast.makeText(AdminDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePanel() {
        panelExpanded = !panelExpanded;
        int vis = panelExpanded ? android.view.View.VISIBLE : android.view.View.GONE;
        animateVisibility(spMobileBg, vis);
        animateVisibility(spPcBg, vis);
        animateVisibility(ivPreviewMobile, vis);
        animateVisibility(ivPreviewPc, vis);
        animateVisibility(btnPickMobile, vis);
        animateVisibility(btnPickPc, vis);
        animateVisibility(btnSaveBg, vis);
    }
    private void animateVisibility(android.view.View v, int toVis) {
        if (toVis == android.view.View.VISIBLE) {
            v.setAlpha(0f);
            v.setVisibility(android.view.View.VISIBLE);
            v.animate().alpha(1f).setDuration(300).start();
        } else {
            v.animate().alpha(0f).setDuration(300).withEndAction(() -> v.setVisibility(android.view.View.GONE)).start();
        }
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


// ...
    private void loadPreview(android.widget.ImageView target, String url) {
        if (url == null || url.isEmpty()) return;
        
        String finalUrl = url;
        if (!url.startsWith("http")) {
            String baseUrl = RetrofitClient.BASE_URL;
            if (baseUrl.endsWith("api/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
            }
            // Check if backend stores filename only, append uploads path
            if (!url.contains("/")) {
                finalUrl = baseUrl + "uploads/backgrounds/" + url;
            } else {
                finalUrl = baseUrl + url;
            }
        }

        Glide.with(this)
            .load(finalUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(target);
    }

    private void loadCurrentBackground() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getBackground("Bearer " + token).enqueue(new Callback<AdminBackgroundResponse>() {
            @Override
            public void onResponse(Call<AdminBackgroundResponse> call, Response<AdminBackgroundResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String mobile = response.body().getBgMobile();
                    String pc = response.body().getBgPc();
                    setSpinnerSelection(spMobileBg, mobile);
                    setSpinnerSelection(spPcBg, pc);
                    loadPreview(ivPreviewMobile, mobile);
                    loadPreview(ivPreviewPc, pc);
                    loadPreview(ivSub1, response.body().getBgSub1());
                    loadPreview(ivSub2, response.body().getBgSub2());
                    loadPreview(ivSub3, response.body().getBgSub3());
                }
            }
            @Override
            public void onFailure(Call<AdminBackgroundResponse> call, Throwable t) { }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
