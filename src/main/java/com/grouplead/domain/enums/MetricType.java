package com.grouplead.domain.enums;

public enum MetricType {
    // Infrastructure metrics
    CPU_USAGE,
    MEMORY_USAGE,
    DISK_USAGE,
    NETWORK_IN,
    NETWORK_OUT,
    LATENCY_P50,
    LATENCY_P95,
    LATENCY_P99,
    ERROR_RATE,
    REQUEST_COUNT,

    // DORA metrics
    DEPLOYMENT_FREQUENCY,
    LEAD_TIME_FOR_CHANGES,
    CHANGE_FAILURE_RATE,
    MEAN_TIME_TO_RECOVERY,

    // Team productivity metrics
    VELOCITY,
    THROUGHPUT,
    CYCLE_TIME,
    STORY_POINTS_COMPLETED,
    TICKETS_COMPLETED,
    BUGS_FOUND,
    CODE_COVERAGE,

    // Code metrics
    COMMITS_COUNT,
    LINES_ADDED,
    LINES_DELETED,
    PR_COUNT,
    PR_REVIEW_TIME,

    // Cost metrics
    AWS_COST,
    COST_PER_SERVICE
}
