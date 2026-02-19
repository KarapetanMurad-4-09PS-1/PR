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

public class ManagerOrderAdapter extends RecyclerView.Adapter<ManagerOrderAdapter.ManagerOrderViewHolder> {

    private List<ManagerOrder> orders = new ArrayList<>();
    private OnOrderStatusChangeListener listener;

    public interface OnOrderStatusChangeListener {
        void onStatusChange(int orderId, String newStatus);
    }

    public ManagerOrderAdapter(OnOrderStatusChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ManagerOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manager_order, parent, false);
        return new ManagerOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManagerOrderViewHolder holder, int position) {
        ManagerOrder order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void submitList(List<ManagerOrder> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class ManagerOrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEmployee, tvCertificateType, tvStatus, tvDate, tvId;
        private Button btnChangeStatus;
        private CardView cardItem;

        public ManagerOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployee = itemView.findViewById(R.id.tvEmployee);
            tvCertificateType = itemView.findViewById(R.id.tvCertificateType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvId = itemView.findViewById(R.id.tvId);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
            cardItem = itemView.findViewById(R.id.cardItem);
        }

        public void bind(ManagerOrder order) {
            tvEmployee.setText(order.getEmployeeName());
            tvCertificateType.setText(order.getCertificateType());
            tvStatus.setText(order.getStatus());
            tvDate.setText(order.getOrderDate());
            tvId.setText("Заказ №" + order.getId());

            if (order.getStatus().equals("В обработке")) {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else if (order.getStatus().equals("Готова")) {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }

            btnChangeStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showStatusDialog(order.getId(), order.getStatus());
                }
            });
        }

        private void showStatusDialog(int orderId, String currentStatus) {
            String[] statuses = {"В обработке", "Готова", "Отклонена"};

            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setTitle("Изменить статус заказа")
                    .setItems(statuses, (dialog, which) -> {
                        String newStatus = statuses[which];
                        if (!newStatus.equals(currentStatus)) {
                            listener.onStatusChange(orderId, newStatus);
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }
}