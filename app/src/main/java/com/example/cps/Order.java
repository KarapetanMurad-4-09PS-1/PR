package com.example.cps;

public class Order {
    private int id;
    private String certificateType;
    private String status;
    private String orderDate;


    public Order(int id, String certificateType, String status, String orderDate) {
        this.id = id;
        this.certificateType = certificateType;
        this.status = status;
        this.orderDate = orderDate;
    }

    public Order(String certificateType, String status, String orderDate) {
        this.id = -1;
        this.certificateType = certificateType;
        this.status = status;
        this.orderDate = orderDate;
    }

    // Геттеры
    public int getId() { return id; }
    public String getCertificateType() { return certificateType; }
    public String getStatus() { return status; }
    public String getOrderDate() { return orderDate; }
}