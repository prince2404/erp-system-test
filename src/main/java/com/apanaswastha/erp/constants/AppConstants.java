package com.apanaswastha.erp.constants;

public final class AppConstants {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final long ACCESS_TOKEN_EXPIRY_MILLIS = 15 * 60 * 1000L;
    public static final long REFRESH_TOKEN_EXPIRY_MILLIS = 7 * 24 * 60 * 60 * 1000L;

    private AppConstants() {
    }
}
