package com.eloka.cqrs.common.domain;

import java.util.Arrays;

public enum DeploymentRegion {
    EU_WEST("eu-west", 0),
    US_EAST("us-east", 1),
    AP_SOUTH("ap-south", 2);

    private final String code;
    private final int partition;

    DeploymentRegion(String code, int partition) {
        this.code = code;
        this.partition = partition;
    }

    public String code() {
        return code;
    }

    public int partition() {
        return partition;
    }

    public static DeploymentRegion fromCode(String code) {
        return Arrays.stream(values())
                .filter(region -> region.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region invalide: " + code));
    }
}

