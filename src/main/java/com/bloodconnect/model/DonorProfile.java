package com.bloodconnect.model;

import java.sql.Date;

/**
 * Represents a donor's medical/location profile.
 * Maps to the `donor_profiles` table.
 * Linked 1:1 with a User where role = 'DONOR'.
 */
public class DonorProfile {

    private int donorId;
    private String bloodGroup;
    private Integer age;
    private String gender; // M, F, OTHER
    private String city;
    private String pincode;
    private Date lastDonationDate;
    private boolean isAvailable;

    public DonorProfile() {
        this.isAvailable = true;
    }

    // --- Getters and Setters ---

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Date getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(Date lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
