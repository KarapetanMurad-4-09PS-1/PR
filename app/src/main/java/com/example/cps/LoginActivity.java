package com.example.cps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvError, tvRegister;
    private ProgressBar progressBar;
    private TextInputLayout tilUsername, tilPassword;
    private DatabaseHelper dbHelper;

    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private long lastAttemptTime = 0;
    private static final long LOCKOUT_TIME = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);

        tvError.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void attemptLogin() {
        if (loginAttempts >= MAX_ATTEMPTS) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttemptTime < LOCKOUT_TIME) {
                long remainingSeconds = (LOCKOUT_TIME - (currentTime - lastAttemptTime)) / 1000;
                showError("Слишком много попыток. Подождите " + remainingSeconds + " сек.");
                return;
            } else {
                loginAttempts = 0;
            }
        }

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Введите логин");
            return;
        } else {
            tilUsername.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Введите пароль");
            return;
        } else {
            tilPassword.setError(null);
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        tvError.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                performLogin(username, password);
            }
        }, 1000);
    }

    private void performLogin(String username, String password) {
        User user = dbHelper.authenticateUser(username, password);

        if (user != null) {
            loginAttempts = 0;
            Log.i("AUTH", "Пользователь " + username + " вошел в систему. Роль: " + user.getRole());

            Intent intent;
            if (user.isManager()) {
                intent = new Intent(LoginActivity.this, ManagerActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }

            intent.putExtra("USER_ID", user.getId());
            intent.putExtra("USERNAME", user.getUsername());
            intent.putExtra("FULL_NAME", user.getFullName());
            intent.putExtra("PHONE", user.getPhone());
            intent.putExtra("DEPARTMENT", user.getDepartment());
            intent.putExtra("POSITION", user.getPosition());
            intent.putExtra("ROLE", user.getRole());

            startActivity(intent);
            finish();

        } else {
            loginAttempts++;
            lastAttemptTime = System.currentTimeMillis();

            Log.w("AUTH", "Неудачная попытка входа для пользователя " + username +
                    " (попытка " + loginAttempts + ")");

            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            int remainingAttempts = MAX_ATTEMPTS - loginAttempts;
            if (remainingAttempts > 0) {
                showError("Неверный логин или пароль. Осталось попыток: " + remainingAttempts);
            } else {
                showError("Аккаунт временно заблокирован на 30 секунд");
            }
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}