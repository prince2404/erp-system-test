package com.apanaswastha.erp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "language", nullable = false, length = 10)
    private String language = "en";

    @Column(name = "theme", nullable = false, length = 10)
    private String theme = "light";

    @Column(name = "notif_inapp", nullable = false, columnDefinition = "TEXT")
    private String notifInapp = "{}";

    @Column(name = "notif_sms", nullable = false, columnDefinition = "TEXT")
    private String notifSms = "{}";

    @Column(name = "notif_email", nullable = false, columnDefinition = "TEXT")
    private String notifEmail = "{}";

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getNotifInapp() { return notifInapp; }
    public void setNotifInapp(String notifInapp) { this.notifInapp = notifInapp; }
    public String getNotifSms() { return notifSms; }
    public void setNotifSms(String notifSms) { this.notifSms = notifSms; }
    public String getNotifEmail() { return notifEmail; }
    public void setNotifEmail(String notifEmail) { this.notifEmail = notifEmail; }
}
