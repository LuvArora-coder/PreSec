package com.urjalusa.presec;

class PrescriptionCardView {
    private final String prescriptionId;
    private final String displayName;
    private final String date;

    PrescriptionCardView(String prescriptionId, String displayName, String date) {
        this.prescriptionId = prescriptionId;
        this.displayName = displayName;
        this.date = date;
    }

    String getPrescriptionId() {
        return prescriptionId;
    }

    String getDisplayName() {
        return displayName;
    }

    String getDate() {
        return date;
    }
}
