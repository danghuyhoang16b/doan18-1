package com.example.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
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

public class StudentProfileActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private TextView tvUsername, tvRole, tvProfileStatus;
    private EditText etFullName, etEmail, etPhone;
    private Button btnSave, btnUploadAvatar, btnSetClass;
    private android.widget.Spinner spClass;
    private android.net.Uri selectedAvatar;
    private androidx.activity.result.ActivityResultLauncher<String> avatarPicker;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { loadProfile(); checkProfileStatus(); }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_student);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hồ sơ Học sinh");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnUploadAvatar = findViewById(R.id.btnUploadAvatar);
        spClass = findViewById(R.id.spClass);
        btnSetClass = findViewById(R.id.btnSetClass);
        loadProfile();
        checkProfileStatus();
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
        loadClasses();
        btnSetClass.setOnClickListener(v -> setClass());
        registerReceiver(receiver, new IntentFilter("com.example.app.PROFILE_UPDATED"));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(receiver); } catch (Exception ignored) {}
    }
    private void checkProfileStatus() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        api.isStudentComplete("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                boolean complete = false;
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        complete = o.optBoolean("complete", false);
                    }
                } catch (Exception ignored) {}
                
                if (complete) {
                    tvProfileStatus.setText("Trạng thái hồ sơ: Đã hoàn thiện");
                    tvProfileStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                } else {
                    tvProfileStatus.setText("Trạng thái hồ sơ: Chưa hoàn thiện");
                    tvProfileStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                tvProfileStatus.setText("Trạng thái hồ sơ: Lỗi kiểm tra");
            }
        });
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
                            url = com.example.app.network.ApiConstants.AVATAR_BASE_URL + url;
                        }
                        com.bumptech.glide.Glide.with(StudentProfileActivity.this)
                            .load(url)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.mipmap.ic_launcher_round)
                            .into(ivAvatar);
                    }
                }
            }
            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                android.widget.Toast.makeText(StudentProfileActivity.this, "Lỗi tải hồ sơ: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveProfile() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
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
                api.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call2, Response<ProfileResponse> resp2) {
                        sendBroadcast(new Intent("com.example.app.PROFILE_UPDATED"));
                        finish();
                    }
                    @Override public void onFailure(Call<ProfileResponse> call2, Throwable t) {
                        sendBroadcast(new Intent("com.example.app.PROFILE_UPDATED"));
                        finish();
                    }
                });
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void loadClasses() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        api.getAllClasses().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        java.util.List<String> names = new java.util.ArrayList<>();
                        for (int i=0;i<arr.length();i++) names.add(arr.getJSONObject(i).optString("name"));
                        spClass.setAdapter(new android.widget.ArrayAdapter<>(StudentProfileActivity.this, android.R.layout.simple_spinner_item, names));
                        ApiService api2 = ApiClient.getInstance().getApiService();
                        api2.getStudentClass("Bearer " + token).enqueue(new Callback<ResponseBody>() {
                            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    if (response.isSuccessful() && response.body()!=null) {
                                        String s2 = response.body().string();
                                        org.json.JSONObject o = new org.json.JSONObject(s2);
                                        String cname = o.optString("name");
                                        for (int i=0;i<names.size();i++) if (names.get(i).equals(cname)) { spClass.setSelection(i); break; }
                                    }
                                } catch (Exception ignored) {}
                            }
                            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    private void setClass() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        // resolve class id by name
        api.getAllClasses().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        String sel = spClass.getSelectedItem()!=null? spClass.getSelectedItem().toString(): null;
                        int classId = 0;
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);
                            if (o.optString("name").equals(sel)) { classId = o.optInt("id"); break; }
                        }
                        if (classId>0) {
                            java.util.Map<String,Integer> body = new java.util.HashMap<>();
                            body.put("class_id", classId);
                            api.setStudentClass("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
                                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }
                                @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
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
}
