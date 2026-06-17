package com.bloodconnect.dao;

import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the `blood_requests` table.
 */
public class RequestDAO {

    /**
     * Creates a new blood request. Returns the generated request_id, or -1 on failure.
     */
    public int createRequest(BloodRequest req) throws SQLException {
        String sql = "INSERT INTO blood_requests (requester_id, patient_name, blood_group_needed, "
                   + "units_required, hospital_name, city, urgency) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, req.getRequesterId());
            ps.setString(2, req.getPatientName());
            ps.setString(3, req.getBloodGroupNeeded());
            ps.setInt(4, req.getUnitsRequired());
            ps.setString(5, req.getHospitalName());
            ps.setString(6, req.getCity());
            ps.setString(7, req.getUrgency());

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
     * Gets a request by ID.
     */
    public BloodRequest getRequestById(int requestId) throws SQLException {
        String sql = "SELECT br.*, u.full_name AS requester_name FROM blood_requests br "
                   + "JOIN users u ON br.requester_id = u.user_id "
                   + "WHERE br.request_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Gets all requests by a specific requester.
     */
    public List<BloodRequest> getRequestsByRequester(int requesterId) throws SQLException {
        String sql = "SELECT br.*, u.full_name AS requester_name FROM blood_requests br "
                   + "JOIN users u ON br.requester_id = u.user_id "
                   + "WHERE br.requester_id = ? ORDER BY br.created_at DESC";

        List<BloodRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRow(rs));
                }
            }
        }
        return requests;
    }

    /**
     * Gets all open/pending requests. Used by admin dashboard.
     */
    public List<BloodRequest> getAllRequests() throws SQLException {
        String sql = "SELECT br.*, u.full_name AS requester_name FROM blood_requests br "
                   + "JOIN users u ON br.requester_id = u.user_id "
                   + "ORDER BY FIELD(br.urgency, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'), br.created_at DESC";

        List<BloodRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requests.add(mapRow(rs));
            }
        }
        return requests;
    }

    /**
     * Gets only unverified (pending) requests for admin verification queue.
     */
    public List<BloodRequest> getPendingVerifications() throws SQLException {
        String sql = "SELECT br.*, u.full_name AS requester_name FROM blood_requests br "
                   + "JOIN users u ON br.requester_id = u.user_id "
                   + "WHERE br.is_verified = FALSE AND br.status != 'CLOSED' "
                   + "ORDER BY FIELD(br.urgency, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'), br.created_at ASC";

        List<BloodRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requests.add(mapRow(rs));
            }
        }
        return requests;
    }

    /**
     * Verifies (approves) a request. Sets is_verified=TRUE and records the admin who verified.
     */
    public void verifyRequest(int requestId, int adminId) throws SQLException {
        String sql = "UPDATE blood_requests SET is_verified = TRUE, verified_by = ? WHERE request_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the status of a request (OPEN, MATCHED, FULFILLED, CLOSED).
     */
    public void updateStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE blood_requests SET status = ? WHERE request_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    private BloodRequest mapRow(ResultSet rs) throws SQLException {
        BloodRequest req = new BloodRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setRequesterId(rs.getInt("requester_id"));
        req.setPatientName(rs.getString("patient_name"));
        req.setBloodGroupNeeded(rs.getString("blood_group_needed"));
        req.setUnitsRequired(rs.getInt("units_required"));
        req.setHospitalName(rs.getString("hospital_name"));
        req.setCity(rs.getString("city"));
        req.setUrgency(rs.getString("urgency"));
        req.setStatus(rs.getString("status"));
        req.setVerified(rs.getBoolean("is_verified"));
        int verifiedBy = rs.getInt("verified_by");
        req.setVerifiedBy(rs.wasNull() ? null : verifiedBy);
        req.setCreatedAt(rs.getTimestamp("created_at"));

        // Transient field from JOIN
        try {
            req.setRequesterName(rs.getString("requester_name"));
        } catch (SQLException ignored) {
            // Column not present in all queries
        }

        return req;
    }
}
