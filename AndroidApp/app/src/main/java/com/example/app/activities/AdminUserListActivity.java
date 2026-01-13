package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.AdminUserAdapter;
import com.example.app.api.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.models.User;
import com.example.app.models.UserListResponse;
import com.example.app.utils.SharedPrefsUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserListActivity extends AppCompatActivity {

    private RecyclerView rvUserList;
    private AdminUserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private EditText etSearch;
    private ImageButton btnSearchAction;
    private FloatingActionButton fabAdd;
    private android.widget.TextView tvNoData;
    
    private String userType; // student, teacher, parent
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) userType = "student";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = "Quản lý ";
            if ("student".equals(userType)) title += "Học sinh";
            else if ("teacher".equals(userType)) title += "Giáo viên";
            else if ("parent".equals(userType)) title += "Phụ huynh";
            else title += userType;
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvUserList = findViewById(R.id.rvUserList);
        etSearch = findViewById(R.id.etSearch);
        btnSearchAction = findViewById(R.id.btnSearchAction);
        fabAdd = findViewById(R.id.fabAdd);
        tvNoData = findViewById(R.id.tvNoData);

        setupRecyclerView();
        
        btnSearchAction.setOnClickListener(v -> {
            currentSearch = etSearch.getText().toString().trim();
            fetchUsers(true);
        });

        // Optional: Search on type
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Debounce could be added here
            }
        });
        
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AdminUserListActivity.this, AdminUserEditActivity.class);
            intent.putExtra("default_role", userType);
            startActivity(intent);
        });

        fetchUsers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUsers(true);
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this, userList, new AdminUserAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(User user) {
                Intent intent = new Intent(AdminUserListActivity.this, AdminUserEditActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(User user) {
                Toast.makeText(AdminUserListActivity.this, "Xóa: " + user.getFullName(), Toast.LENGTH_SHORT).show();
                // Show confirmation dialog then call API
            }

            @Override
            public void onItemClick(User user) {
                Toast.makeText(AdminUserListActivity.this, "Chi tiết: " + user.getFullName(), Toast.LENGTH_SHORT).show();
            }
        });
        
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        rvUserList.setAdapter(adapter);
        
        rvUserList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == userList.size() - 1) {
                    if (currentPage < totalPages) {
                        currentPage++;
                        fetchUsers(false);
                    }
                }
            }
        });
    }

    private void fetchUsers(boolean refresh) {
        if (refresh) {
            currentPage = 1;
            userList.clear();
            adapter.notifyDataSetChanged();
        }
        
        isLoading = true;
        String token = SharedPrefsUtils.getToken(this);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        
        Map<String, Object> body = new HashMap<>();
        body.put("role", userType);
        body.put("token", token); // Send token in body as failsafe
        body.put("page", currentPage);
        body.put("limit", 20);
        if (!currentSearch.isEmpty()) {
            body.put("search", currentSearch);
        }

        apiService.getUsers("Bearer " + token, body).enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    UserListResponse data = response.body();
                    List<User> newUsers = data.getData();
                    if (data.getPagination() != null) {
                        totalPages = data.getPagination().getTotalPages();
                    }
                    
                    if (refresh) {
                        userList.clear();
                        userList.addAll(newUsers);
                        adapter.setUsers(userList);
                    } else {
                        int start = userList.size();
                        userList.addAll(newUsers);
                        adapter.notifyItemRangeInserted(start, newUsers.size());
                    }
                    
                    if (userList.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                        rvUserList.setVisibility(View.GONE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                        rvUserList.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(AdminUserListActivity.this, "Phiên đăng nhập hết hạn hoặc không có quyền truy cập", Toast.LENGTH_LONG).show();
                        SharedPrefsUtils.logout(AdminUserListActivity.this);
                        Intent intent = new Intent(AdminUserListActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    String errorMsg = "Lỗi tải dữ liệu: " + response.code();
                    try {
                        errorMsg += "\nURL: " + response.raw().request().url();
                    } catch (Exception e) {}
                    Toast.makeText(AdminUserListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    try {
                        if (response.errorBody() != null) {
                             android.util.Log.e("AdminUserList", "Error: " + response.errorBody().string());
                        }
                    } catch (Exception e) {}
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                isLoading = false;
                Toast.makeText(AdminUserListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
