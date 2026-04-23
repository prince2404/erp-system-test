package com.apanaswastha.erp.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private String timestamp;
    private String path;
}
