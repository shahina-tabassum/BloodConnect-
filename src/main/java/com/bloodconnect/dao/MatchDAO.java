package com.bloodconnect.dao;

import com.bloodconnect.model.DonorMatch;
import com.bloodconnect.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the `donor_matches` table.
 */
public class MatchDAO {

    /**
     * Creates a match between a request and a donor.
     * Returns the generated match_id, or -1 on failure.
     */
    public int createMatch(int requestId, int donorId) throws SQLException {
        String sql = "INSERT INTO donor_matches (request_id, donor_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, requestId);
            ps.setInt(2, donorId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Gets all matches for a specific request, with donor details via JOIN.
     * Used by requester to see matched donors.
     */
    public List<DonorMatch> getMatchesByRequest(int requestId) throws SQLException {
        String sql = "SELECT dm.*, u.full_name AS donor_name, u.phone AS donor_phone, "
                   + "d.blood_group AS donor_blood_group, d.city AS donor_city "
                   + "FROM donor_matches dm "
                   + "JOIN users u ON dm.donor_id = u.user_id "
                   + "JOIN donor_profiles d ON dm.donor_id = d.donor_id "
                   + "WHERE dm.request_id = ? "
                   + "ORDER BY dm.matched_at DESC";

        List<DonorMatch> matches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRow(rs));
                }
            }
        }
        return matches;
    }

    /**
     * Gets all matches for a specific donor (incoming requests matched to them).
     * Used by donor dashboard.
     */
    public List<DonorMatch> getMatchesByDonor(int donorId) throws SQLException {
        String sql = "SELECT dm.*, u.full_name AS donor_name, u.phone AS donor_phone, "
                   + "d.blood_group AS donor_blood_group, d.city AS donor_city, "
                   + "br.patient_name, br.blood_group_needed, br.units_required, "
                   + "br.hospital_name, br.city AS request_city, br.urgency "
                   + "FROM donor_matches dm "
                   + "JOIN users u ON dm.donor_id = u.user_id "
                   + "JOIN donor_profiles d ON dm.donor_id = d.donor_id "
                   + "JOIN blood_requests br ON dm.request_id = br.request_id "
                   + "WHERE dm.donor_id = ? "
                   + "ORDER BY dm.matched_at DESC";

        List<DonorMatch> matches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRow(rs));
                }
            }
        }
        return matches;
    }

    /**
     * Updates the status of a match (ACCEPTED, DECLINED).
     */
    public void updateMatchStatus(int matchId, String status) throws SQLException {
        String sql = "UPDATE donor_matches SET status = ? WHERE match_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, matchId);
            ps.executeUpdate();
        }
    }

    /**
     * Checks if a match already exists between a request and donor.
     * Prevents duplicate matches on re-run.
     */
    public boolean matchExists(int requestId, int donorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM donor_matches WHERE request_id = ? AND donor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            ps.setInt(2, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private DonorMatch mapRow(ResultSet rs) throws SQLException {
        DonorMatch match = new DonorMatch();
        match.setMatchId(rs.getInt("match_id"));
        match.setRequestId(rs.getInt("request_id"));
        match.setDonorId(rs.getInt("donor_id"));
        match.setStatus(rs.getString("status"));
        match.setMatchedAt(rs.getTimestamp("matched_at"));

        // Transient fields from JOINs
        try {
            match.setDonorName(rs.getString("donor_name"));
            match.setDonorPhone(rs.getString("donor_phone"));
            match.setDonorBloodGroup(rs.getString("donor_blood_group"));
            match.setDonorCity(rs.getString("donor_city"));
        } catch (SQLException ignored) {
            // Not all queries include these JOIN columns
        }

        try {
            match.setPatientName(rs.getString("patient_name"));
            match.setBloodGroupNeeded(rs.getString("blood_group_needed"));
            match.setUnitsRequired(rs.getInt("units_required"));
            match.setHospitalName(rs.getString("hospital_name"));
            match.setRequestCity(rs.getString("request_city"));
            match.setUrgency(rs.getString("urgency"));
        } catch (SQLException ignored) {
            // Not all queries include request columns
        }

        return match;
    }
}
