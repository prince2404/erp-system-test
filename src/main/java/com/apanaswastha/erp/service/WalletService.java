package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.wallet.WalletTransactionRequest;
import com.apanaswastha.erp.dto.response.wallet.WalletTransactionResponse;

import java.util.List;

public interface WalletService {

    /**
     * Credits wallet balance.
     *
     * @param request credit payload
     * @return wallet transaction details
     */
    WalletTransactionResponse credit(WalletTransactionRequest request);

    /**
     * Debits wallet balance.
     *
     * @param request debit payload
     * @return wallet transaction details
     */
    WalletTransactionResponse debit(WalletTransactionRequest request);

    /**
     * Lists wallet transactions for a health card.
     *
     * @param healthCardNumber family health card
     * @return transaction list
     */
    List<WalletTransactionResponse> getTransactions(String healthCardNumber);
}
