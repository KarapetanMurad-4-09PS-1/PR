package com.example.cps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class VacationManagerAdapter extends RecyclerView.Adapter<VacationManagerAdapter.ViewHolder> {

    private List<VacationRequest> items = new ArrayList<>();
    private OnVacationStatusChangeListener listener;

    public interface OnVacationStatusChangeListener {
        void onStatusChange(int requestId, String newStatus);
    }

    public VacationManagerAdapter(OnVacationStatusChangeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VacationRequest> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vacation_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VacationRequest r = items.get(position);
        holder.tvEmployee.setText(r.getUserName() + " (ID: " + r.getUserId() + ")");
        holder.tvDate.setText("📅 " + r.getDate());
        holder.tvReason.setText("📝 " + r.getReason());
        holder.tvStatus.setText(r.getStatus());
        holder.tvCreated.setText("Создано: " + r.getCreatedAt());

        if (r.getStatus().contains("На рассмотрении")) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        } else if (r.getStatus().contains("Одобрено")) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }

        holder.btnChangeStatus.setOnClickListener(v -> {
            showStatusDialog(r.getId(), r.getStatus(), holder.itemView);
        });
    }

    private void showStatusDialog(int requestId, String currentStatus, View view) {
        String[] statuses = {"⏳ На рассмотрении", "✅ Одобрено", "❌ Отклонено"};

        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle("Изменить статус заявки")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    if (!newStatus.equals(currentStatus)) {
                        listener.onStatusChange(requestId, newStatus);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployee, tvDate, tvReason, tvStatus, tvCreated;
        Button btnChangeStatus;
        CardView cardItem;

        ViewHolder(View v) {
            super(v);
            tvEmployee = v.findViewById(R.id.tvEmployee);
            tvDate = v.findViewById(R.id.tvDate);
            tvReason = v.findViewById(R.id.tvReason);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvCreated = v.findViewById(R.id.tvCreated);
            btnChangeStatus = v.findViewById(R.id.btnChangeStatus);
            cardItem = v.findViewById(R.id.cardItem);
        }
    }
}