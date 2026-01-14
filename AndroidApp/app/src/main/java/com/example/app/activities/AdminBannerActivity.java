package com.example.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.ProgressDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.AdminBannerAdapter;
import com.example.app.api.ApiService;
import com.example.app.models.Banner;
import com.example.app.models.BannerIdRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBannerActivity extends AppCompatActivity {

    private RecyclerView rvBanners;
    private FloatingActionButton fabAddBanner;
    private AdminBannerAdapter adapter;
    private ActivityResultLauncher<String> imagePicker;
    private Uri selectedImageUri;
    private ImageView imgPreview; // Used in dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_banner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvBanners = findViewById(R.id.rvBanners);
        fabAddBanner = findViewById(R.id.fabAddBanner);

        rvBanners.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBannerAdapter(new ArrayList<>(), this::confirmDeleteBanner, this::toggleBannerActive);
        rvBanners.setAdapter(adapter);

        fabAddBanner.setOnClickListener(v -> showAddBannerDialog());

        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                if (imgPreview != null) imgPreview.setImageURI(uri);
            }
        });

        loadBanners();
    }

    private void loadBanners() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getActiveBanners().enqueue(new Callback<List<Banner>>() {
            @Override
            public void onResponse(Call<List<Banner>> call, Response<List<Banner>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setBanners(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Banner>> call, Throwable t) {
                Toast.makeText(AdminBannerActivity.this, "Lỗi tải banner: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBannerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_banner); // Need to create this layout or build dynamically
        
        // Since I haven't created dialog_add_banner.xml, let's build a simple view or better, create the file.
        // I'll create the file first.
        dialog.dismiss();
        createAddBannerDialog();
    }
    
    // Placeholder for now, will implement after creating the layout
    private void createAddBannerDialog() {
        // ...
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_banner, null);
        builder.setView(view);
        builder.setTitle("Thêm Banner Mới");

        imgPreview = view.findViewById(R.id.imgPreview);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etCta = view.findViewById(R.id.etCta);
        EditText etLink = view.findViewById(R.id.etLink);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(v -> imagePicker.launch("image/*"));

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            // Override in show() to prevent auto-close
        });
        builder.setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String cta = etCta.getText().toString();
            String link = etLink.getText().toString();

            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadBanner(selectedImageUri, title, cta, link, dialog);
        });
    }


// ...
    private void uploadBanner(Uri uri, String title, String cta, String link, Dialog dialog) {
        try {
            File file = getFileFromUri(uri);
            if (file == null) return;
            
            // Validate file size (max 5MB)
            if (file.length() > 5 * 1024 * 1024) {
                Toast.makeText(this, "Ảnh quá lớn (>5MB). Vui lòng chọn ảnh nhỏ hơn.", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang tải lên...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody ctaBody = RequestBody.create(MediaType.parse("text/plain"), cta);
            RequestBody linkBody = RequestBody.create(MediaType.parse("text/plain"), link);
            RequestBody tokenBody = RequestBody.create(MediaType.parse("text/plain"), SharedPrefsUtils.getToken(this));

            // Sử dụng uploadBannerV2 hỗ trợ Authorization header
            RequestBody priorityBody = RequestBody.create(MediaType.parse("text/plain"), "0");
            
            // Lấy token và thêm "Bearer "
            String token = "Bearer " + SharedPrefsUtils.getToken(this);

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            // Gọi hàm V2 với CẢ Header và Body (để chắc chắn server nhận được)
            apiService.uploadBannerV2(token, tokenBody, body, titleBody, ctaBody, linkBody, priorityBody).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminBannerActivity.this, "Thêm banner thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadBanners();
                        selectedImageUri = null;
                    } else {
                        Toast.makeText(AdminBannerActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AdminBannerActivity.this, "Lỗi upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleBannerActive(Banner banner, boolean isActive) {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.toggleBannerActive("Bearer " + token, new com.example.app.models.BannerToggleRequest(banner.getId(), isActive))
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        banner.setActive(isActive);
                        // Optional: Toast.makeText(AdminBannerActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminBannerActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                        // Revert switch visually if failed
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AdminBannerActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            });
    }

    private void confirmDeleteBanner(Banner banner) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Banner")
                .setMessage("Bạn có chắc chắn muốn xóa banner này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBanner(banner))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBanner(Banner banner) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = SharedPrefsUtils.getToken(this);
        apiService.deleteBanner(new BannerIdRequest(token, banner.getId())).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminBannerActivity.this, "Đã xóa banner", Toast.LENGTH_SHORT).show();
                    loadBanners();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AdminBannerActivity.this, "Lỗi xóa: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws java.io.IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = new File(getCacheDir(), "temp_banner_" + System.currentTimeMillis());
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return file;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isTaskRoot()) {
                startActivity(new Intent(this, AdminHomeActivity.class));
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
