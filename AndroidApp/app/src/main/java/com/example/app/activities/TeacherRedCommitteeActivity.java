package com.example.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.api.ApiService;
import com.example.app.models.ClassModel;
import com.example.app.models.TokenRequest;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherRedCommitteeActivity extends AppCompatActivity {
    private Spinner spClasses;
    private RecyclerView rvMembers;
    private Button btnAdd;
    private ArrayAdapter<ClassModel> classAdapter;
    private List<ClassModel> classes = new ArrayList<>();
    private ApiService api;
    private String bearer;
    private MembersAdapter membersAdapter;
    private Integer selectedClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_red_committee);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Sao đỏ");
        }
        spClasses = findViewById(R.id.spClasses);
        rvMembers = findViewById(R.id.rvMembers);
        btnAdd = findViewById(R.id.btnAdd);
        // btnRemove removed, integrated into item click
        findViewById(R.id.btnRemove).setVisibility(View.GONE);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter();
        membersAdapter.setOnItemClickListener(this::showMemberOptions);
        rvMembers.setAdapter(membersAdapter);
        
        api = RetrofitClient.getClient().create(ApiService.class);
        bearer = "Bearer " + SharedPrefsUtils.getToken(this);
        
        loadClasses();
        btnAdd.setOnClickListener(v -> addMemberFlow());
    }

    private void loadClasses() {
        String token = SharedPrefsUtils.getToken(this);
        api.getTeacherClasses("Bearer " + token).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    classes = response.body();
                    classAdapter = new ArrayAdapter<>(TeacherRedCommitteeActivity.this, android.R.layout.simple_spinner_item, classes);
                    classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spClasses.setAdapter(classAdapter);
                    spClasses.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            selectedClassId = classes.get(position).getId();
                            refreshMembers();
                        }
                        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                    });
                }
            }
            @Override public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                Toast.makeText(TeacherRedCommitteeActivity.this, "Lỗi tải lớp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMembers() {
        api.listRedCommittee(bearer, selectedClassId, null).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONArray arr = new org.json.JSONArray(s);
                        List<MemberItem> items = new ArrayList<>();
                        for (int i=0;i<arr.length();i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);
                            items.add(new MemberItem(
                                o.optInt("id"), // red_committee_members.id
                                o.optInt("user_id"), 
                                o.optString("full_name"), 
                                o.optString("username"),
                                o.optString("status_text", ""),
                                o.optString("status_color", "#000000")
                            ));
                        }
                        membersAdapter.setData(items);
                    }
                } catch (Exception e) { }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }

    private void showMemberOptions(MemberItem item) {
        String[] options = {"Gia hạn / Sửa đổi", "Xóa thành viên"};
        new AlertDialog.Builder(this)
            .setTitle(item.name)
            .setItems(options, (dialog, which) -> {
                if (which == 0) editMemberFlow(item);
                else removeMemberFlow(item);
            })
            .show();
    }

    private void editMemberFlow(MemberItem item) {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setTitle("Gia hạn hiệu lực");
        String[] options = {"4 tuần (1 tháng)", "8 tuần (2 tháng)", "18 tuần (1 kỳ)", "36 tuần (1 năm)"};
        int[] values = {4, 8, 18, 36};
        db.setItems(options, (dd, ww) -> {
            Map<String, Object> body = new HashMap<>();
            body.put("id", item.id);
            body.put("duration_weeks", values[ww]);
            
            api.updateRedCommittee(bearer, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TeacherRedCommitteeActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        refreshMembers();
                    } else {
                        Toast.makeText(TeacherRedCommitteeActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(TeacherRedCommitteeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        db.show();
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
                        showSecurityCheck(code, ticket, true, null);
                    }
                } catch (Exception e) { }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }

    private void removeMemberFlow(MemberItem item) {
        api.getActionChallenge(bearer).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body()!=null) {
                        String s = response.body().string();
                        org.json.JSONObject o = new org.json.JSONObject(s);
                        String code = o.optString("code");
                        String ticket = o.optString("ticket");
                        showSecurityCheck(code, ticket, false, item);
                    }
                } catch (Exception e) { }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }

    private void showSecurityCheck(String code, String ticket, boolean add, MemberItem itemToRemove) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập mã: " + code);
        b.setTitle("Xác thực thao tác");
        b.setView(input);
        b.setPositiveButton("Xác nhận", (dialog, which) -> {
            String entered = input.getText().toString().trim();
            if (!entered.equals(code)) {
                Toast.makeText(TeacherRedCommitteeActivity.this, "Sai mã xác nhận", Toast.LENGTH_SHORT).show();
                return;
            }
            if (add) showSelectUserAndSubmit(code, ticket);
            else submitRemove(code, ticket, itemToRemove);
        });
        b.setNegativeButton("Hủy", null);
        b.show();
    }

    private void showSelectUserAndSubmit(String code, String ticket) {
        // New Flow: Create Red Star Account (Same as Admin)
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Tạo tài khoản Sao đỏ");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        
        final android.widget.EditText etUsername = new android.widget.EditText(this);
        etUsername.setHint("Tên đăng nhập (VD: sd_10a1)");
        
        // Auto generate hint based on selected class name
        String className = "";
        for(ClassModel c : classes) if(c.getId() == selectedClassId) className = c.getName();
        if(!className.isEmpty()) etUsername.setText("sd_" + className.toLowerCase().replaceAll("\\s+",""));
        
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
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Thiếu thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Map<String,Object> body = new HashMap<>();
            body.put("class_id", selectedClassId);
            body.put("username", u);
            body.put("password", p);
            body.put("duration_weeks", durationValues[spDuration.getSelectedItemPosition()]);
            
            api.createRedStarAccount(bearer, code, ticket, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TeacherRedCommitteeActivity.this, "Đã tạo tài khoản Sao đỏ", Toast.LENGTH_SHORT).show();
                        refreshMembers();
                    } else if (response.code() == 409) {
                         Toast.makeText(TeacherRedCommitteeActivity.this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TeacherRedCommitteeActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                     Toast.makeText(TeacherRedCommitteeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.setNegativeButton("Hủy", null);
        b.show();
    }

    private void submitRemove(String code, String ticket, MemberItem item) {
        Map<String,Object> body = new HashMap<>();
        body.put("user_id", item.userId);
        body.put("class_id", selectedClassId);
        api.removeRedCommittee(bearer, code, ticket, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherRedCommitteeActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                    refreshMembers();
                } else {
                    Toast.makeText(TeacherRedCommitteeActivity.this, "Lỗi xóa", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    interface OnItemClickListener {
        void onItemClick(MemberItem item);
    }

    static class MemberItem {
        int id; // Record ID
        int userId;
        String name;
        String username;
        String statusText;
        String statusColor;
        
        MemberItem(int id, int uid, String n, String un, String st, String sc) { 
            this.id=id; userId=uid; name=n; username=un; statusText=st; statusColor=sc; 
        }
    }
    static class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.VH> {
        private List<MemberItem> data = new ArrayList<>();
        private OnItemClickListener listener;
        
        public void setOnItemClickListener(OnItemClickListener l) { listener = l; }
        public void setData(List<MemberItem> d) { data = d; notifyDataSetChanged(); }
        
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            MemberItem m = data.get(pos);
            h.t1.setText(m.name);
            
            String sub = m.username + " - " + m.statusText;
            android.text.SpannableString ss = new android.text.SpannableString(sub);
            try {
                int start = m.username.length() + 3;
                int end = sub.length();
                ss.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor(m.statusColor)), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception ignored) {}
            
            h.t2.setText(ss);
            h.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(m);
            });
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            android.widget.TextView t1, t2;
            VH(android.view.View v) { super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2); }
        }
    }
}