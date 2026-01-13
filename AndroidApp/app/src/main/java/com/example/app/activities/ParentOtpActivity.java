package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.models.LoginResponse;
import com.example.app.models.PhoneRequest;
import com.example.app.models.VerifyOtpRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentOtpActivity extends AppCompatActivity {
    private EditText etPhone;
    private EditText etOtp;
    private Button btnRequest;
    private Button btnVerify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_otp);
        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        btnRequest = findViewById(R.id.btnRequest);
        btnVerify = findViewById(R.id.btnVerify);
        btnRequest.setOnClickListener(v -> requestOtp());
        btnVerify.setOnClickListener(v -> verifyOtp());
    }
    private void requestOtp() {
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) { Toast.makeText(this,"Vui lòng nhập số điện thoại",Toast.LENGTH_SHORT).show(); return; }
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.requestOtp(new PhoneRequest(phone)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    Toast.makeText(ParentOtpActivity.this,"Đã gửi OTP",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ParentOtpActivity.this,"Gửi OTP thất bại",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(ParentOtpActivity.this,"Lỗi: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void verifyOtp() {
        String phone = etPhone.getText().toString().trim();
        String code = etOtp.getText().toString().trim();
        if (phone.isEmpty() || code.isEmpty()) { Toast.makeText(this,"Vui lòng nhập đủ thông tin",Toast.LENGTH_SHORT).show(); return; }
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.verifyOtp(new VerifyOtpRequest(phone, code)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    SharedPrefsUtils.saveUser(ParentOtpActivity.this, response.body().getUser());
                    SharedPrefsUtils.saveToken(ParentOtpActivity.this, response.body().getToken());
                    Toast.makeText(ParentOtpActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ParentOtpActivity.this, HomeActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(ParentOtpActivity.this,"Xác minh OTP thất bại",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(ParentOtpActivity.this,"Lỗi: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
