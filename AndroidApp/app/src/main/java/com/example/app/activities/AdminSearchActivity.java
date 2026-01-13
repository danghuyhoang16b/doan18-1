package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.app.R;

public class AdminSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý hệ thống");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CardView cardStudent = findViewById(R.id.cardStudent);
        CardView cardTeacher = findViewById(R.id.cardTeacher);
        CardView cardParent = findViewById(R.id.cardParent);
        CardView cardClass = findViewById(R.id.cardClass);

        cardStudent.setOnClickListener(v -> openUserList("student"));
        cardTeacher.setOnClickListener(v -> openUserList("teacher"));
        cardParent.setOnClickListener(v -> openUserList("parent"));
        cardClass.setOnClickListener(v -> {
            // Placeholder or link to existing Class Management
            // For now, let's just show a toast or open a placeholder
            android.widget.Toast.makeText(this, "Chức năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show();
        });

        // Thêm xử lý cho nút Sao đỏ
        CardView cardRedStar = findViewById(R.id.cardRedStar);
        if (cardRedStar != null) {
            cardRedStar.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminRedCommitteeActivity.class);
                startActivity(intent);
            });
        }
    }

    private void openUserList(String userType) {
        Intent intent = new Intent(this, AdminUserListActivity.class);
        intent.putExtra("USER_TYPE", userType);
        startActivity(intent);
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