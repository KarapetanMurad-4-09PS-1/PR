package com.example.cps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void submitList(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCertificateType, tvStatus, tvOrderDate;
        private CardView cardItem;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertificateType = itemView.findViewById(R.id.tvCertificateType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            cardItem = itemView.findViewById(R.id.cardItem);
        }

        public void bind(Order order) {
            tvCertificateType.setText(order.getCertificateType());
            tvStatus.setText(order.getStatus());
            tvOrderDate.setText(order.getOrderDate());

            if (order.getStatus().equals("В обработке")) {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else if (order.getStatus().equals("Готова")) {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }
        }
    }
}