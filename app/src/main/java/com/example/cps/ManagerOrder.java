package com.example.cps;

public class ManagerOrder {
    private int id;
    private String employeeName;
    private String employeeId;
    private String certificateType;
    private String status;
    private String orderDate;
    private String updatedBy;
    private String updateDate;


    public ManagerOrder(int id, String employeeName, String employeeId,
                        String certificateType, String status, String orderDate,
                        String updatedBy, String updateDate) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.certificateType = certificateType;
        this.status = status;
        this.orderDate = orderDate;
        this.updatedBy = updatedBy;
        this.updateDate = updateDate;
    }

    // Геттеры
    public int getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeId() { return employeeId; }
    public String getCertificateType() { return certificateType; }
    public String getStatus() { return status; }
    public String getOrderDate() { return orderDate; }
    public String getUpdatedBy() { return updatedBy; }
    public String getUpdateDate() { return updateDate; }
}