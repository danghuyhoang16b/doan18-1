package com.example.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.models.CreateNotificationRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNotificationActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private RadioGroup rgPriority;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        rgPriority = findViewById(R.id.rgPriority);
        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> createNotification());
    }

    private void createNotification() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = "normal";
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbHigh) priority = "high";
        else if (checkedId == R.id.rbUrgent) priority = "urgent";

        String token = SharedPrefsUtils.getToken(this);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.createNotification(new CreateNotificationRequest(token, title, content, priority))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreateNotificationActivity.this, "Tạo thông báo thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateNotificationActivity.this, "Không thể tạo thông báo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(CreateNotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
