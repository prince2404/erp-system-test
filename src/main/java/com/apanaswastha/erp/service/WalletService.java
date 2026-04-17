package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.WalletTransactionRequest;
import com.apanaswastha.erp.dto.WalletTransactionResponse;
import com.apanaswastha.erp.entity.Family;
import com.apanaswastha.erp.entity.WalletTransaction;
import com.apanaswastha.erp.entity.enums.TransactionType;
import com.apanaswastha.erp.exception.InsufficientBalanceException;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.FamilyRepository;
import com.apanaswastha.erp.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WalletService {

    private final FamilyRepository familyRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletService(FamilyRepository familyRepository, WalletTransactionRepository walletTransactionRepository) {
        this.familyRepository = familyRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Transactional
    public WalletTransactionResponse credit(WalletTransactionRequest request) {
        return processTransaction(request, TransactionType.CREDIT);
    }

    @Transactional
    public WalletTransactionResponse debit(WalletTransactionRequest request) {
        return processTransaction(request, TransactionType.DEBIT);
    }

    public List<WalletTransactionResponse> getTransactions(String healthCardNumber) {
        Family family = familyRepository.findByHealthCardNumber(healthCardNumber)
                .orElseThrow(() -> new NotFoundException("Family not found with health card number: " + healthCardNumber));

        return walletTransactionRepository.findAllByFamilyIdOrderByCreatedAtDesc(family.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private WalletTransactionResponse processTransaction(
            WalletTransactionRequest request,
            TransactionType transactionType
    ) {
        if (walletTransactionRepository.existsByReferenceId(request.getReferenceId())) {
            throw new IllegalArgumentException("Duplicate transaction reference id");
        }

        Family family = familyRepository.findByHealthCardNumber(request.getHealthCardNumber())
                .orElseThrow(() -> new NotFoundException("Family not found with health card number: " + request.getHealthCardNumber()));

        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentBalance = family.getWalletBalance().setScale(2, RoundingMode.HALF_UP);

        if (transactionType == TransactionType.DEBIT && currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        BigDecimal newBalance = transactionType == TransactionType.CREDIT
                ? currentBalance.add(amount)
                : currentBalance.subtract(amount);
        family.setWalletBalance(newBalance);
        familyRepository.save(family);

        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setFamily(family);
        walletTransaction.setTransactionType(transactionType);
        walletTransaction.setAmount(amount);
        walletTransaction.setReferenceId(request.getReferenceId());
        walletTransaction.setDescription(request.getDescription());

        return toResponse(walletTransactionRepository.save(walletTransaction));
    }

    private WalletTransactionResponse toResponse(WalletTransaction walletTransaction) {
        return new WalletTransactionResponse(
                walletTransaction.getId(),
                walletTransaction.getFamily().getId(),
                walletTransaction.getTransactionType(),
                walletTransaction.getAmount(),
                walletTransaction.getReferenceId(),
                walletTransaction.getDescription(),
                walletTransaction.getCreatedAt()
        );
    }
}
