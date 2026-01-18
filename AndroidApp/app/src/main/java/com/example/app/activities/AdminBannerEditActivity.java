package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.app.R;
import com.example.app.network.ApiService;
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

public class AdminBannerEditActivity extends AppCompatActivity {
    private EditText etIndex, etText, etLink;
    private Button btnSave, btnCancel, btnPickImage, btnUploadImage;
    private android.net.Uri selectedImage;
    private androidx.activity.result.ActivityResultLauncher<String> picker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_banner_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Chỉnh sửa banner");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        etIndex = findViewById(R.id.etIndex);
        etText = findViewById(R.id.etText);
        etLink = findViewById(R.id.etLink);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        picker = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
            if (uri!=null) selectedImage = uri;
        });
        btnPickImage.setOnClickListener(v -> { if (picker!=null) picker.launch("image/*"); });
        btnUploadImage.setOnClickListener(v -> uploadImage());
        btnSave.setOnClickListener(v -> saveTextLink());
        btnCancel.setOnClickListener(v -> finish());
    }
    private void uploadImage() {
        if (selectedImage==null) {
            android.widget.Toast.makeText(this, "Vui lòng chọn ảnh trước", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        try {
            String mime = getContentResolver().getType(selectedImage);
            java.io.InputStream is = getContentResolver().openInputStream(selectedImage);
            byte[] bytes = readAll(is);
            RequestBody body = RequestBody.create(MediaType.parse(mime!=null?mime:"image/*"), bytes);
            MultipartBody.Part img = MultipartBody.Part.createFormData("image","banner.jpg", body);
            String target = "sub"+etIndex.getText().toString().trim();
            RequestBody targetPart = RequestBody.create(okhttp3.MultipartBody.FORM, target);
            RequestBody tokenPart = RequestBody.create(okhttp3.MultipartBody.FORM, token);
            
            android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
            pd.setMessage("Đang tải lên...");
            pd.show();
            
            api.uploadBackground("Bearer " + token, img, targetPart, tokenPart).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { 
                    pd.dismiss();
                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(AdminBannerEditActivity.this, "Tải ảnh thành công!", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(AdminBannerEditActivity.this, "Lỗi tải ảnh: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) { 
                    pd.dismiss();
                    android.widget.Toast.makeText(AdminBannerEditActivity.this, "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) { 
            android.widget.Toast.makeText(this, "Lỗi đọc file: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    private void saveTextLink() {
        String token = SharedPrefsUtils.getToken(this);
        ApiService api = ApiClient.getInstance().getApiService();
        java.util.Map<String,Object> body = new java.util.HashMap<>();
        body.put("index", Integer.parseInt(etIndex.getText().toString().trim()));
        body.put("text", etText.getText().toString().trim());
        body.put("link", etLink.getText().toString().trim());
        api.setBannerContent("Bearer " + token, body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { finish(); }
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
