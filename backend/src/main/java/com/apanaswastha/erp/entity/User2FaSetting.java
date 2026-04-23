package com.apanaswastha.erp.entity;

import com.apanaswastha.erp.enums.TwoFaMethod;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_2fa_settings")
public class User2FaSetting {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 10)
    private TwoFaMethod method = TwoFaMethod.SMS;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public TwoFaMethod getMethod() { return method; }
    public void setMethod(TwoFaMethod method) { this.method = method; }
    public Instant getUpdatedAt() { return updatedAt; }
}
