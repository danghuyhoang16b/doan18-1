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
import com.example.app.api.ApiService;
import com.example.app.models.ProfileResponse;
import com.example.app.models.UserIdRequest;
import com.example.app.models.UserUpdateRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

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
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getProfile("Bearer " + token, new UserIdRequest(null)).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse p = response.body();
                    tvUsername.setText(p.username);
                    tvRole.setText(p.role);
                    etFullName.setText(p.full_name);
                    etEmail.setText(p.email);
                    etPhone.setText(p.phone);
                }
            }
            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) { }
        });
    }
    private void saveProfile() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        if (selectedAvatar != null) {
            try {
                String mime = getContentResolver().getType(selectedAvatar);
                java.io.InputStream is = getContentResolver().openInputStream(selectedAvatar);
                byte[] bytes = readAll(is);
                RequestBody body = RequestBody.create(MediaType.parse(mime != null ? mime : "image/*"), bytes);
                MultipartBody.Part img = MultipartBody.Part.createFormData("image", "avatar.jpg", body);
                api.uploadAvatar("Bearer " + token, img).enqueue(new Callback<ResponseBody>() {
                    @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }
                    @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
                });
            } catch (Exception ignored) { }
        }
        UserUpdateRequest req = new UserUpdateRequest(null,
                etFullName.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                null);
        api.updateProfile("Bearer " + token, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                sendBroadcast(new android.content.Intent("com.example.app.PROFILE_UPDATED"));
                finish();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
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
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getTeacherClasses(new com.example.app.models.TokenRequest(token)).enqueue(new retrofit2.Callback<java.util.List<com.example.app.models.ClassModel>>() {
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
