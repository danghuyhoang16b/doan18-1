package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.example.app.network.ApiService;
import com.example.app.models.LoginResponse;
import com.example.app.models.TeacherLoginRequest;
import com.example.app.models.StudentLoginRequest;
import com.example.app.models.User;
import com.example.app.network.ApiClient;
import com.example.app.utils.SharedPrefsUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private Spinner spRole;
    private TextView tvCaptchaQuestion;
    private EditText etCaptchaAnswer;
    private Button btnOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in → luôn vào HomeActivity
        User currentUser = SharedPrefsUtils.getUser(this);
        if (currentUser != null && SharedPrefsUtils.getToken(this) != null) {
            navigateBasedOnRole(currentUser.getRole());
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        spRole = findViewById(R.id.spRole);
        tvCaptchaQuestion = findViewById(R.id.tvCaptchaQuestion);
        etCaptchaAnswer = findViewById(R.id.etCaptchaAnswer);
        btnOtp = findViewById(R.id.btnOtp);

        btnLogin.setOnClickListener(v -> login());
        
            tvForgotPassword.setOnClickListener(v -> {
                com.example.app.utils.ToastManager.show(this, "Vui lòng liên hệ quản trị viên để lấy lại mật khẩu");
            });

        String[] roles = new String[]{getString(R.string.role_teacher), getString(R.string.role_student), getString(R.string.role_parent)};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);
        spRole.setSelection(0);
        spRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                btnOtp.setVisibility(position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        btnOtp.setOnClickListener(v -> {
            if (spRole.getSelectedItemPosition() == 2) {
                startActivity(new Intent(this, ParentOtpActivity.class));
            } else {
                com.example.app.utils.ToastManager.show(this, "Chỉ áp dụng cho phụ huynh");
            }
        });
    }

    private void navigateBasedOnRole(String role) {
        Intent intent = new Intent(this, HomeActivity.class);
        // Handle Red Star role (treat as student for now, or separate if needed)
        if ("red_star".equals(role)) {
            // Red Star specific logic can be handled in HomeActivity
        }
        startActivity(intent);
        finish();
    }

    private android.app.ProgressDialog progressDialog;

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            com.example.app.utils.ToastManager.show(this, "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        ApiService apiService = ApiClient.getInstance().getApiService();
        Call<LoginResponse> call;
        int role = spRole.getSelectedItemPosition();
        String captchaToken = tvCaptchaQuestion.getTag() != null ? tvCaptchaQuestion.getTag().toString() : null;
        String captchaAns = etCaptchaAnswer.getText().toString().trim();
        if (role == 0) {
            call = apiService.loginTeacher(new TeacherLoginRequest(username, password, captchaToken, captchaAns));
        } else if (role == 1) {
            // Student Login
            // Allow HS-xxxxx or username or email
            // Regex: ^HS-\d{5}$ OR contains @ OR just alphanumeric
            if (username.isEmpty()) {
                com.example.app.utils.ToastManager.show(this, "Vui lòng nhập tài khoản");
                return;
            }
            // Removed strict regex check to allow more flexible usernames (like old accounts or red stars logging in as student view)
            call = apiService.loginStudent(new StudentLoginRequest(username, password, captchaToken, captchaAns));
        } else {
            // Parent Login - Allow flexible username format for linked students (e.g. HS-10A1-10)
            if (username.isEmpty()) {
                com.example.app.utils.ToastManager.show(this, "Vui lòng nhập mã học sinh con");
                return;
            }
            // Regex check removed here as well to be consistent with backend flexibility
            // if (!username.matches("^HS-\\d{5}$")) { ... }
            
            call = apiService.loginParentPassword(new com.example.app.models.ParentLoginRequest(username, password));
        }

        if (progressDialog == null) {
            progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Đang đăng nhập...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
        btnLogin.setEnabled(false);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressDialog.dismiss();
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    com.example.app.utils.ToastManager.show(LoginActivity.this, loginResponse.getMessage());
                    
                    SharedPrefsUtils.saveUser(LoginActivity.this, loginResponse.getUser());
                    SharedPrefsUtils.saveToken(LoginActivity.this, loginResponse.getToken());
                    
                    if (loginResponse.isCaptchaRequired()) {
                        tvCaptchaQuestion.setText(loginResponse.getCaptchaQuestion());
                        tvCaptchaQuestion.setTag(loginResponse.getCaptchaToken());
                        findViewById(R.id.layoutCaptcha).setVisibility(android.view.View.VISIBLE);
                        return;
                    }
                    
                    String bearer = "Bearer " + loginResponse.getToken();
                    if ("student".equals(loginResponse.getUser().getRole())) {
                        checkStudentOnboarding(bearer, loginResponse.getUser().getRole());
                    } else if ("parent".equals(loginResponse.getUser().getRole())) {
                        checkStudentOnboarding(bearer, loginResponse.getUser().getRole());
                    } else {
                        navigateBasedOnRole(loginResponse.getUser().getRole());
                    }
                } else {
                    String body = null;
                    try { if (response.errorBody()!=null) body = response.errorBody().string(); } catch (Exception ignored) {}
                    String msg = "Đăng nhập thất bại: " + response.code();
                    boolean needCaptcha = false;
                    String cq = null, ct = null;
                    try {
                        if (body != null && !body.isEmpty()) {
                            org.json.JSONObject o = new org.json.JSONObject(body);
                            msg = o.optString("message", msg);
                            needCaptcha = o.optBoolean("captcha_required", false);
                            cq = o.optString("captcha_question", null);
                            ct = o.optString("captcha_token", null);
                        }
                    } catch (Exception ignored) {}
                    if (needCaptcha) {
                        if (cq != null) tvCaptchaQuestion.setText(cq);
                        if (ct != null) tvCaptchaQuestion.setTag(ct);
                        findViewById(R.id.layoutCaptcha).setVisibility(android.view.View.VISIBLE);
                    }
                    com.example.app.utils.ToastManager.show(LoginActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                btnLogin.setEnabled(true);
                com.example.app.utils.ToastManager.show(LoginActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void checkStudentOnboarding(String bearerToken, String role) {
        if ("parent".equals(role)) {
            // Check Parent Profile Completeness (User info)
            ApiClient.getInstance().getApiService().isProfileComplete(bearerToken).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> resp) {
                    boolean complete = false;
                    try {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            String s = resp.body().string();
                            org.json.JSONObject o = new org.json.JSONObject(s);
                            complete = o.optBoolean("complete", false);
                        }
                    } catch (Exception ignored) {}

                    if (!complete) {
                        // Redirect to Parent Profile to update info
                        android.widget.Toast.makeText(LoginActivity.this, "Vui lòng cập nhật thông tin cá nhân", android.widget.Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, ParentProfileActivity.class));
                        finish();
                    } else {
                        // Check linked student completeness if needed, or just go home
                        // For now, assume if parent is complete, go home. 
                        // Or we can chain checkStudentOnboarding (the original one) to check the child.
                        // But let's stick to the request: "nhảy vào profile cửa phụ huynh"
                        navigateBasedOnRole(role);
                    }
                }
                @Override
                public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                    navigateBasedOnRole(role);
                }
            });
            return;
        }

        ApiClient.getInstance().getApiService().isStudentComplete(bearerToken).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> resp) {
                boolean complete = false;
                try { 
                    if (resp.isSuccessful() && resp.body()!=null) {
                        String s = resp.body().string(); 
                        org.json.JSONObject o = new org.json.JSONObject(s); 
                        complete = o.optBoolean("complete", false);
                    }
                } catch (Exception ignored) {}
                
                if (!complete) {
                    if ("student".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, StudentOnboardingActivity.class));
                        finish();
                    } else {
                        // Old logic for parent checking child (kept as fallback or removed if above block handles parent)
                         navigateBasedOnRole(role);
                    }
                } else {
                    navigateBasedOnRole(role);
                }
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                navigateBasedOnRole(role);
            }
        });
    }
}
