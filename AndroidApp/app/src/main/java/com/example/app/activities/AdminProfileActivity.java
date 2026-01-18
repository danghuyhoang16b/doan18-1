package com.example.app.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app.R;
import com.example.app.network.ApiService;
import com.example.app.models.ProfileResponse;
import com.example.app.models.UserIdRequest;
import com.example.app.models.UserUpdateRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.network.ApiClient;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private TextView tvUsername, tvRole;
    private EditText etFullName, etEmail, etPhone;
    private Button btnUploadAvatar, btnSave;
    private android.net.Uri selectedAvatar;
    private androidx.activity.result.ActivityResultLauncher<String> avatarPicker;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(android.content.Context context, Intent intent) { loadProfile(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hồ sơ Quản trị");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUploadAvatar = findViewById(R.id.btnUploadAvatar);
        btnSave = findViewById(R.id.btnSave);
        loadProfile();
        avatarPicker = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAvatar = uri;
                        ivAvatar.setImageURI(uri);
                    }
                });
        btnUploadAvatar.setOnClickListener(v -> pickAvatar());
        btnSave.setOnClickListener(v -> saveProfile());
        registerReceiver(receiver, new IntentFilter("com.example.app.PROFILE_UPDATED"));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(receiver); } catch (Exception ignored) {}
    }

    private void loadProfile() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        api.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse p = response.body();
                    tvUsername.setText(p.username);
                    tvRole.setText(p.role);
                    etFullName.setText(p.full_name);
                    etEmail.setText(p.email);
                    etPhone.setText(p.phone);

                    // Load avatar
                    if (p.avatar != null && !p.avatar.isEmpty()) {
                        String url = p.avatar.startsWith("http") ? p.avatar :
                            com.example.app.network.ApiConstants.AVATAR_BASE_URL + p.avatar;
                        com.bumptech.glide.Glide.with(AdminProfileActivity.this)
                            .load(url)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.mipmap.ic_launcher_round)
                            .into(ivAvatar);
                    }
                }
            }
            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) { }
        });
    }

    private void pickAvatar() {
        if (avatarPicker != null) avatarPicker.launch("image/*");
    }

    private void saveProfile() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();

        // If avatar selected, upload first then update profile
        if (selectedAvatar != null) {
            try {
                String mime = getContentResolver().getType(selectedAvatar);
                java.io.InputStream is = getContentResolver().openInputStream(selectedAvatar);
                byte[] bytes = readAll(is);
                android.util.Log.d("AvatarUpload", "File size: " + bytes.length + " bytes, MIME: " + mime);

                RequestBody body = RequestBody.create(MediaType.parse(mime != null ? mime : "image/*"), bytes);
                MultipartBody.Part img = MultipartBody.Part.createFormData("image", "avatar.jpg", body);

                android.widget.Toast.makeText(this, "Đang tải ảnh lên...", android.widget.Toast.LENGTH_SHORT).show();
                api.uploadAvatar("Bearer " + token, img).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String respBody = response.body() != null ? response.body().string() : "null";
                            String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                            android.util.Log.d("AvatarUpload", "Response code: " + response.code() + ", body: " + respBody + ", error: " + errBody);
                        } catch (Exception e) {
                            android.util.Log.e("AvatarUpload", "Error reading response", e);
                        }

                        if (response.isSuccessful()) {
                            android.widget.Toast.makeText(AdminProfileActivity.this,
                                "Tải ảnh thành công!", android.widget.Toast.LENGTH_SHORT).show();
                            updateProfileAndFinish(api, token);
                        } else {
                            android.widget.Toast.makeText(AdminProfileActivity.this,
                                "Lỗi tải ảnh: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                            updateProfileAndFinish(api, token);
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        android.util.Log.e("AvatarUpload", "Upload failed", t);
                        android.widget.Toast.makeText(AdminProfileActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        updateProfileAndFinish(api, token);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AvatarUpload", "Error preparing upload", e);
                android.widget.Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                updateProfileAndFinish(api, token);
            }
        } else {
            updateProfileAndFinish(api, token);
        }
    }

    private void updateProfileAndFinish(ApiService api, String token) {
        UserUpdateRequest req = new UserUpdateRequest(null,
                etFullName.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                null);
        api.updateProfile("Bearer " + token, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                android.widget.Toast.makeText(AdminProfileActivity.this,
                    "Đã lưu hồ sơ", android.widget.Toast.LENGTH_SHORT).show();
                sendBroadcast(new android.content.Intent("com.example.app.PROFILE_UPDATED"));
                finish();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                android.widget.Toast.makeText(AdminProfileActivity.this,
                    "Lỗi lưu: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
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
}
