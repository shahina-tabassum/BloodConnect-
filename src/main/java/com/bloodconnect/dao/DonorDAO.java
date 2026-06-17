package com.bloodconnect.dao;

import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the `donor_profiles` table.
 * Includes the core matching query with 90-day eligibility check.
 */
public class DonorDAO {

    /**
     * Creates a new donor profile (called during donor registration).
     */
    public void createProfile(DonorProfile dp) throws SQLException {
        String sql = "INSERT INTO donor_profiles (donor_id, blood_group, age, gender, city, pincode, last_donation_date, is_available) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dp.getDonorId());
            ps.setString(2, dp.getBloodGroup());
            if (dp.getAge() != null) {
                ps.setInt(3, dp.getAge());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, dp.getGender());
            ps.setString(5, dp.getCity());
            ps.setString(6, dp.getPincode());
            if (dp.getLastDonationDate() != null) {
                ps.setDate(7, dp.getLastDonationDate());
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setBoolean(8, dp.isAvailable());

            ps.executeUpdate();
        }
    }

    /**
     * Gets a donor's profile by donor_id.
     */
    public DonorProfile getProfile(int donorId) throws SQLException {
        String sql = "SELECT * FROM donor_profiles WHERE donor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Updates a donor's profile.
     */
    public void updateProfile(DonorProfile dp) throws SQLException {
        String sql = "UPDATE donor_profiles SET blood_group = ?, age = ?, gender = ?, "
                   + "city = ?, pincode = ?, last_donation_date = ?, is_available = ? "
                   + "WHERE donor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dp.getBloodGroup());
            if (dp.getAge() != null) {
                ps.setInt(2, dp.getAge());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, dp.getGender());
            ps.setString(4, dp.getCity());
            ps.setString(5, dp.getPincode());
            if (dp.getLastDonationDate() != null) {
                ps.setDate(6, dp.getLastDonationDate());
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setBoolean(7, dp.isAvailable());
            ps.setInt(8, dp.getDonorId());

            ps.executeUpdate();
        }
    }

    /**
     * Toggles a donor's availability status.
     */
    public void toggleAvailability(int donorId, boolean available) throws SQLException {
        String sql = "UPDATE donor_profiles SET is_available = ? WHERE donor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, donorId);
            ps.executeUpdate();
        }
    }

    /**
     * Core matching query — finds eligible donors by blood group and city.
     * 
     * Eligibility rules:
     * 1. Same blood group (exact match)
     * 2. Same city (case-insensitive via LOWER() backstop)
     * 3. is_available = TRUE
     * 4. last_donation_date is NULL OR > 90 days ago
     * 
     * Returns DonorProfile objects with donorId populated
     * (caller joins with User data for contact info).
     */
    public List<DonorProfile> findEligibleDonors(String bloodGroup, String city) throws SQLException {
        String sql = "SELECT d.*, u.full_name, u.phone FROM donor_profiles d "
                   + "JOIN users u ON d.donor_id = u.user_id "
                   + "WHERE d.blood_group = ? "
                   + "AND LOWER(d.city) = LOWER(?) "
                   + "AND d.is_available = TRUE "
                   + "AND (d.last_donation_date IS NULL OR DATEDIFF(CURDATE(), d.last_donation_date) > 90)";

        List<DonorProfile> donors = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bloodGroup);
            ps.setString(2, city);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    donors.add(mapRow(rs));
                }
            }
        }
        return donors;
    }

    /**
     * Gets all donor profiles. Used by admin dashboard.
     */
    public List<DonorProfile> getAllDonors() throws SQLException {
        String sql = "SELECT * FROM donor_profiles";
        List<DonorProfile> donors = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                donors.add(mapRow(rs));
            }
        }
        return donors;
    }

    private DonorProfile mapRow(ResultSet rs) throws SQLException {
        DonorProfile dp = new DonorProfile();
        dp.setDonorId(rs.getInt("donor_id"));
        dp.setBloodGroup(rs.getString("blood_group"));
        dp.setAge(rs.getObject("age") != null ? rs.getInt("age") : null);
        dp.setGender(rs.getString("gender"));
        dp.setCity(rs.getString("city"));
        dp.setPincode(rs.getString("pincode"));
        dp.setLastDonationDate(rs.getDate("last_donation_date"));
        dp.setAvailable(rs.getBoolean("is_available"));
        return dp;
    }
}
