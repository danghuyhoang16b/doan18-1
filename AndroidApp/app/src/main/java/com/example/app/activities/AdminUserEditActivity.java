package com.example.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app.R;
import com.example.app.network.ApiService;
import com.example.app.models.User;
import com.example.app.models.UserIdRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.network.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserEditActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etFullName, etEmail, etPhone;
    private Spinner spinnerRole;
    private Button btnSave;
    private ApiService apiService;
    private Integer userId = null;
    private String[] roles = {"student", "teacher", "parent", "admin"};
    private String currentRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        apiService = ApiClient.getInstance().getApiService();

        if (getIntent().hasExtra("user_id")) {
            userId = getIntent().getIntExtra("user_id", -1);
            getSupportActionBar().setTitle("Cập nhật người dùng");
            etUsername.setEnabled(false); // Username cannot be changed
            loadUserData();
        } else {
            getSupportActionBar().setTitle("Thêm người dùng mới");
            String defaultRole = getIntent().getStringExtra("default_role");
            if (defaultRole != null) {
                for (int i = 0; i < roles.length; i++) {
                    if (roles[i].equals(defaultRole)) {
                        spinnerRole.setSelection(i);
                        break;
                    }
                }
            }
        }

        btnSave.setOnClickListener(v -> saveUser());
    }

    private void loadUserData() {
        String token = SharedPrefsUtils.getToken(this);
        apiService.getUser("Bearer " + token, userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    etUsername.setText(u.getUsername());
                    etFullName.setText(u.getFullName());
                    etEmail.setText(u.getEmail());
                    etPhone.setText(u.getPhone());
                    
                    for (int i = 0; i < roles.length; i++) {
                        if (roles[i].equals(u.getRole())) {
                            spinnerRole.setSelection(i);
                            break;
                        }
                    }
                    currentRole = u.getRole();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AdminUserEditActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        
        if (spinnerRole.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "Vui lòng chọn vai trò", Toast.LENGTH_SHORT).show();
            return;
        }
        String role = roles[spinnerRole.getSelectedItemPosition()];
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (userId == null && (username.isEmpty() || password.isEmpty() || fullName.isEmpty())) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId != null && fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        if (userId != null) {
            user.setId(userId);
        }
        user.setUsername(username);
        
        if (!password.isEmpty()) {
            user.setPassword(password);
        }
        
        user.setFullName(fullName);
        if (userId != null && currentRole != null && role.equals(currentRole)) {
            role = "";
        }
        user.setRole(role);
        user.setEmail(email);
        user.setPhone(phone);

        String token = SharedPrefsUtils.getToken(this);
        okhttp3.Call callRaw = null;
        retrofit2.Call<okhttp3.ResponseBody> call;
        if (userId == null) {
            call = apiService.createUser("Bearer " + token, user);
        } else {
            call = apiService.updateUser("Bearer " + token, user);
        }

        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    String msg = "Lưu thành công";
                    try {
                        if (response.body() != null) {
                            String s = response.body().string();
                            org.json.JSONObject o = new org.json.JSONObject(s);
                            msg = o.optString("message", msg);
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AdminUserEditActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String err = "Lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String s = response.errorBody().string();
                            org.json.JSONObject o = new org.json.JSONObject(s);
                            String m = o.optString("message", "");
                            if (!m.isEmpty()) err = m;
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AdminUserEditActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(AdminUserEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
