package com.bloodconnect.model;

import java.sql.Timestamp;

/**
 * Represents a blood request posted by a requester.
 * Maps to the `blood_requests` table.
 */
public class BloodRequest {

    private int requestId;
    private int requesterId;
    private String patientName;
    private String bloodGroupNeeded;
    private int unitsRequired;
    private String hospitalName;
    private String city;
    private String urgency; // LOW, MEDIUM, HIGH, CRITICAL
    private String status;  // OPEN, MATCHED, FULFILLED, CLOSED
    private boolean isVerified;
    private Integer verifiedBy; // nullable — admin user_id
    private Timestamp createdAt;

    // Transient field for display — requester's name (not persisted)
    private String requesterName;

    public BloodRequest() {
        this.unitsRequired = 1;
        this.urgency = "MEDIUM";
        this.status = "OPEN";
        this.isVerified = false;
    }

    // --- Getters and Setters ---

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getBloodGroupNeeded() {
        return bloodGroupNeeded;
    }

    public void setBloodGroupNeeded(String bloodGroupNeeded) {
        this.bloodGroupNeeded = bloodGroupNeeded;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public Integer getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Integer verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    // Transient field for display — matching records
    private java.util.List<com.bloodconnect.model.DonorMatch> matches;

    public java.util.List<com.bloodconnect.model.DonorMatch> getMatches() {
        return matches;
    }

    public void setMatches(java.util.List<com.bloodconnect.model.DonorMatch> matches) {
        this.matches = matches;
    }
}
