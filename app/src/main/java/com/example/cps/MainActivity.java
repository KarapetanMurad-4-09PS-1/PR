package com.example.cps;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText, employeeIdText, userInfoText, instituteInfoText, tvStats;
    private MaterialButton btnOrderCertificate, btnViewHistory, btnProfile, btnVacation;
    private Button btnLogout;
    private CardView cardStats;
    private DatabaseHelper dbHelper;

    private int userId;
    private String username;
    private String fullName;
    private String phone;
    private String department;
    private String position;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        fullName = getIntent().getStringExtra("FULL_NAME");
        phone = getIntent().getStringExtra("PHONE");
        department = getIntent().getStringExtra("DEPARTMENT");
        position = getIntent().getStringExtra("POSITION");
        role = getIntent().getStringExtra("ROLE");

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupClickListeners();
        loadInstituteInfo();
        loadStats();
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        employeeIdText = findViewById(R.id.employeeIdText);
        userInfoText = findViewById(R.id.userInfoText);
        instituteInfoText = findViewById(R.id.instituteInfoText);
        tvStats = findViewById(R.id.tvStats);
        btnOrderCertificate = findViewById(R.id.btnOrderCertificate);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnProfile = findViewById(R.id.btnProfile);
        btnVacation = findViewById(R.id.btnVacation);
        btnLogout = findViewById(R.id.btnLogout);
        cardStats = findViewById(R.id.cardStats);

        User user = new User(userId, username, fullName, phone, department, position, role);

        welcomeText.setText((fullName != null ? fullName : "Сотрудник") + "!");
        employeeIdText.setText("Табельный №: " + user.getFormattedEmployeeId());

        String dept = (department != null && !department.equals("Не указан")) ? department : "—";
        String pos = (position != null && !position.equals("Не указана")) ? position : "—";
        userInfoText.setText("Отдел: " + dept + " | Должность: " + pos);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("НИИ ЦПС");
        }
    }

    private void setupClickListeners() {
        btnOrderCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrderDialog();
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("FULL_NAME", fullName);
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("FULL_NAME", fullName);
                startActivity(intent);
            }
        });

        btnVacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VacationActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void loadInstituteInfo() {
        String info = "НИИ «Центрпрограммсистем» (НИИ ЦПС)\n\n" +
                "🏢 Основан: 1971 год\n" +
                "👥 Сотрудников: >500\n" +
                "📍 Адрес: г. Тверь, ул. Горького, 56\n\n" +

                "📌 Основные направления:\n" +
                "• Разработка ПО для госструктур\n" +
                "• Системы автоматизации управления\n" +
                "• Обучающие комплексы\n" +
                "• Встроенные системы\n" +
                "• Защита информации\n\n" +

                "🏆 Ключевые проекты:\n" +
                "• АСУ для Министерства обороны\n" +
                "• Системы боевой подготовки\n" +
                "• Комплексы моделирования\n\n" +

                "📞 Контакты:\n" +
                "• Приемная: (4822) 32-10-55\n" +
                "• HR-отдел: (4822) 32-10-56\n" +
                "• Email: info@cps.tver.ru\n\n" +

                "⏰ Режим работы:\n" +
                "• Пн-Пт: 9:00 - 18:00\n" +
                "• Обед: 13:00 - 14:00";

        instituteInfoText.setText(info);
    }

    private void loadStats() {
        int ordersCount = dbHelper.getUserOrdersCount(userId);
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        tvStats.setText("📊 Ваши заказы: " + ordersCount + " | " + df.format(new Date()));
    }

    private void showOrderDialog() {
        final String[] types = {
                "Справка 2-НДФЛ",
                "Справка о заработной плате",
                "Копия трудовой книжки",
                "Справка о месте работы",
                "Справка для визы",
                "Справка в больницу",
                "Справка для учебы"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Выберите тип справки")
                .setItems(types, (dialog, which) -> {
                    confirmOrder(types[which]);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmOrder(String type) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Подтверждение")
                .setMessage("Заказать: " + type + "?")
                .setPositiveButton("Да", (dialog, which) -> {
                    createOrder(type);
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void createOrder(String type) {
        long result = dbHelper.createOrder(userId, fullName != null ? fullName : "Сотрудник", type);

        if (result != -1) {
            Snackbar.make(findViewById(android.R.id.content),
                    "✅ Заказ создан", Snackbar.LENGTH_SHORT).show();
            loadStats();
        } else {
            Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Выход")
                .setMessage("Выйти из системы?")
                .setPositiveButton("Да", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Нет", null)
                .show();
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