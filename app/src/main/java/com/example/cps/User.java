package com.example.cps;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String phone;
    private String department;
    private String position;
    private String role;

    public User(int id, String username, String fullName, String phone,
                String department, String position, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public String getRole() { return role; }
    public boolean isManager() { return "manager".equals(role); }

    public String getFormattedEmployeeId() {
        String digits = username.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return String.format("ТН-%04d", id);
        }
        return "ТН-" + digits;
    }
}