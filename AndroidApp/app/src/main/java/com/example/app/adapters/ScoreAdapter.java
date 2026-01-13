package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.Score;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    private List<Score> scoreList;

    public ScoreAdapter(List<Score> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Score score = scoreList.get(position);
        holder.tvSubjectName.setText(score.getSubjectName());
        holder.tvScore15m.setText("15p: " + score.getScore15m());
        holder.tvScore45m.setText("1 Tiết: " + score.getScore45m());
        holder.tvScoreFinal.setText("Cuối kỳ: " + score.getScoreFinal());
    }

    @Override
    public int getItemCount() {
        return scoreList != null ? scoreList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvScore15m, tvScore45m, tvScoreFinal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvScore15m = itemView.findViewById(R.id.tvScore15m);
            tvScore45m = itemView.findViewById(R.id.tvScore45m);
            tvScoreFinal = itemView.findViewById(R.id.tvScoreFinal);
        }
    }
}
