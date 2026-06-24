package org.example.domain.enums;

public enum Platform {
    nongra, coupang, smartstore;

    public String dbValue() {
        return name().toLowerCase();
    }
}
