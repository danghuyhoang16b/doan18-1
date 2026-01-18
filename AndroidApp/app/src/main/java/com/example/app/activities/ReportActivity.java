package com.example.app.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.ConductAdapter;
import com.example.app.adapters.ScoreAdapter;
import com.example.app.network.ApiService;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;
import com.example.app.models.CompetitionStatsResponse;
import com.example.app.models.Conduct;
import com.example.app.models.Score;
import com.example.app.models.StatisticsResponse;
import com.example.app.models.TokenRequest;
import com.example.app.network.ApiClient;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvReport;
    private View layoutStatistics;
    private BarChart barChart;
    private PieChart pieChart;
    private BarChart barChartCompetition;
    private PieChart pieChartViolations;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabLayout = findViewById(R.id.tabLayout);
        rvReport = findViewById(R.id.rvReport);
        layoutStatistics = findViewById(R.id.layoutStatistics);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        barChartCompetition = findViewById(R.id.barChartCompetition);
        pieChartViolations = findViewById(R.id.pieChartViolations);

        rvReport.setLayoutManager(new LinearLayoutManager(this));

        token = SharedPrefsUtils.getToken(this);
        apiService = ApiClient.getInstance().getApiService();

        // Load default tab (Academic)
        loadAcademicReport();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvReport.setVisibility(View.VISIBLE);
                    layoutStatistics.setVisibility(View.GONE);
                    loadAcademicReport();
                } else if (tab.getPosition() == 1) {
                    rvReport.setVisibility(View.VISIBLE);
                    layoutStatistics.setVisibility(View.GONE);
                    loadConductReport();
                } else {
                    rvReport.setVisibility(View.GONE);
                    layoutStatistics.setVisibility(View.VISIBLE);
                    loadStatistics();
                    loadCompetitionStats();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadStatistics() {
        apiService.getStatistics("Bearer " + token).enqueue(new Callback<StatisticsResponse>() {
            @Override
            public void onResponse(Call<StatisticsResponse> call, Response<StatisticsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayCharts(response.body());
                } else {
                    String errorMsg = "Không thể tải thống kê (HTTP " + response.code() + ")";
                    android.util.Log.e("ReportActivity", errorMsg);
                    Toast.makeText(ReportActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatisticsResponse> call, Throwable t) {
                android.util.Log.e("ReportActivity", "Statistics error: " + t.getMessage(), t);
                Toast.makeText(ReportActivity.this, "Lỗi kết nối thống kê: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCharts(StatisticsResponse stats) {
        // Bar Chart - Scores
        List<BarEntry> scoreEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<StatisticsResponse.ScoreStat> scores = stats.getScores();
        
        for (int i = 0; i < scores.size(); i++) {
            scoreEntries.add(new BarEntry(i, scores.get(i).getScore()));
            labels.add(scores.get(i).getSubjectName());
        }

        BarDataSet scoreDataSet = new BarDataSet(scoreEntries, "Điểm số");
        scoreDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        scoreDataSet.setValueTextColor(Color.BLACK);
        scoreDataSet.setValueTextSize(12f);

        BarData scoreData = new BarData(scoreDataSet);
        barChart.setFitBars(true);
        barChart.setData(scoreData);
        barChart.getDescription().setText("Điểm các môn học");
        barChart.animateY(1000);
        barChart.invalidate(); // Refresh

        // Pie Chart - Attendance
        List<PieEntry> attendanceEntries = new ArrayList<>();
        List<StatisticsResponse.AttendanceStat> attendance = stats.getAttendance();
        
        for (StatisticsResponse.AttendanceStat stat : attendance) {
            String label = stat.getStatus().equals("present") ? "Có mặt" : 
                          (stat.getStatus().equals("absent") ? "Vắng" : "Muộn");
            attendanceEntries.add(new PieEntry(stat.getCount(), label));
        }

        PieDataSet attendanceDataSet = new PieDataSet(attendanceEntries, "Điểm danh");
        attendanceDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        attendanceDataSet.setValueTextColor(Color.WHITE);
        attendanceDataSet.setValueTextSize(14f);

        PieData attendanceData = new PieData(attendanceDataSet);
        pieChart.setData(attendanceData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Tỷ lệ\nChuyên cần");
        pieChart.animate();
        pieChart.invalidate();
    }

    private void loadAcademicReport() {
        apiService.getAcademicReport("Bearer " + token).enqueue(new Callback<List<Score>>() {
            @Override
            public void onResponse(Call<List<Score>> call, Response<List<Score>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScoreAdapter adapter = new ScoreAdapter(response.body());
                    rvReport.setAdapter(adapter);
                } else {
                    Toast.makeText(ReportActivity.this, "Không thể tải báo cáo học tập", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Score>> call, Throwable t) {
                Toast.makeText(ReportActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadConductReport() {
        apiService.getConductReport("Bearer " + token).enqueue(new Callback<List<Conduct>>() {
            @Override
            public void onResponse(Call<List<Conduct>> call, Response<List<Conduct>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConductAdapter adapter = new ConductAdapter(response.body());
                    rvReport.setAdapter(adapter);
                } else {
                    Toast.makeText(ReportActivity.this, "Không thể tải báo cáo hạnh kiểm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Conduct>> call, Throwable t) {
                Toast.makeText(ReportActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadCompetitionStats() {
        apiService.getCompetitionStats("Bearer " + token).enqueue(new Callback<CompetitionStatsResponse>() {
            @Override
            public void onResponse(Call<CompetitionStatsResponse> call, Response<CompetitionStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayCompetitionCharts(response.body());
                } else {
                    String errorMsg = "Không thể tải thống kê thi đua (HTTP " + response.code() + ")";
                    android.util.Log.e("ReportActivity", errorMsg);
                    Toast.makeText(ReportActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompetitionStatsResponse> call, Throwable t) {
                android.util.Log.e("ReportActivity", "Competition stats error: " + t.getMessage(), t);
                Toast.makeText(ReportActivity.this, "Lỗi kết nối thống kê thi đua: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCompetitionCharts(CompetitionStatsResponse stats) {
        // Class Rankings
        List<BarEntry> rankingEntries = new ArrayList<>();
        List<String> rankingLabels = new ArrayList<>();
        List<CompetitionStatsResponse.ClassStat> rankings = stats.getClassRankings();
        if (rankings != null) {
            for (int i = 0; i < rankings.size(); i++) {
                rankingEntries.add(new BarEntry(i, rankings.get(i).getTotalDeducted()));
                rankingLabels.add(rankings.get(i).getClassName());
            }
            BarDataSet rankingDataSet = new BarDataSet(rankingEntries, "Điểm trừ");
            rankingDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            rankingDataSet.setValueTextColor(android.graphics.Color.BLACK);
            rankingDataSet.setValueTextSize(12f);

            BarData rankingData = new BarData(rankingDataSet);
            barChartCompetition.setData(rankingData);
            barChartCompetition.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(rankingLabels));
            barChartCompetition.getXAxis().setGranularity(1f);
            barChartCompetition.getDescription().setText("Xếp hạng lớp (theo điểm trừ)");
            barChartCompetition.animateY(1000);
            barChartCompetition.invalidate();
        }

        // Common Violations
        List<PieEntry> violationEntries = new ArrayList<>();
        List<CompetitionStatsResponse.RuleStat> violations = stats.getCommonViolations();
        if (violations != null) {
            for (CompetitionStatsResponse.RuleStat stat : violations) {
                violationEntries.add(new PieEntry(stat.getCount(), stat.getRuleName()));
            }
            PieDataSet violationDataSet = new PieDataSet(violationEntries, "Lỗi vi phạm");
            violationDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
            violationDataSet.setValueTextColor(android.graphics.Color.WHITE);
            violationDataSet.setValueTextSize(14f);

            PieData violationData = new PieData(violationDataSet);
            pieChartViolations.setData(violationData);
            pieChartViolations.getDescription().setEnabled(false);
            pieChartViolations.setCenterText("Lỗi phổ biến");
            pieChartViolations.animate();
            pieChartViolations.invalidate();
        }
    }
}
