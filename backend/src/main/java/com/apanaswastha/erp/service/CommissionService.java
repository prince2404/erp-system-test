package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.commission.CommissionLedgerResponse;
import com.apanaswastha.erp.entity.Invoice;

import java.util.List;

public interface CommissionService {

    /**
     * Distributes commissions for a paid invoice.
     *
     * @param invoice paid invoice
     */
    void distributeForPaidInvoice(Invoice invoice);

    /**
     * Fetches commission entries for a user.
     *
     * @param userId user id
     * @return commission ledger entries
     */
    List<CommissionLedgerResponse> getUserCommissions(Long userId);
}
