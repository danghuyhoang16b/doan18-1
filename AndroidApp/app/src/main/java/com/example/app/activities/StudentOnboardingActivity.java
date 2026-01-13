package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class StudentOnboardingActivity extends AppCompatActivity {
    private EditText etFullName, etBirthDate, etAddress, etPhone, etEmail, etGuardianName, etGuardianPhone;
    private Button btnSave, btnDraft;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_onboarding);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Hoàn thiện hồ sơ học sinh");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        etFullName = findViewById(R.id.etFullName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etGuardianName = findViewById(R.id.etGuardianName);
        etGuardianPhone = findViewById(R.id.etGuardianPhone);
        
        etBirthDate.setFocusable(false);
        etBirthDate.setOnClickListener(v -> showDatePicker());
        
        btnSave = findViewById(R.id.btnSave);
        btnDraft = findViewById(R.id.btnDraft);
        loadDraft();
        btnDraft.setOnClickListener(v -> saveDraft());
        btnSave.setOnClickListener(v -> submit());
    }
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etBirthDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, y, m, d).show();
    }
    private void loadDraft() {
        etFullName.setText(SharedPrefsUtils.getString(this,"draft_full_name",""));
        etBirthDate.setText(SharedPrefsUtils.getString(this,"draft_birth_date",""));
        etAddress.setText(SharedPrefsUtils.getString(this,"draft_address",""));
        etPhone.setText(SharedPrefsUtils.getString(this,"draft_phone",""));
        etEmail.setText(SharedPrefsUtils.getString(this,"draft_email",""));
        etGuardianName.setText(SharedPrefsUtils.getString(this,"draft_guardian_name",""));
        etGuardianPhone.setText(SharedPrefsUtils.getString(this,"draft_guardian_phone",""));
    }
    private void saveDraft() {
        SharedPrefsUtils.putString(this,"draft_full_name", etFullName.getText().toString().trim());
        SharedPrefsUtils.putString(this,"draft_birth_date", etBirthDate.getText().toString().trim());
        SharedPrefsUtils.putString(this,"draft_address", etAddress.getText().toString().trim());
        SharedPrefsUtils.putString(this,"draft_phone", etPhone.getText().toString().trim());
        SharedPrefsUtils.putString(this,"draft_email", etEmail.getText().toString().trim());
        SharedPrefsUtils.putString(this,"draft_guardian_name", etGuardianName.getText().toString().trim());
        SharedPrefsUtils.putString(this,"draft_guardian_phone", etGuardianPhone.getText().toString().trim());
        Toast.makeText(this,"Đã lưu nháp",Toast.LENGTH_SHORT).show();
    }
    private void submit() {
        if (etFullName.getText().toString().trim().isEmpty()
                || etBirthDate.getText().toString().trim().isEmpty()
                || etAddress.getText().toString().trim().isEmpty()
                || etPhone.getText().toString().trim().isEmpty()
                || etEmail.getText().toString().trim().isEmpty()
                || etGuardianName.getText().toString().trim().isEmpty()
                || etGuardianPhone.getText().toString().trim().isEmpty()) {
            Toast.makeText(this,"Vui lòng nhập đầy đủ thông tin",Toast.LENGTH_SHORT).show();
            return;
        }
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Map<String,Object> body = new HashMap<>();
        body.put("full_name", etFullName.getText().toString().trim());
        body.put("birth_date", etBirthDate.getText().toString().trim());
        body.put("address", etAddress.getText().toString().trim());
        body.put("phone", etPhone.getText().toString().trim());
        body.put("email", etEmail.getText().toString().trim());
        body.put("guardian_name", etGuardianName.getText().toString().trim());
        body.put("guardian_phone", etGuardianPhone.getText().toString().trim());
        api.updateStudentFull("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_full_name","");
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_birth_date","");
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_address","");
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_phone","");
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_email","");
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_guardian_name","");
                    SharedPrefsUtils.putString(StudentOnboardingActivity.this,"draft_guardian_phone","");
                    startActivity(new android.content.Intent(StudentOnboardingActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(StudentOnboardingActivity.this,"Lưu thất bại",Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { Toast.makeText(StudentOnboardingActivity.this,"Lỗi kết nối",Toast.LENGTH_SHORT).show(); }
        });
    }
    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
