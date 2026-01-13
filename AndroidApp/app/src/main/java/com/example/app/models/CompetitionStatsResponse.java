package com.example.app.models;
import java.util.List;

public class CompetitionStatsResponse {
    private List<ClassStat> class_rankings;
    private List<RuleStat> common_violations;

    public List<ClassStat> getClassRankings() { return class_rankings; }
    public List<RuleStat> getCommonViolations() { return common_violations; }

    public static class ClassStat {
        private String class_name;
        private int total_deducted;
        public String getClassName() { return class_name; }
        public int getTotalDeducted() { return total_deducted; }
    }

    public static class RuleStat {
        private String rule_name;
        private int count;
        public String getRuleName() { return rule_name; }
        public int getCount() { return count; }
    }
}
