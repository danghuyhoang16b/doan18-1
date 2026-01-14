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
                new android.app.AlertDialog.Builder(AdminUserListActivity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Khóa tài khoản " + user.getFullName() + "?")
                        .setPositiveButton("Khóa", (d, w) -> {
                            String token = SharedPrefsUtils.getToken(AdminUserListActivity.this);
                            ApiService api = RetrofitClient.getClient().create(ApiService.class);
                            java.util.Map<String, Integer> body = new java.util.HashMap<>();
                            body.put("id", user.getId());
                            api.deleteUser("Bearer " + token, body).enqueue(new Callback<okhttp3.ResponseBody>() {
                                @Override public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> rsp) {
                                    String msg = "Thành công";
                                    try { if (rsp.body()!=null) { msg = new org.json.JSONObject(rsp.body().string()).optString("message", msg); } } catch(Exception ignored){}
                                    Toast.makeText(AdminUserListActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    // Remove item from list
                                    int idx = userList.indexOf(user);
                                    if (idx >= 0) { userList.remove(idx); adapter.notifyItemRemoved(idx); }
                                }
                                @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                                    Toast.makeText(AdminUserListActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
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
        if ("teacher".equals(userType)) {
            body.put("strict", true);
        }
        if (!currentSearch.isEmpty()) {
            body.put("search", currentSearch);
        }

        apiService.getUsers("Bearer " + token, body).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        org.json.JSONArray arr = o.optJSONArray("data");
                        List<User> newUsers = new ArrayList<>();
                        if (arr != null) {
                            for (int i=0;i<arr.length();i++) {
                                org.json.JSONObject u = arr.getJSONObject(i);
                                User user = new User();
                                user.setId(u.optInt("id"));
                                user.setUsername(u.optString("username"));
                                user.setFullName(u.optString("full_name"));
                                user.setRole(u.optString("role"));
                                user.setEmail(u.optString("email"));
                                user.setPhone(u.optString("phone"));
                                userList.add(user);
                                newUsers.add(user);
                            }
                        }
                        org.json.JSONObject pag = o.optJSONObject("pagination");
                        if (pag != null) totalPages = pag.optInt("total_pages", 1);
                        if (refresh) {
                            adapter.setUsers(userList);
                        } else {
                            int start = userList.size() - newUsers.size();
                            adapter.notifyItemRangeInserted(start, newUsers.size());
                        }
                        if (userList.isEmpty()) {
                            tvNoData.setVisibility(View.VISIBLE);
                            rvUserList.setVisibility(View.GONE);
                        } else {
                            tvNoData.setVisibility(View.GONE);
                            rvUserList.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(AdminUserListActivity.this, "Lỗi dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
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
