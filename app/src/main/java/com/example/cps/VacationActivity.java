package com.example.cps;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VacationActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText etReason;
    private Button btnRequest;
    private TextView tvSelectedDate;
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private VacationAdapter adapter;

    private int userId;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation);

        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        createVacationTable();
        loadRequests();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Отпуска и отгулы");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        calendarView = findViewById(R.id.calendarView);
        etReason = findViewById(R.id.etReason);
        btnRequest = findViewById(R.id.btnRequest);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VacationAdapter();
        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year);
                tvSelectedDate.setText("📅 Выбрано: " + selectedDate);
            }
        });

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate.isEmpty()) {
                    Toast.makeText(VacationActivity.this, "Сначала выберите дату", Toast.LENGTH_SHORT).show();
                    return;
                }

                String reason = etReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    reason = "Отгул";
                }

                saveRequest(selectedDate, reason);
            }
        });
    }

    private void createVacationTable() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS vacations (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id TEXT," +
                    "date TEXT," +
                    "reason TEXT," +
                    "status TEXT," +
                    "created_at TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRequest(String date, String reason) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("date", date);
        values.put("reason", reason);
        values.put("status", "⏳ На рассмотрении");
        values.put("created_at", getCurrentDateTime());

        long result = db.insert("vacations", null, values);

        if (result != -1) {
            Toast.makeText(this, "✅ Заявка отправлена руководителю", Toast.LENGTH_SHORT).show();
            etReason.setText("");
            tvSelectedDate.setText("Дата не выбрана");
            selectedDate = "";
            loadRequests();
        } else {
            Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRequests() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM vacations WHERE user_id = ? ORDER BY date DESC",
                new String[]{String.valueOf(userId)}
        );

        List<VacationRequest> requests = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String reason = cursor.getString(cursor.getColumnIndex("reason"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            String created = cursor.getString(cursor.getColumnIndex("created_at"));

            requests.add(new VacationRequest(id, String.valueOf(userId), date, reason, status, created));
        }
        cursor.close();

        adapter.submitList(requests);
    }

    private String getCurrentDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return df.format(new Date());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.ViewHolder> {
        private List<VacationRequest> items = new ArrayList<>();

        public void submitList(List<VacationRequest> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vacation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VacationRequest r = items.get(position);
            holder.tvDate.setText(r.getDate());
            holder.tvReason.setText(r.getReason());
            holder.tvStatus.setText(r.getStatus());
            holder.tvCreated.setText(r.getCreatedAt());

            if (r.getStatus().contains("На рассмотрении")) {
                holder.tvStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
            } else if (r.getStatus().contains("Одобрено")) {
                holder.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            } else if (r.getStatus().contains("Отклонено")) {
                holder.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvReason, tvStatus, tvCreated;

            ViewHolder(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvDate);
                tvReason = v.findViewById(R.id.tvReason);
                tvStatus = v.findViewById(R.id.tvStatus);
                tvCreated = v.findViewById(R.id.tvCreated);
            }
        }
    }
}