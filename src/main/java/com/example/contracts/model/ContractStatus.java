package com.example.contracts.model;

public enum ContractStatus {
    DRAFT,
    REVIEW,
    APPROVED;

    public boolean canTransitionTo(ContractStatus target) {
        if (target == null) return false;
        return switch (this) {
            case DRAFT -> target == REVIEW;
            case REVIEW -> target == APPROVED || target == DRAFT;
            case APPROVED -> false;
        };
    }
}
