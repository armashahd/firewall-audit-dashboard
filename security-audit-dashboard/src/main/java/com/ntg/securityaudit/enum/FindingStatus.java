package com.ntg.securityaudit.enums;

public enum FindingStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    CLOSED("Closed"),
    ACCEPTED_RISK("Accepted Risk");

    private final String displayName;

    FindingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isAcceptedRisk() {
        return this == ACCEPTED_RISK;
    }

    public boolean isOpenOrInProgress() {
        return this == OPEN || this == IN_PROGRESS;
    }
}
