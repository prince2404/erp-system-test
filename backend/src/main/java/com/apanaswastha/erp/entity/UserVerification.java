package com.apanaswastha.erp.entity;

import com.apanaswastha.erp.enums.PhotoIdType;
import com.apanaswastha.erp.enums.VerificationStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_verifications")
public class UserVerification {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "aadhaar_last4", length = 4)
    private String aadhaarLast4;

    @Column(name = "aadhaar_doc_url", length = 500)
    private String aadhaarDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "aadhaar_status", length = 20)
    private VerificationStatus aadhaarStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "aadhaar_verified_at")
    private Instant aadhaarVerifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aadhaar_reviewed_by")
    private User aadhaarReviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_id_type", length = 30)
    private PhotoIdType photoIdType;

    @Column(name = "photo_id_doc_url", length = 500)
    private String photoIdDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_id_status", length = 20)
    private VerificationStatus photoIdStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "photo_id_reject_reason", length = 300)
    private String photoIdRejectReason;

    @Column(name = "photo_id_verified_at")
    private Instant photoIdVerifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id_reviewed_by")
    private User photoIdReviewedBy;

    @Column(name = "phone_failed_attempts", nullable = false)
    private int phoneFailedAttempts = 0;

    @Column(name = "email_failed_attempts", nullable = false)
    private int emailFailedAttempts = 0;

    @Column(name = "phone_locked_until")
    private Instant phoneLockedUntil;

    @Column(name = "email_locked_until")
    private Instant emailLockedUntil;

    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    public Instant getPhoneVerifiedAt() { return phoneVerifiedAt; }
    public void setPhoneVerifiedAt(Instant phoneVerifiedAt) { this.phoneVerifiedAt = phoneVerifiedAt; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Instant emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
    public String getAadhaarLast4() { return aadhaarLast4; }
    public void setAadhaarLast4(String aadhaarLast4) { this.aadhaarLast4 = aadhaarLast4; }
    public String getAadhaarDocUrl() { return aadhaarDocUrl; }
    public void setAadhaarDocUrl(String aadhaarDocUrl) { this.aadhaarDocUrl = aadhaarDocUrl; }
    public VerificationStatus getAadhaarStatus() { return aadhaarStatus; }
    public void setAadhaarStatus(VerificationStatus aadhaarStatus) { this.aadhaarStatus = aadhaarStatus; }
    public Instant getAadhaarVerifiedAt() { return aadhaarVerifiedAt; }
    public void setAadhaarVerifiedAt(Instant aadhaarVerifiedAt) { this.aadhaarVerifiedAt = aadhaarVerifiedAt; }
    public User getAadhaarReviewedBy() { return aadhaarReviewedBy; }
    public void setAadhaarReviewedBy(User aadhaarReviewedBy) { this.aadhaarReviewedBy = aadhaarReviewedBy; }
    public PhotoIdType getPhotoIdType() { return photoIdType; }
    public void setPhotoIdType(PhotoIdType photoIdType) { this.photoIdType = photoIdType; }
    public String getPhotoIdDocUrl() { return photoIdDocUrl; }
    public void setPhotoIdDocUrl(String photoIdDocUrl) { this.photoIdDocUrl = photoIdDocUrl; }
    public VerificationStatus getPhotoIdStatus() { return photoIdStatus; }
    public void setPhotoIdStatus(VerificationStatus photoIdStatus) { this.photoIdStatus = photoIdStatus; }
    public String getPhotoIdRejectReason() { return photoIdRejectReason; }
    public void setPhotoIdRejectReason(String photoIdRejectReason) { this.photoIdRejectReason = photoIdRejectReason; }
    public Instant getPhotoIdVerifiedAt() { return photoIdVerifiedAt; }
    public void setPhotoIdVerifiedAt(Instant photoIdVerifiedAt) { this.photoIdVerifiedAt = photoIdVerifiedAt; }
    public User getPhotoIdReviewedBy() { return photoIdReviewedBy; }
    public void setPhotoIdReviewedBy(User photoIdReviewedBy) { this.photoIdReviewedBy = photoIdReviewedBy; }
    public int getPhoneFailedAttempts() { return phoneFailedAttempts; }
    public void setPhoneFailedAttempts(int phoneFailedAttempts) { this.phoneFailedAttempts = phoneFailedAttempts; }
    public int getEmailFailedAttempts() { return emailFailedAttempts; }
    public void setEmailFailedAttempts(int emailFailedAttempts) { this.emailFailedAttempts = emailFailedAttempts; }
    public Instant getPhoneLockedUntil() { return phoneLockedUntil; }
    public void setPhoneLockedUntil(Instant phoneLockedUntil) { this.phoneLockedUntil = phoneLockedUntil; }
    public Instant getEmailLockedUntil() { return emailLockedUntil; }
    public void setEmailLockedUntil(Instant emailLockedUntil) { this.emailLockedUntil = emailLockedUntil; }
}
