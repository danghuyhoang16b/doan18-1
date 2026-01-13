package com.example.app.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherStudentManagerActivity extends AppCompatActivity {

    private Spinner spClasses;
    private RecyclerView rvStudents;
    private EditText etSearch;
    private List<StudentItem> originalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_student_manager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Học sinh & Phụ huynh");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spClasses = findViewById(R.id.spClasses);
        rvStudents = findViewById(R.id.rvStudents);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        etSearch = findViewById(R.id.etSearch);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        api = RetrofitClient.getClient().create(ApiService.class);
        token = SharedPrefsUtils.getToken(this);
        bearer = "Bearer " + token;

        loadClasses();

        btnAddStudent.setOnClickListener(v -> addStudentFlow());
        
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                filterStudents(s.toString());
            }
        });
    }

    private void filterStudents(String query) {
        if (adapter == null) return;
        List<StudentItem> filtered = new ArrayList<>();
        for (StudentItem item : originalList) {
            if (com.example.app.utils.StringHelper.containsIgnoreCase(item.name, query) ||
                com.example.app.utils.StringHelper.containsIgnoreCase(item.code, query)) {
                filtered.add(item);
            }
        }
        adapter.setData(filtered);
    }


    private Button btnAddStudent;
    private ApiService api;
    private String token;
    private String bearer;
    private List<ClassModel> classes = new ArrayList<>();
    private StudentParentAdapter adapter;
    private Integer selectedClassId;
    
    // ... filterStudents method ...

    private void loadClasses() {
        api.getTeacherClasses(new TokenRequest(token)).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    classes = response.body();
                    if (classes.isEmpty()) {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Bạn chưa được phân công lớp nào", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ArrayAdapter<ClassModel> adapter = new ArrayAdapter<>(TeacherStudentManagerActivity.this, android.R.layout.simple_spinner_item, classes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spClasses.setAdapter(adapter);

                    spClasses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedClassId = classes.get(position).getId();
                            loadStudents(selectedClassId);
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi tải danh sách lớp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudents(int classId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("class_id", classId);
        
        api.listStudentsByClass(bearer, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONArray arr = new JSONArray(json);
                        List<StudentItem> items = new ArrayList<>();
                        
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            items.add(new StudentItem(
                                o.optInt("id"),
                                o.optString("full_name"),
                                o.optString("code"),
                                o.optString("parent_name", "Chưa cập nhật"),
                                o.optString("parent_phone", "")
                            ));
                        }
                        
                        originalList = new ArrayList<>(items);
                        adapter = new StudentParentAdapter(items);
                        adapter.setOnItemClickListener(item -> showStudentOptions(item));
                        rvStudents.setAdapter(adapter);
                        
                        // Re-apply filter if any
                        if (etSearch.getText().length() > 0) {
                            filterStudents(etSearch.getText().toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStudentFlow() {
        if (selectedClassId == null) {
            Toast.makeText(this, "Vui lòng chọn lớp trước", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Thêm học sinh mới");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText etName = new EditText(this); 
        etName.setHint("Họ tên học sinh"); 
        etName.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        layout.addView(etName);
        
        EditText etCode = new EditText(this); 
        etCode.setHint("Mã học sinh (Username)"); 
        layout.addView(etCode);
        
        EditText etParent = new EditText(this); 
        etParent.setHint("Tên phụ huynh"); 
        etParent.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        layout.addView(etParent);
        
        EditText etPhone = new EditText(this); 
        etPhone.setHint("SĐT phụ huynh"); 
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        b.setView(layout);
        b.setPositiveButton("Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String code = etCode.getText().toString().trim();
            String parent = etParent.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Tên và Mã HS không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("class_id", selectedClassId);
            body.put("full_name", name);
            body.put("username", code);
            body.put("password", "123456"); // Default password
            body.put("parent_name", parent);
            body.put("parent_phone", phone);

            api.addStudentToClass(bearer, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        loadStudents(selectedClassId);
                    } else if (response.code() == 409) {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Mã học sinh đã tồn tại", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi thêm: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.setNegativeButton("Hủy", null);
        b.show();
    }

    private void showStudentOptions(StudentItem item) {
        String[] options = {"Sửa thông tin", "Xóa học sinh"};
        new AlertDialog.Builder(this)
            .setTitle(item.name)
            .setItems(options, (dialog, which) -> {
                if (which == 0) editStudentFlow(item);
                else deleteStudentFlow(item);
            })
            .show();
    }

    private void editStudentFlow(StudentItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Sửa thông tin: " + item.name);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText etName = new EditText(this); 
        etName.setHint("Họ tên học sinh"); 
        etName.setText(item.name); 
        etName.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        layout.addView(etName);
        
        EditText etParent = new EditText(this); 
        etParent.setHint("Tên phụ huynh"); 
        etParent.setText(item.parentName); 
        etParent.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        layout.addView(etParent);
        
        EditText etPhone = new EditText(this); 
        etPhone.setHint("SĐT phụ huynh"); 
        etPhone.setText(item.parentPhone); 
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        b.setView(layout);
        b.setPositiveButton("Lưu", (dialog, which) -> {
            Map<String, Object> body = new HashMap<>();
            body.put("id", item.id);
            body.put("class_id", selectedClassId);
            body.put("full_name", etName.getText().toString().trim());
            body.put("parent_name", etParent.getText().toString().trim());
            body.put("parent_phone", etPhone.getText().toString().trim());

            api.updateStudentInClass(bearer, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        loadStudents(selectedClassId);
                    } else {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.setNegativeButton("Hủy", null);
        b.show();
    }

    private void deleteStudentFlow(StudentItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa học sinh " + item.name + " và toàn bộ dữ liệu liên quan?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                Map<String, Object> body = new HashMap<>();
                body.put("id", item.id);
                body.put("class_id", selectedClassId);
                
                api.removeStudentFromClass(bearer, body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(TeacherStudentManagerActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                            loadStudents(selectedClassId);
                        } else {
                            Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(TeacherStudentManagerActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    interface OnItemClickListener {
        void onItemClick(StudentItem item);
    }

    static class StudentItem {
        int id;
        String name, code, parentName, parentPhone;
        StudentItem(int id, String n, String c, String pn, String pp) {
            this.id = id; name = n; code = c; parentName = pn; parentPhone = pp;
        }
    }

    static class StudentParentAdapter extends RecyclerView.Adapter<StudentParentAdapter.VH> {
        List<StudentItem> list;
        OnItemClickListener listener;
        
        StudentParentAdapter(List<StudentItem> l) { list = l; }
        public void setOnItemClickListener(OnItemClickListener l) { listener = l; }
        public void setData(List<StudentItem> l) { list = l; notifyDataSetChanged(); }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_parent, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            StudentItem item = list.get(position);
            holder.tvName.setText(item.name);
            holder.tvCode.setText("MSHS: " + item.code);
            holder.tvParent.setText(item.parentName != null && !item.parentName.isEmpty() && !"null".equals(item.parentName) ? item.parentName : "Chưa cập nhật");
            holder.tvPhone.setText(item.parentPhone != null && !item.parentPhone.isEmpty() && !"null".equals(item.parentPhone) ? item.parentPhone : "---");
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvCode, tvParent, tvPhone;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvStudentName);
                tvCode = v.findViewById(R.id.tvStudentCode);
                tvParent = v.findViewById(R.id.tvParentName);
                tvPhone = v.findViewById(R.id.tvParentPhone);
            }
        }
    }
}