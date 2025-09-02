package org.example.domain;

public enum Platform {
    nongra, coupang, smartstore;

    public String dbValue() {
        return name().toLowerCase();
    }
}
