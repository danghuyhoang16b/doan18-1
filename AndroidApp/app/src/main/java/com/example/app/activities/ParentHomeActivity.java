package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.User;
import com.example.app.utils.SharedPrefsUtils;

import androidx.viewpager2.widget.ViewPager2;
import com.example.app.adapters.ImageSliderAdapter;
import java.util.Arrays;
import java.util.List;
import android.widget.Toast;

public class ParentHomeActivity extends AppCompatActivity {

    private TextView tvFullName, tvRole, tvEmptyChildren;
    private ImageView imgAvatar;
    private ImageButton btnLogout;
    private RecyclerView rvChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        // Bind Views
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        rvChildren = findViewById(R.id.rvChildren);
        tvEmptyChildren = findViewById(R.id.tvEmptyChildren);

        rvChildren.setLayoutManager(new LinearLayoutManager(this));

        // Load User Data
        User user = SharedPrefsUtils.getUser(this);
        if (user != null) {
            tvFullName.setText(user.getFullName());
            tvRole.setText("Phụ huynh");
        }

        btnLogout.setOnClickListener(v -> finish());

        // Mock empty state for now as API for parent's children might not be ready
        tvEmptyChildren.setVisibility(View.VISIBLE);
        rvChildren.setVisibility(View.GONE);
    }
}