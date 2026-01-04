package com.amore.aketer.domain.enums;

public enum AgeBand {
    AGE_20_EARLY("20_EARLY"),
    AGE_20_MID("20_MID"),
    AGE_20_LATE("20_LATE"),
    AGE_30_EARLY("30_EARLY"),
    AGE_30_MID("30_MID"),
    AGE_30_LATE("30_LATE");

    private final String code;

    AgeBand(String code) { this.code = code; }
    public String code() { return code; }

    public static AgeBand fromCode(String code) {
        if (code == null) return null;
        for (AgeBand v : values()) {
            if (v.code.equals(code)) return v;
        }
        throw new IllegalArgumentException("Unknown AgeBand code: " + code);
    }
}
