package com.domain;

import com.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteQueueMapping {

    public boolean deleteMapping(long publisherId, String consumerQueueName, String eventType) {
        String query = "DELETE FROM queue_mapping WHERE publisher_id = ? AND consumer_queuename = ? AND event_type = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, publisherId);
            stmt.setString(2, consumerQueueName);
            stmt.setString(3, eventType);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
