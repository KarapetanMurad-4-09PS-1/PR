package com.example.cps;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class ProfileActivity extends AppCompatActivity {

    private EditText etPhone, etDepartment, etPosition;
    private TextView tvFullName, tvUsername, tvId;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    private int userId;
    private String username;
    private String fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        fullName = getIntent().getStringExtra("FULL_NAME");

        if (userId == -1) {
            Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        loadUserData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Мой профиль");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvFullName = findViewById(R.id.tvFullName);
        tvUsername = findViewById(R.id.tvUsername);
        tvId = findViewById(R.id.tvId);
        etPhone = findViewById(R.id.etPhone);
        etDepartment = findViewById(R.id.etDepartment);
        etPosition = findViewById(R.id.etPosition);
        btnSave = findViewById(R.id.btnSave);

        tvFullName.setText(fullName != null ? fullName : "Не указано");
        tvUsername.setText("Логин: " + (username != null ? username : "неизвестно"));
        tvId.setText("ID: " + userId);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void loadUserData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_DEPARTMENT,
                DatabaseHelper.COLUMN_POSITION
        };

        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
            String dept = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEPARTMENT));
            String pos = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSITION));

            etPhone.setText(phone != null && !phone.equals("null") ? phone : "");
            etDepartment.setText(dept != null && !dept.equals("Не указан") && !dept.equals("null") ? dept : "");
            etPosition.setText(pos != null && !pos.equals("Не указана") && !pos.equals("null") ? pos : "");
        }
        cursor.close();
    }

    private void saveProfile() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        String phone = etPhone.getText().toString().trim();
        String dept = etDepartment.getText().toString().trim();
        String pos = etPosition.getText().toString().trim();

        values.put(DatabaseHelper.COLUMN_PHONE, phone.isEmpty() ? "" : phone);
        values.put(DatabaseHelper.COLUMN_DEPARTMENT, dept.isEmpty() ? "Не указан" : dept);
        values.put(DatabaseHelper.COLUMN_POSITION, pos.isEmpty() ? "Не указана" : pos);

        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int count = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);

        if (count > 0) {
            Toast.makeText(this, "✅ Данные сохранены", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Ошибка сохранения", Toast.LENGTH_SHORT).show();
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