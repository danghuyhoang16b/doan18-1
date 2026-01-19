package com.example.app.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.models.CompetitionStatsResponse;
import com.example.app.R;
import com.example.app.network.ApiClient;
import com.example.app.network.ApiService;
import com.example.app.utils.SharedPrefsUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRankingActivity extends AppCompatActivity {
    private TextInputEditText etStartDate, etEndDate;
    private Button btnLoadStats;
    private ProgressBar progressBar;
    private BarChart barChartClass;
    private PieChart pieChartViolations;
    private ApiService apiService;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ranking);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnLoadStats = findViewById(R.id.btnLoadStats);
        progressBar = findViewById(R.id.progressBar);
        barChartClass = findViewById(R.id.barChartClass);
        pieChartViolations = findViewById(R.id.pieChartViolations);
        apiService = ApiClient.getInstance().getApiService();

        // Setup DatePickers
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // Default dates: Start of month to today
        updateDateLabel(etEndDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        updateDateLabel(etStartDate);

        btnLoadStats.setOnClickListener(v -> loadCompetitionStats());
    }

    private void showDatePicker(EditText target) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            updateDateLabel(target);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel(EditText target) {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        target.setText(sdf.format(calendar.getTime()));
    }

    private void loadCompetitionStats() {
        String start = etStartDate.getText().toString();
        String end = etEndDate.getText().toString();
        String token = SharedPrefsUtils.getToken(this);

        progressBar.setVisibility(android.view.View.VISIBLE);
        apiService.getCompetitionStats("Bearer " + token, start, end).enqueue(new Callback<CompetitionStatsResponse>() {
            @Override
            public void onResponse(Call<CompetitionStatsResponse> call, Response<CompetitionStatsResponse> response) {
                progressBar.setVisibility(android.view.View.GONE);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        CompetitionStatsResponse stats = response.body();
                        // Chuyển đổi CompetitionStatsResponse sang JSONArray để dùng hàm display cũ (hoặc viết lại hàm display)
                        // Cách tốt nhất là cập nhật hàm display để nhận List object thay vì JSONArray
                        // Nhưng để nhanh, ta sẽ dùng trực tiếp data object từ response
                        
                        displayClassRanking(stats.getClassRankings());
                        displayCommonViolations(stats.getCommonViolations());
                    } else {
                        Toast.makeText(AdminRankingActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(AdminRankingActivity.this, "Lỗi xử lý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompetitionStatsResponse> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(AdminRankingActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayClassRanking(List<CompetitionStatsResponse.ClassStat> list) {
        if (list == null) return;
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            CompetitionStatsResponse.ClassStat item = list.get(i);
            entries.add(new BarEntry(i, (float) item.getTotalDeducted()));
            labels.add(item.getClassName());
        }

        BarDataSet set = new BarDataSet(entries, "Điểm trừ");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(set);
        barChartClass.setData(data);

        XAxis xAxis = barChartClass.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);
        
        barChartClass.getDescription().setEnabled(false);
        barChartClass.animateY(1000);
        barChartClass.invalidate();
    }

    private void displayCommonViolations(List<CompetitionStatsResponse.RuleStat> list) {
        if (list == null) return;
        List<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            CompetitionStatsResponse.RuleStat item = list.get(i);
            entries.add(new PieEntry((float) item.getCount(), item.getRuleName()));
        }

        PieDataSet set = new PieDataSet(entries, "Lỗi vi phạm");
        set.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData data = new PieData(set);
        
        pieChartViolations.setData(data);
        pieChartViolations.getDescription().setEnabled(false);
        pieChartViolations.setEntryLabelColor(Color.BLACK);
        pieChartViolations.animateY(1000);
        pieChartViolations.invalidate();
    }
}
