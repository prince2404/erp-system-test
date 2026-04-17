package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.WalletTransactionRequest;
import com.apanaswastha.erp.dto.WalletTransactionResponse;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/credit")
    public ApiResponse<WalletTransactionResponse> credit(@Valid @RequestBody WalletTransactionRequest request) {
        return ApiResponse.success("Wallet credited successfully", walletService.credit(request));
    }

    @PostMapping("/debit")
    public ApiResponse<WalletTransactionResponse> debit(@Valid @RequestBody WalletTransactionRequest request) {
        return ApiResponse.success("Wallet debited successfully", walletService.debit(request));
    }

    @GetMapping("/{healthCardNumber}/transactions")
    public ApiResponse<List<WalletTransactionResponse>> getTransactions(@PathVariable String healthCardNumber) {
        return ApiResponse.success("Wallet transactions fetched successfully", walletService.getTransactions(healthCardNumber));
    }
}
