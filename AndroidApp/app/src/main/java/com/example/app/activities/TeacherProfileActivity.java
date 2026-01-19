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

public class TeacherProfileActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private TextView tvUsername, tvRole;
    private EditText etFullName, etEmail, etPhone;
    private Button btnSave, btnUploadAvatar, btnGoViolation, btnRequestManage;
    private android.widget.Spinner spClassTeacher;
    private android.widget.TextView tvNoClassHint;
    private android.net.Uri selectedAvatar;
    private androidx.activity.result.ActivityResultLauncher<String> avatarPicker;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(android.content.Context context, Intent intent) { loadProfile(); }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_teacher);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hồ sơ Giáo viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnUploadAvatar = findViewById(R.id.btnUploadAvatar);
        spClassTeacher = findViewById(R.id.spClassTeacher);
        btnGoViolation = findViewById(R.id.btnGoViolation);
        btnRequestManage = findViewById(R.id.btnRequestManage);
        tvNoClassHint = findViewById(R.id.tvNoClassHint);
        loadProfile();
        btnSave.setOnClickListener(v -> saveProfile());
        avatarPicker = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAvatar = uri;
                        ivAvatar.setImageURI(uri);
                    }
                });
        btnUploadAvatar.setOnClickListener(v -> { if (avatarPicker != null) avatarPicker.launch("image/*"); });
        loadTeacherClasses();
        btnGoViolation.setOnClickListener(v -> {
            startActivity(new Intent(TeacherProfileActivity.this, ConductEntryActivity.class));
        });
        btnRequestManage.setOnClickListener(v -> startActivity(new Intent(TeacherProfileActivity.this, ManageClassRequestActivity.class)));
        registerReceiver(receiver, new IntentFilter("com.example.app.PROFILE_UPDATED"));
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherClasses();
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
                    
                    if (p.avatar != null && !p.avatar.isEmpty()) {
                        String url = p.avatar;
                        if (!url.startsWith("http")) {
                            url = com.example.app.utils.UrlUtils.getFullUrl(TeacherProfileActivity.this, "uploads/avatars/" + url);
                        }
                        com.bumptech.glide.Glide.with(TeacherProfileActivity.this)
                            .load(url)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
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
    private void saveProfile() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        
        Runnable updateTask = () -> {
            UserUpdateRequest req = new UserUpdateRequest(null,
                    etFullName.getText().toString().trim(),
                    etEmail.getText().toString().trim(),
                    etPhone.getText().toString().trim(),
                    null);
            api.updateProfile("Bearer " + token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(TeacherProfileActivity.this, "Cập nhật thành công", android.widget.Toast.LENGTH_SHORT).show();
                        sendBroadcast(new android.content.Intent("com.example.app.PROFILE_UPDATED"));
                        finish();
                    } else {
                        android.widget.Toast.makeText(TeacherProfileActivity.this, "Lỗi cập nhật: " + response.message(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    android.widget.Toast.makeText(TeacherProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        };

        if (selectedAvatar != null) {
            try {
                String mime = getContentResolver().getType(selectedAvatar);
                java.io.InputStream is = getContentResolver().openInputStream(selectedAvatar);
                byte[] bytes = readAll(is);
                RequestBody body = RequestBody.create(MediaType.parse(mime != null ? mime : "image/*"), bytes);
                MultipartBody.Part img = MultipartBody.Part.createFormData("image", "avatar.jpg", body);
                
                android.widget.Toast.makeText(this, "Đang tải ảnh...", android.widget.Toast.LENGTH_SHORT).show();
                api.uploadAvatar("Bearer " + token, img).enqueue(new Callback<ResponseBody>() {
                    @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            updateTask.run();
                        } else {
                            String errorMsg = "Lỗi tải ảnh";
                            try {
                                if (response.errorBody() != null) {
                                    org.json.JSONObject errorJson = new org.json.JSONObject(response.errorBody().string());
                                    errorMsg = errorJson.optString("message", response.message());
                                } else {
                                    errorMsg = response.message();
                                }
                            } catch (Exception e) {
                                errorMsg = "Lỗi: " + response.code();
                            }
                            android.widget.Toast.makeText(TeacherProfileActivity.this, errorMsg, android.widget.Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                        android.widget.Toast.makeText(TeacherProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                 android.widget.Toast.makeText(this, "Lỗi đọc file ảnh", android.widget.Toast.LENGTH_SHORT).show();
            }
        } else {
            updateTask.run();
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
    private void loadTeacherClasses() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        api.getTeacherClasses("Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<com.example.app.models.ClassModel>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<com.example.app.models.ClassModel>> call, retrofit2.Response<java.util.List<com.example.app.models.ClassModel>> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    java.util.List<com.example.app.models.ClassModel> list = resp.body();
                    android.widget.ArrayAdapter<com.example.app.models.ClassModel> adapter = new android.widget.ArrayAdapter<>(TeacherProfileActivity.this, android.R.layout.simple_spinner_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spClassTeacher.setAdapter(adapter);
                    boolean enabled = list!=null && !list.isEmpty();
                    spClassTeacher.setEnabled(enabled);
                    tvNoClassHint.setVisibility(enabled ? android.view.View.GONE : android.view.View.VISIBLE);
                }
            }
            @Override public void onFailure(retrofit2.Call<java.util.List<com.example.app.models.ClassModel>> call, Throwable t) { }
        });
    }
}
