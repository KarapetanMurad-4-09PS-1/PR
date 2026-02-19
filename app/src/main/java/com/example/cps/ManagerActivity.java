package com.example.cps;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class ManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders, recyclerViewVacations;
    private TextView tvNoOrders, tvStats, tvVacationStats;
    private Button btnRefresh, btnLogout, btnShowOrders, btnShowVacations;
    private DatabaseHelper dbHelper;
    private ManagerOrderAdapter orderAdapter;
    private VacationManagerAdapter vacationAdapter;

    private int managerId;
    private String managerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        managerId = getIntent().getIntExtra("USER_ID", -1);
        managerName = getIntent().getStringExtra("FULL_NAME");


        dbHelper = new DatabaseHelper(this);
        if (!dbHelper.isUserManager(managerId)) {
            Toast.makeText(this, "Нет прав доступа", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupAdapters();
        setupClickListeners();


        showOrdersTab();
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewVacations = findViewById(R.id.recyclerViewVacations);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        tvStats = findViewById(R.id.tvStats);
        tvVacationStats = findViewById(R.id.tvVacationStats);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLogout = findViewById(R.id.btnLogout);
        btnShowOrders = findViewById(R.id.btnShowOrders);
        btnShowVacations = findViewById(R.id.btnShowVacations);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("HR Руководитель");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupAdapters() {

        orderAdapter = new ManagerOrderAdapter(new ManagerOrderAdapter.OnOrderStatusChangeListener() {
            @Override
            public void onStatusChange(int orderId, String newStatus) {
                updateOrderStatus(orderId, newStatus);
            }
        });
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);

        vacationAdapter = new VacationManagerAdapter(new VacationManagerAdapter.OnVacationStatusChangeListener() {
            @Override
            public void onStatusChange(int requestId, String newStatus) {
                updateVacationStatus(requestId, newStatus);
            }
        });
        recyclerViewVacations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVacations.setAdapter(vacationAdapter);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerViewOrders.getVisibility() == View.VISIBLE) {
                    loadAllOrders();
                } else {
                    loadAllVacations();
                }
                Toast.makeText(ManagerActivity.this, "Список обновлен", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnShowOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersTab();
            }
        });

        btnShowVacations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVacationsTab();
            }
        });
    }

    private void showOrdersTab() {
        recyclerViewOrders.setVisibility(View.VISIBLE);
        recyclerViewVacations.setVisibility(View.GONE);
        tvVacationStats.setVisibility(View.GONE);
        tvStats.setVisibility(View.VISIBLE);

        btnShowOrders.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
        btnShowVacations.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));

        loadAllOrders();
    }

    private void showVacationsTab() {
        recyclerViewOrders.setVisibility(View.GONE);
        recyclerViewVacations.setVisibility(View.VISIBLE);
        tvStats.setVisibility(View.GONE);
        tvVacationStats.setVisibility(View.VISIBLE);

        btnShowVacations.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
        btnShowOrders.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));

        loadAllVacations();
    }

    private void loadAllOrders() {
        List<ManagerOrder> orders = dbHelper.getAllOrders();

        int total = orders.size();
        int processing = 0;
        int ready = 0;
        int rejected = 0;

        for (ManagerOrder order : orders) {
            if (order.getStatus().contains("В обработке")) processing++;
            else if (order.getStatus().contains("Готова")) ready++;
            else if (order.getStatus().contains("Отклонена")) rejected++;
        }

        tvStats.setText(String.format("Всего: %d | В обработке: %d | Готово: %d | Отклонено: %d",
                total, processing, ready, rejected));

        if (orders.isEmpty()) {
            tvNoOrders.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
        } else {
            tvNoOrders.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
            orderAdapter.submitList(orders);
        }
    }

    private void loadAllVacations() {
        List<VacationRequest> requests = dbHelper.getAllVacationRequests();

        int total = requests.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (VacationRequest r : requests) {
            if (r.getStatus().contains("На рассмотрении")) pending++;
            else if (r.getStatus().contains("Одобрено")) approved++;
            else if (r.getStatus().contains("Отклонено")) rejected++;
        }

        tvVacationStats.setText(String.format("Всего: %d | В ожидании: %d | Одобрено: %d | Отклонено: %d",
                total, pending, approved, rejected));

        if (requests.isEmpty()) {
            tvVacationStats.setText("Нет заявок на отпуска");
        }
        vacationAdapter.submitList(requests);
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        boolean success = dbHelper.updateOrderStatus(orderId, newStatus, managerId, managerName);

        if (success) {
            Toast.makeText(this, "✅ Статус заказа обновлен", Toast.LENGTH_SHORT).show();
            loadAllOrders();
        } else {
            Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateVacationStatus(int requestId, String newStatus) {
        boolean success = dbHelper.updateVacationStatus(requestId, newStatus, managerId, managerName);

        if (success) {
            Toast.makeText(this, "✅ Статус заявки обновлен", Toast.LENGTH_SHORT).show();
            loadAllVacations();
        } else {
            Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}