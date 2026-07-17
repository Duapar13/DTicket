package com.duapar.dticket.storage;

import com.duapar.dticket.model.Ticket;
import com.duapar.dticket.model.TicketStatus;
import com.duapar.dticket.model.TicketType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLTicketStorage implements TicketStorage {

    private final JavaPlugin plugin;
    private final String url;
    private final String username;
    private final String password;

    private Connection connection;

    public MySQLTicketStorage(JavaPlugin plugin, String host, int port, String database,
                               String username, String password, boolean useSSL) {
        this.plugin = plugin;
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true&autoReconnect=true";
        this.username = username;
        this.password = password;
    }

    @Override
    public void init() throws Exception {
        Class.forName(com.mysql.cj.jdbc.Driver.class.getName());
        connect();
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS dticket_tickets (" +
                    "id INT PRIMARY KEY," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "type VARCHAR(16) NOT NULL," +
                    "message VARCHAR(1024) NOT NULL," +
                    "status VARCHAR(16) NOT NULL," +
                    "admin_response VARCHAR(1024)," +
                    "responded_by VARCHAR(16)," +
                    "created_at BIGINT NOT NULL," +
                    "updated_at BIGINT NOT NULL)");
        }
    }

    private void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
    }

    @Override
    public Map<Integer, Ticket> loadTickets() throws SQLException {
        Map<Integer, Ticket> result = new HashMap<>();
        connect();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM dticket_tickets")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                String playerName = rs.getString("player_name");
                TicketType type = TicketType.valueOf(rs.getString("type"));
                String message = rs.getString("message");
                TicketStatus status = TicketStatus.valueOf(rs.getString("status"));
                String adminResponse = rs.getString("admin_response");
                String respondedBy = rs.getString("responded_by");
                long createdAt = rs.getLong("created_at");
                long updatedAt = rs.getLong("updated_at");
                result.put(id, new Ticket(id, playerUUID, playerName, type, message, status,
                        adminResponse, respondedBy, createdAt, updatedAt));
            }
        }
        return result;
    }

    @Override
    public void saveTicket(Ticket ticket) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                connect();
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO dticket_tickets (id, player_uuid, player_name, type, message, status, " +
                                "admin_response, responded_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE status = VALUES(status), admin_response = VALUES(admin_response), " +
                                "responded_by = VALUES(responded_by), updated_at = VALUES(updated_at)")) {
                    ps.setInt(1, ticket.getId());
                    ps.setString(2, ticket.getPlayerUUID().toString());
                    ps.setString(3, ticket.getPlayerName());
                    ps.setString(4, ticket.getType().name());
                    ps.setString(5, ticket.getMessage());
                    ps.setString(6, ticket.getStatus().name());
                    ps.setString(7, ticket.getAdminResponse());
                    ps.setString(8, ticket.getRespondedBy());
                    ps.setLong(9, ticket.getCreatedAt());
                    ps.setLong(10, ticket.getUpdatedAt());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur MySQL lors de la sauvegarde du ticket #" + ticket.getId(), e);
            }
        });
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur lors de la fermeture de la connexion MySQL", e);
        }
    }
}
