package com.example.cps;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword, etConfirmPassword, etPhone, etSecretCode;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private TextInputLayout tilFullName, tilUsername, tilPassword, tilConfirmPassword, tilPhone, tilSecretCode;
    private RadioGroup radioGroupRole;
    private RadioButton radioEmployee, radioManager;
    private DatabaseHelper dbHelper;

    // СЕКРЕТНЫЙ КОД ДЛЯ РУКОВОДИТЕЛЯ (только для демо)
    private static final String MANAGER_SECRET_CODE = "HR2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
        setupRoleListener();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        etSecretCode = findViewById(R.id.etSecretCode);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        tilFullName = findViewById(R.id.tilFullName);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone = findViewById(R.id.tilPhone);
        tilSecretCode = findViewById(R.id.tilSecretCode);

        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioEmployee = findViewById(R.id.radioEmployee);
        radioManager = findViewById(R.id.radioManager);

        progressBar.setVisibility(View.GONE);

        tilSecretCode.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRoleListener() {
        radioGroupRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioManager) {
                    tilSecretCode.setVisibility(View.VISIBLE);
                } else {
                    tilSecretCode.setVisibility(View.GONE);
                    tilSecretCode.setError(null);
                }
            }
        });
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String secretCode = etSecretCode.getText().toString().trim();

        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Введите ФИО");
            return;
        } else {
            tilFullName.setError(null);
        }

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Введите логин");
            return;
        } else if (username.length() < 4) {
            tilUsername.setError("Логин должен содержать минимум 4 символа");
            return;
        } else if (dbHelper.isUsernameExists(username)) {
            tilUsername.setError("Такой логин уже существует");
            return;
        } else {
            tilUsername.setError(null);
        }


        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Введите пароль");
            return;
        } else if (password.length() < 6) {
            tilPassword.setError("Пароль должен содержать минимум 6 символов");
            return;
        } else {
            tilPassword.setError(null);
        }


        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Пароли не совпадают");
            return;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Введите телефон");
            return;
        } else if (!Patterns.PHONE.matcher(phone).matches() && !phone.matches("[0-9]{10,11}")) {
            tilPhone.setError("Введите корректный телефон (10-11 цифр)");
            return;
        } else {
            tilPhone.setError(null);
        }

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Выберите роль", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = (selectedRoleId == R.id.radioManager) ? "manager" : "employee";


        if (role.equals("manager")) {
            if (TextUtils.isEmpty(secretCode)) {
                tilSecretCode.setError("Введите секретный код (HR2024)");
                return;
            } else if (!secretCode.equals(MANAGER_SECRET_CODE)) {
                tilSecretCode.setError("Неверный секретный код");
                return;
            } else {
                tilSecretCode.setError(null);
            }
        }


        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                performRegistration(fullName, username, password, phone, role);
            }
        }, 1500);
    }

    private void performRegistration(String fullName, String username, String password,
                                     String phone, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, dbHelper.hashPassword(password));
        values.put(DatabaseHelper.COLUMN_FULL_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_PHONE, phone);

        if (role.equals("manager")) {
            values.put(DatabaseHelper.COLUMN_DEPARTMENT, "Управление персоналом");
            values.put(DatabaseHelper.COLUMN_POSITION, "Руководитель HR");
        } else {
            values.put(DatabaseHelper.COLUMN_DEPARTMENT, "Не указан");
            values.put(DatabaseHelper.COLUMN_POSITION, "Не указана");
        }

        values.put(DatabaseHelper.COLUMN_ROLE, role);
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, 1);

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);

        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);

        if (result != -1) {
            Toast.makeText(this, "Регистрация успешна! Теперь можно войти", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при регистрации", Toast.LENGTH_LONG).show();
        }
    }
}