package com.apanaswastha.erp.dto;

public class CenterStockResponse {

    private final Long medicineId;
    private final String medicineName;
    private final Integer quantityAvailable;

    public CenterStockResponse(Long medicineId, String medicineName, Integer quantityAvailable) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantityAvailable = quantityAvailable;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }
}
