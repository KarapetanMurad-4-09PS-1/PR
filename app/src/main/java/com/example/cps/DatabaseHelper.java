package com.example.cps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HRSecure.db";
    private static final int DATABASE_VERSION = 5;


    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DEPARTMENT = "department";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_LAST_LOGIN = "last_login";


    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EMPLOYEE_ID = "employee_id";
    public static final String COLUMN_EMPLOYEE_NAME = "employee_name";
    public static final String COLUMN_CERTIFICATE_TYPE = "certificate_type";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_UPDATED_BY = "updated_by";
    public static final String COLUMN_UPDATE_DATE = "update_date";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_DEPARTMENT + " TEXT, " +
                COLUMN_POSITION + " TEXT, " +
                COLUMN_ROLE + " TEXT NOT NULL, " +
                COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                COLUMN_LAST_LOGIN + " TEXT)";
        db.execSQL(createUsersTable);


        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMPLOYEE_ID + " TEXT NOT NULL, " +
                COLUMN_EMPLOYEE_NAME + " TEXT NOT NULL, " +
                COLUMN_CERTIFICATE_TYPE + " TEXT NOT NULL, " +
                COLUMN_STATUS + " TEXT NOT NULL, " +
                COLUMN_ORDER_DATE + " TEXT NOT NULL, " +
                COLUMN_UPDATED_BY + " TEXT, " +
                COLUMN_UPDATE_DATE + " TEXT)";
        db.execSQL(createOrdersTable);


        createVacationTable(db);


        createTestUsers(db);
    }

    private void createVacationTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS vacations (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id TEXT," +
                    "date TEXT," +
                    "reason TEXT," +
                    "status TEXT," +
                    "created_at TEXT," +
                    "updated_by TEXT," +
                    "update_date TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTestUsers(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, "hr.chief");
        values.put(COLUMN_PASSWORD_HASH, hashPassword("hr123"));
        values.put(COLUMN_FULL_NAME, "Иванов Иван Иванович");
        values.put(COLUMN_PHONE, "+79161234567");
        values.put(COLUMN_DEPARTMENT, "Управление персоналом");
        values.put(COLUMN_POSITION, "Начальник отдела кадров");
        values.put(COLUMN_ROLE, "manager");
        db.insert(TABLE_USERS, null, values);

        values.clear();
        values.put(COLUMN_USERNAME, "employee1");
        values.put(COLUMN_PASSWORD_HASH, hashPassword("emp123"));
        values.put(COLUMN_FULL_NAME, "Петров Петр Петрович");
        values.put(COLUMN_PHONE, "+79167654321");
        values.put(COLUMN_DEPARTMENT, "Отдел разработки");
        values.put(COLUMN_POSITION, "Инженер-программист");
        values.put(COLUMN_ROLE, "employee");
        db.insert(TABLE_USERS, null, values);

        values.clear();
        values.put(COLUMN_USERNAME, "employee2");
        values.put(COLUMN_PASSWORD_HASH, hashPassword("emp456"));
        values.put(COLUMN_FULL_NAME, "Сидорова Анна Сергеевна");
        values.put(COLUMN_PHONE, "+79169876543");
        values.put(COLUMN_DEPARTMENT, "Отдел тестирования");
        values.put(COLUMN_POSITION, "Инженер-тестировщик");
        values.put(COLUMN_ROLE, "employee");
        db.insert(TABLE_USERS, null, values);
    }

    public String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS vacations");
        onCreate(db);
    }

    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_FULL_NAME,
                COLUMN_PHONE,
                COLUMN_DEPARTMENT,
                COLUMN_POSITION,
                COLUMN_ROLE
        };

        String selection = COLUMN_USERNAME + " = ? AND " +
                COLUMN_PASSWORD_HASH + " = ? AND " +
                COLUMN_IS_ACTIVE + " = 1";
        String[] selectionArgs = { username, hashPassword(password) };

        Cursor cursor = db.query(
                TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            String dept = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT));
            String pos = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));

            user = new User(id, username, name, phone, dept, pos, role);
            updateLastLogin(id);
        }
        cursor.close();

        return user;
    }

    private void updateLastLogin(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, getCurrentDateTime());

        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        db.update(TABLE_USERS, values, selection, selectionArgs);
    }

    public long createOrder(int userId, String userName, String certificateType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMPLOYEE_ID, String.valueOf(userId));
        values.put(COLUMN_EMPLOYEE_NAME, userName);
        values.put(COLUMN_CERTIFICATE_TYPE, certificateType);
        values.put(COLUMN_STATUS, "⏳ В обработке");
        values.put(COLUMN_ORDER_DATE, getCurrentDateTime());

        return db.insert(TABLE_ORDERS, null, values);
    }

    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_ID,
                COLUMN_CERTIFICATE_TYPE,
                COLUMN_STATUS,
                COLUMN_ORDER_DATE
        };

        String selection = COLUMN_EMPLOYEE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        Cursor cursor = db.query(
                TABLE_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_ORDER_DATE + " DESC"
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CERTIFICATE_TYPE));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE));

            orders.add(new Order(id, type, status, date));
        }
        cursor.close();

        return orders;
    }

    public List<ManagerOrder> getAllOrders() {
        List<ManagerOrder> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_ORDERS + " ORDER BY " + COLUMN_ORDER_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String employeeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMPLOYEE_NAME));
            String employeeId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMPLOYEE_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CERTIFICATE_TYPE));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE));
            String updatedBy = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_BY));
            String updateDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATE_DATE));

            orders.add(new ManagerOrder(id, employeeName, employeeId, type, status, date, updatedBy, updateDate));
        }
        cursor.close();

        return orders;
    }

    public boolean updateOrderStatus(int orderId, String newStatus, int managerId, String managerName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, newStatus);
        values.put(COLUMN_UPDATED_BY, managerName + " (ID: " + managerId + ")");
        values.put(COLUMN_UPDATE_DATE, getCurrentDateTime());

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(orderId) };

        int count = db.update(TABLE_ORDERS, values, selection, selectionArgs);
        return count > 0;
    }

    public int getUserOrdersCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS +
                " WHERE " + COLUMN_EMPLOYEE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }


    public List<VacationRequest> getAllVacationRequests() {
        List<VacationRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        createVacationTable(db);


        String query = "SELECT v.*, u." + COLUMN_FULL_NAME +
                " FROM vacations v" +
                " LEFT JOIN " + TABLE_USERS + " u ON v.user_id = u." + COLUMN_USER_ID +
                " ORDER BY v.date DESC";

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String reason = cursor.getString(cursor.getColumnIndexOrThrow("reason"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
            String userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME));

            requests.add(new VacationRequest(id, userId, userName, date, reason, status, createdAt));
        }
        cursor.close();

        return requests;
    }

    public List<VacationRequest> getUserVacationRequests(int userId) {
        List<VacationRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        createVacationTable(db);

        String query = "SELECT * FROM vacations WHERE user_id = ? ORDER BY date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String reason = cursor.getString(cursor.getColumnIndexOrThrow("reason"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));

            requests.add(new VacationRequest(id, String.valueOf(userId), "", date, reason, status, createdAt));
        }
        cursor.close();

        return requests;
    }

    public boolean updateVacationStatus(int requestId, String newStatus, int managerId, String managerName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        values.put("updated_by", managerName + " (ID: " + managerId + ")");
        values.put("update_date", getCurrentDateTime());

        String selection = "_id = ?";
        String[] args = {String.valueOf(requestId)};

        int count = db.update("vacations", values, selection, args);
        return count > 0;
    }


    public boolean isUserManager(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        boolean isManager = false;
        if (cursor.moveToFirst()) {
            isManager = "manager".equals(cursor.getString(0));
        }
        cursor.close();
        return isManager;
    }


    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count > 0;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}