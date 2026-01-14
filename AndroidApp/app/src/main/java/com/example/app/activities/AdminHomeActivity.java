package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.example.app.models.User;
import com.example.app.utils.SharedPrefsUtils;

public class AdminHomeActivity extends AppCompatActivity {

    private TextView tvFullName, tvRole;
    private ImageView imgAvatar;
    private ImageButton btnLogout;
    private View btnManageStudents, btnManageTeachers, btnManageParents, btnSystemStats, btnManageBanners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Bind Views
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        
        btnManageStudents = findViewById(R.id.btnManageStudents);
        btnManageTeachers = findViewById(R.id.btnManageTeachers);
        btnManageParents = findViewById(R.id.btnManageParents);
        btnSystemStats = findViewById(R.id.btnSystemStats);
        btnManageBanners = findViewById(R.id.btnManageBanners);

        // Load User Data
        User user = SharedPrefsUtils.getUser(this);
        if (user != null) {
            tvFullName.setText(user.getFullName());
        }

        setupActions();
    }

    private void setupActions() {
        btnLogout.setOnClickListener(v -> finish());

        imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, AdminProfileActivity.class)));

        btnManageStudents.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserListActivity.class);
            intent.putExtra("USER_TYPE", "student");
            startActivity(intent);
        });

        btnManageTeachers.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserListActivity.class);
            intent.putExtra("USER_TYPE", "teacher");
            startActivity(intent);
        });

        btnManageParents.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserListActivity.class);
            intent.putExtra("USER_TYPE", "parent");
            startActivity(intent);
        });

        btnSystemStats.setOnClickListener(v -> startActivity(new Intent(this, AdminViolationStatsActivity.class)));
        
        if (btnManageBanners != null) {
            btnManageBanners.setOnClickListener(v -> startActivity(new Intent(this, AdminBannerActivity.class)));
        }
        android.view.View btnManageRedCommittee = findViewById(R.id.btnManageRedCommittee);
        if (btnManageRedCommittee != null) {
            btnManageRedCommittee.setOnClickListener(v -> startActivity(new Intent(this, AdminRedCommitteeActivity.class)));
        }
        
        android.view.View btnManageGrading = findViewById(R.id.btnManageGrading);
        if (btnManageGrading != null) {
            btnManageGrading.setOnClickListener(v -> startActivity(new Intent(this, AdminRankingActivity.class)));
        }
    }
}
