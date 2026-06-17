package com.bloodconnect.model;

import java.sql.Timestamp;

/**
 * Represents a match between a blood request and a donor.
 * Maps to the `donor_matches` table.
 */
public class DonorMatch {

    private int matchId;
    private int requestId;
    private int donorId;
    private String status; // PENDING, ACCEPTED, DECLINED
    private Timestamp matchedAt;

    // Transient fields for display (populated by JOINs, not persisted)
    private String donorName;
    private String donorPhone;
    private String donorBloodGroup;
    private String donorCity;

    // Transient request fields for donor view
    private String patientName;
    private String bloodGroupNeeded;
    private String hospitalName;
    private String requestCity;
    private String urgency;
    private Integer unitsRequired;

    public DonorMatch() {
        this.status = "PENDING";
    }

    // --- Getters and Setters ---

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(Timestamp matchedAt) {
        this.matchedAt = matchedAt;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public String getDonorBloodGroup() {
        return donorBloodGroup;
    }

    public void setDonorBloodGroup(String donorBloodGroup) {
        this.donorBloodGroup = donorBloodGroup;
    }

    public String getDonorCity() {
        return donorCity;
    }

    public void setDonorCity(String donorCity) {
        this.donorCity = donorCity;
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

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getRequestCity() {
        return requestCity;
    }

    public void setRequestCity(String requestCity) {
        this.requestCity = requestCity;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }
}
