package com.example.cps;

public class VacationRequest {
    private int id;
    private String userId;
    private String userName;
    private String date;
    private String reason;
    private String status;
    private String createdAt;


    public VacationRequest(int id, String userId, String userName, String date,
                           String reason, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public VacationRequest(int id, String userId, String date,
                           String reason, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = "";
        this.date = date;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }


    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}