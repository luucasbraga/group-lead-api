package com.grouplead.integration.aws.dto;

public enum Metric {
    UNBLENDED_COST("UNBLENDED_COST"),
    BLENDED_COST("BLENDED_COST"),
    AMORTIZED_COST("AMORTIZED_COST"),
    NET_UNBLENDED_COST("NET_UNBLENDED_COST"),
    NET_AMORTIZED_COST("NET_AMORTIZED_COST"),
    USAGE_QUANTITY("USAGE_QUANTITY"),
    NORMALIZED_USAGE_AMOUNT("NORMALIZED_USAGE_AMOUNT");

    private final String value;

    Metric(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
