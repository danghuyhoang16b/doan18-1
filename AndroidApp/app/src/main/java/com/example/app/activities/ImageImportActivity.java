package com.example.app.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.models.ImportImageRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageImportActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView tvStatus, tvLog;
    private Button btnStartImport;
    private List<ImageFile> imageList = new ArrayList<>();
    private StringBuilder logBuilder = new StringBuilder();
    private boolean isImporting = false;

    private static class ImageFile {
        Uri uri;
        String name;
        long size;
        String mimeType;

        ImageFile(Uri uri, String name, long size, String mimeType) {
            this.uri = uri;
            this.name = name;
            this.size = size;
            this.mimeType = mimeType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_import);

        tvStatus = findViewById(R.id.tvStatus);
        tvLog = findViewById(R.id.tvLog);
        btnStartImport = findViewById(R.id.btnStartImport);

        btnStartImport.setOnClickListener(v -> startImportProcess());

        checkPermissionsAndScan();
    }

    private void checkPermissionsAndScan() {
        String permission = Build.VERSION.SDK_INT >= 33 ? 
                Manifest.permission.READ_MEDIA_IMAGES : 
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            scanDownloads();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanDownloads();
            } else {
                tvStatus.setText("Quyền truy cập bị từ chối. Không thể quét ảnh.");
                log("Permission denied.");
            }
        }
    }

    private void scanDownloads() {
        tvStatus.setText("Đang quét thư mục Download...");
        imageList.clear();
        
        ContentResolver collection = getContentResolver();
        Uri queryUri = Build.VERSION.SDK_INT >= 29 ?
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) :
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                Build.VERSION.SDK_INT >= 29 ? MediaStore.Images.Media.RELATIVE_PATH : MediaStore.Images.Media.DATA
        };

        // Filter for Download folder
        String selection = null;
        String[] selectionArgs = null;
        if (Build.VERSION.SDK_INT >= 29) {
            selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
            selectionArgs = new String[]{"%Download%"};
        } else {
            selection = MediaStore.Images.Media.DATA + " LIKE ?";
            selectionArgs = new String[]{"%/Download/%"};
        }

        try (Cursor cursor = collection.query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    long size = cursor.getLong(sizeColumn);
                    String mime = cursor.getString(mimeColumn);

                    // Filter extensions if needed (MIME type is better)
                    if (mime != null && (mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/webp"))) {
                        Uri contentUri = ContentUris.withAppendedId(queryUri, id);
                        imageList.add(new ImageFile(contentUri, name, size, mime));
                    }
                }
            }
        } catch (Exception e) {
            log("Lỗi khi quét: " + e.getMessage());
        }

        tvStatus.setText("Tìm thấy " + imageList.size() + " ảnh hợp lệ trong Download.");
        log("Đã tìm thấy " + imageList.size() + " ảnh.");
        if (!imageList.isEmpty()) {
            btnStartImport.setEnabled(true);
        }
    }

    private void startImportProcess() {
        if (isImporting) return;
        isImporting = true;
        btnStartImport.setEnabled(false);
        processNextImage(0, 0, 0);
    }

    private void processNextImage(int index, int success, int fail) {
        if (index >= imageList.size()) {
            isImporting = false;
            tvStatus.setText("Hoàn tất! Thành công: " + success + ", Thất bại: " + fail);
            log("=== HOÀN TẤT ===");
            Toast.makeText(this, "Import hoàn tất", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageFile img = imageList.get(index);
        log("Đang xử lý (" + (index + 1) + "/" + imageList.size() + "): " + img.name);

        try {
            // Read file to Base64
            InputStream is = getContentResolver().openInputStream(img.uri);
            byte[] bytes = getBytes(is);
            String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
            
            String token = SharedPrefsUtils.getToken(this);
            ImportImageRequest request = new ImportImageRequest(img.name, img.size, img.mimeType, base64, token);
            
            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            api.importImage("Bearer " + token, request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        log("-> Upload OK");
                        processNextImage(index + 1, success + 1, fail);
                    } else {
                        log("-> Lỗi server: " + response.code());
                        processNextImage(index + 1, success, fail + 1);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    log("-> Lỗi kết nối: " + t.getMessage());
                    processNextImage(index + 1, success, fail + 1);
                }
            });

        } catch (Exception e) {
            log("-> Lỗi đọc file: " + e.getMessage());
            processNextImage(index + 1, success, fail + 1);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void log(String msg) {
        logBuilder.append(msg).append("\n");
        tvLog.setText(logBuilder.toString());
    }
}
