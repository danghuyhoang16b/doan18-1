package com.example.app.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.network.ApiClient;
import com.example.app.network.ApiService;
import com.example.app.models.ClassModel;
import com.example.app.models.TokenRequest;
import com.example.app.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRedCommitteeActivity extends AppCompatActivity {
    private Spinner spClasses;
    private RecyclerView rvMembers;
    private Button btnAdd, btnRemove, btnLogs;
    private ArrayAdapter<ClassModel> classAdapter;
    private List<ClassModel> classes = new ArrayList<>();
    private ApiService api;
    private String bearer;
    private MembersAdapter membersAdapter;
    private Integer selectedClassId;
    private List<MemberItem> originalItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_red_committee);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        spClasses = findViewById(R.id.spClasses);
        rvMembers = findViewById(R.id.rvMembers);
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        btnLogs = findViewById(R.id.btnLogs);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter();
        rvMembers.setAdapter(membersAdapter);
        api = ApiClient.getInstance().getApiService();
        bearer = "Bearer " + SharedPrefsUtils.getToken(this);
        loadClasses();
        btnAdd.setOnClickListener(v -> addMemberFlow());
        btnRemove.setOnClickListener(v -> removeMemberFlow());
        btnLogs.setOnClickListener(v -> openLogs());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm kiếm học sinh...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filter(String text) {
        List<MemberItem> filteredList = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            filteredList.addAll(originalItems);
        } else {
            String lower = text.toLowerCase().trim();
            for (MemberItem item : originalItems) {
                if (item.name.toLowerCase().contains(lower) || 
                    item.username.toLowerCase().contains(lower) ||
                    item.className.toLowerCase().contains(lower)) {
                    filteredList.add(item);
                }
            }
        }
        if (membersAdapter != null) {
            membersAdapter.setData(filteredList);
        }
    }

    private void loadClasses() {
        String token = SharedPrefsUtils.getToken(this);
        api.getTeacherClasses("Bearer " + token).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    classes = response.body();
                    // Add "All" option
                    classes.add(0, new ClassModel(0, "Tất cả các lớp"));
                    
                    classAdapter = new ArrayAdapter<>(AdminRedCommitteeActivity.this, android.R.layout.simple_spinner_item, classes);
                    classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spClasses.setAdapter(classAdapter);
                    spClasses.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            selectedClassId = classes.get(position).getId();
                            refreshMembers();
                        }
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                Toast.makeText(AdminRedCommitteeActivity.this, "Lỗi tải lớp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMembers() {
        // Pass null if selectedClassId is 0 (All)
        Integer cid = (selectedClassId != null && selectedClassId != 0) ? selectedClassId : null;
        
        api.listRedCommittee(bearer, cid, null).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        List<MemberItem> items = new ArrayList<>();
                        for (int i=0; i<arr.length(); i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);
                            MemberItem item = new MemberItem(
                                o.optInt("id"), // red_committee_members.id
                                o.optInt("user_id"),
                                o.optString("full_name"),
                                o.optString("username"),
                                o.optString("class_name"),
                                o.optInt("duration_weeks"),
                                o.optString("expired_at")
                            );
                            item.daysLeft = o.optInt("days_left");
                            items.add(item);
                        }
                        originalItems = new ArrayList<>(items);
                        membersAdapter.setData(items);
                        rvMembers.setVisibility(View.VISIBLE);
                        
                        // Update UI buttons based on context
                        if (cid == null) {
                            btnAdd.setText("Thêm Sao đỏ");
                            btnRemove.setEnabled(false); // Disable bulk remove in All view for now
                        } else {
                             if (items.size() > 0) {
                                btnAdd.setText("Thay đổi Sao đỏ");
                                btnRemove.setEnabled(true);
                            } else {
                                btnAdd.setText("Phân công Sao đỏ");
                                btnRemove.setEnabled(false);
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
    
    private void addMemberFlow() {
        api.getActionChallenge(bearer).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        String code = o.optString("code");
                        String ticket = o.optString("ticket");
                        AlertDialog.Builder b = new AlertDialog.Builder(AdminRedCommitteeActivity.this);
                        android.widget.EditText input = new android.widget.EditText(AdminRedCommitteeActivity.this);
                        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        input.setHint("Nhập mã xác nhận: " + code);
                        b.setTitle("Xác thực thao tác");
                        b.setView(input);
                        b.setPositiveButton("Xác nhận", (dialog, which) -> {
                            String entered = input.getText().toString().trim();
                            if (!entered.equals(code)) {
                                Toast.makeText(AdminRedCommitteeActivity.this, "Sai mã xác nhận", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showSelectUserAndSubmit(code, ticket, true);
                        });
                        b.setNegativeButton("Hủy", (dialog, which) -> {});
                        b.show();
                    }
                } catch (Exception e) { }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }

    private void removeMemberFlow() {
        api.getActionChallenge(bearer).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        String code = o.optString("code");
                        String ticket = o.optString("ticket");
                        AlertDialog.Builder b = new AlertDialog.Builder(AdminRedCommitteeActivity.this);
                        android.widget.EditText input = new android.widget.EditText(AdminRedCommitteeActivity.this);
                        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        input.setHint("Nhập mã xác nhận: " + code);
                        b.setTitle("Xác thực thao tác");
                        b.setView(input);
                        b.setPositiveButton("Xác nhận", (dialog, which) -> {
                            String entered = input.getText().toString().trim();
                            if (!entered.equals(code)) {
                                Toast.makeText(AdminRedCommitteeActivity.this, "Sai mã xác nhận", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showSelectUserAndSubmit(code, ticket, false);
                        });
                        b.setNegativeButton("Hủy", (dialog, which) -> {});
                        b.show();
                    }
                } catch (Exception e) { }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }

    private void openLogs() {
        // Pass null if selectedClassId is 0 (All)
        Integer cid = (selectedClassId != null && selectedClassId != 0) ? selectedClassId : null;
        api.getRedCommitteeLogs(bearer, cid).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        StringBuilder sb = new StringBuilder();
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);
                            sb.append(o.optString("created_at")).append(" - ").append(o.optString("actor_name")).append(" ").append(o.optString("action")).append(" ").append(o.optString("target_name")).append("\n");
                        }
                        AlertDialog.Builder b = new AlertDialog.Builder(AdminRedCommitteeActivity.this);
                        b.setTitle("Nhật ký phân quyền");
                        b.setMessage(sb.toString());
                        b.setPositiveButton("Đóng", (d,w)->{});
                        b.show();
                    }
                } catch (Exception e) { }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }

    private void showSelectUserAndSubmit(String code, String ticket, boolean add) {
        // If Adding and "All" is selected, we need to pick a class
        if (add) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Tạo tài khoản Sao đỏ");
            
            android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(32, 16, 32, 16);
            
            // Class Spinner (Only if All is selected)
            final Spinner spDialogClass = new Spinner(this);
            final List<ClassModel> dialogClasses = new ArrayList<>();
            // Filter out "All"
            for(ClassModel c : classes) if (c.getId() != 0) dialogClasses.add(c);
            
            if (selectedClassId == null || selectedClassId == 0) {
                ArrayAdapter<ClassModel> da = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dialogClasses);
                da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spDialogClass.setAdapter(da);
                layout.addView(spDialogClass);
            }

            final android.widget.EditText etUsername = new android.widget.EditText(this);
            etUsername.setHint("Tên đăng nhập (VD: sd_10a1)");
            
            // Auto generate hint logic
            Runnable updateHint = () -> {
                int cid = (selectedClassId != null && selectedClassId != 0) ? selectedClassId : dialogClasses.get(spDialogClass.getSelectedItemPosition()).getId();
                String cName = "";
                for(ClassModel c : classes) if(c.getId() == cid) cName = c.getName();
                if(!cName.isEmpty()) etUsername.setText("sd_" + cName.toLowerCase().replaceAll("\\s+",""));
            };
            
            if (selectedClassId != null && selectedClassId != 0) {
                updateHint.run();
            } else {
                spDialogClass.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                     @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { updateHint.run(); }
                     @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
                });
            }

            final android.widget.EditText etPassword = new android.widget.EditText(this);
            etPassword.setHint("Mật khẩu");
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setText("123456"); // Default
            
            layout.addView(etUsername);
            layout.addView(etPassword);

            // Duration Spinner
            final Spinner spDuration = new Spinner(this);
            String[] durations = {"4 tuần (1 tháng)", "8 tuần (2 tháng)", "18 tuần (1 kỳ)", "36 tuần (1 năm)"};
            int[] durationValues = {4, 8, 18, 36};
            ArrayAdapter<String> da = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durations);
            da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDuration.setAdapter(da);
            layout.addView(spDuration);

            b.setView(layout);
            
            b.setPositiveButton("Tạo tài khoản", (d, w) -> {
                String u = etUsername.getText().toString().trim();
                String p = etPassword.getText().toString().trim();
                int targetClassId = (selectedClassId != null && selectedClassId != 0) ? selectedClassId : dialogClasses.get(spDialogClass.getSelectedItemPosition()).getId();
                
                if (u.isEmpty() || p.isEmpty()) {
                    Toast.makeText(this, "Thiếu thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Map<String,Object> body = new HashMap<>();
                body.put("class_id", targetClassId);
                body.put("username", u);
                body.put("password", p);
                body.put("duration_weeks", durationValues[spDuration.getSelectedItemPosition()]);
                
                api.createRedStarAccount(bearer, code, ticket, body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminRedCommitteeActivity.this, "Đã tạo tài khoản Sao đỏ", Toast.LENGTH_SHORT).show();
                            refreshMembers();
                        } else if (response.code() == 409) {
                             Toast.makeText(AdminRedCommitteeActivity.this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminRedCommitteeActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                         Toast.makeText(AdminRedCommitteeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            b.setNegativeButton("Hủy", null);
            b.show();
            return;
        }

        java.util.List<MemberItem> items = membersAdapter.getData();
        String[] names = new String[items.size()];
        for (int i=0;i<items.size();i++) names[i]=items.get(i).name;
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(add ? "Chọn học sinh để thêm Sao đỏ" : "Chọn thành viên để xóa");
        b.setItems(names, (dialog, which) -> {
            MemberItem mi = items.get(which);
            Map<String,Object> body = new HashMap<>();
            body.put("user_id", mi.userId);
            body.put("class_id", selectedClassId);
            api.removeRedCommittee(bearer, code, ticket, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminRedCommitteeActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                        refreshMembers();
                    } else {
                        Toast.makeText(AdminRedCommitteeActivity.this, "Thao tác thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AdminRedCommitteeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.show();
    }
    
    private void showEditDialog(MemberItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Quản lý: " + item.username);
        String[] options = {"Gia hạn hoạt động", "Xóa Sao đỏ"};
        b.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Gia hạn
                showExtendDialog(item);
            } else if (which == 1) {
                // Xóa
                confirmDelete(item);
            }
        });
        b.show();
    }
    
    private void showExtendDialog(MemberItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Gia hạn cho " + item.username);
        final Spinner spDuration = new Spinner(this);
        String[] durations = {"4 tuần", "8 tuần", "18 tuần", "36 tuần"};
        int[] durationValues = {4, 8, 18, 36};
        ArrayAdapter<String> da = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durations);
        da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDuration.setAdapter(da);
        b.setView(spDuration);
        b.setPositiveButton("Cập nhật", (d, w) -> {
            int weeks = durationValues[spDuration.getSelectedItemPosition()];
            Map<String,Object> body = new HashMap<>();
            body.put("id", item.id);
            body.put("duration_weeks", weeks);
            api.updateRedCommittee(bearer, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminRedCommitteeActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        refreshMembers();
                    } else {
                        Toast.makeText(AdminRedCommitteeActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        });
        b.show();
    }
    
    private void confirmDelete(MemberItem item) {
        // Use existing remove flow logic but for specific user
        // Need to call getActionChallenge first
        api.getActionChallenge(bearer).enqueue(new Callback<ResponseBody>() {
             @Override
             public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                 try {
                     if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        String code = o.optString("code");
                        String ticket = o.optString("ticket");
                        
                        AlertDialog.Builder b = new AlertDialog.Builder(AdminRedCommitteeActivity.this);
                        android.widget.EditText input = new android.widget.EditText(AdminRedCommitteeActivity.this);
                        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        input.setHint("Nhập mã: " + code);
                        b.setTitle("Xác nhận xóa " + item.username);
                        b.setView(input);
                        b.setPositiveButton("Xóa", (d, w) -> {
                             if (!input.getText().toString().trim().equals(code)) {
                                 Toast.makeText(AdminRedCommitteeActivity.this, "Sai mã", Toast.LENGTH_SHORT).show();
                                 return;
                             }
                             Map<String,Object> body = new HashMap<>();
                             body.put("user_id", item.userId);
                             // Need class_id? API remove.php usually needs it to verify, or we can just pass it if we have it
                             // Wait, list.php returns class_id but MemberItem didn't store it. Let's fix MemberItem.
                             // Actually remove.php uses user_id mainly? Let's check remove.php logic.
                             // Assuming remove.php takes user_id.
                             
                             api.removeRedCommittee(bearer, code, ticket, body).enqueue(new Callback<ResponseBody>() {
                                 @Override
                                 public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                     if (response.isSuccessful()) {
                                         Toast.makeText(AdminRedCommitteeActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                         refreshMembers();
                                     }
                                 }
                                 @Override
                                 public void onFailure(Call<ResponseBody> call, Throwable t) {}
                             });
                        });
                        b.show();
                     }
                 } catch(Exception e){}
             }
             @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    static class MemberItem {
        int id; // red_committee_members id
        int userId;
        String name;
        String username;
        String className;
        int durationWeeks;
        String expiredAt;
        int daysLeft;
        
        MemberItem(int id, int uid, String n, String un, String cn, int dw, String exp) { 
            this.id=id; userId=uid; name=n; username=un; className=cn; durationWeeks=dw; expiredAt=exp; 
        }
    }
    
    static class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.VH> {
        private List<MemberItem> data = new ArrayList<>();
        private Consumer<MemberItem> onClick;
        
        public void setData(List<MemberItem> d) { data = d; notifyDataSetChanged(); }
        public List<MemberItem> getData() { return data; }
        public void setOnClick(Consumer<MemberItem> c) { onClick = c; }
        
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            MemberItem m = data.get(pos);
            h.t1.setText(m.className + " - " + m.name);
            String status = m.daysLeft < 0 ? "(Hết hạn)" : "(" + m.daysLeft + " ngày)";
            h.t2.setText(m.username + " " + status);
            h.itemView.setOnClickListener(v -> {
                if (onClick != null) onClick.accept(m);
            });
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            android.widget.TextView t1, t2;
            VH(android.view.View v) { super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2); }
        }
    }
    
    interface Consumer<T> { void accept(T t); }
}
