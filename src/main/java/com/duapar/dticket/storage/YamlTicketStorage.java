package com.duapar.dticket.storage;

import com.duapar.dticket.model.Ticket;
import com.duapar.dticket.model.TicketStatus;
import com.duapar.dticket.model.TicketType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlTicketStorage implements TicketStorage {

    private final File file;
    private final Logger logger;
    private YamlConfiguration config;

    public YamlTicketStorage(File dataFolder, Logger logger) {
        this.file = new File(new File(dataFolder, "data"), "tickets.yml");
        this.logger = logger;
    }

    @Override
    public void init() throws IOException {
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Impossible de créer le dossier de données " + dir);
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Impossible de créer " + file);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public Map<Integer, Ticket> loadTickets() {
        Map<Integer, Ticket> result = new HashMap<>();
        ConfigurationSection root = config.getConfigurationSection("tickets");
        if (root == null) {
            return result;
        }
        for (String idStr : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(idStr);
            if (section == null) continue;
            try {
                int id = Integer.parseInt(idStr);
                UUID playerUUID = UUID.fromString(section.getString("playerUUID"));
                String playerName = section.getString("playerName", "Inconnu");
                TicketType type = TicketType.valueOf(section.getString("type"));
                String message = section.getString("message", "");
                TicketStatus status = TicketStatus.valueOf(section.getString("status", "OPEN"));
                String adminResponse = section.getString("adminResponse", "");
                String respondedBy = section.getString("respondedBy", "");
                long createdAt = section.getLong("createdAt", System.currentTimeMillis());
                long updatedAt = section.getLong("updatedAt", createdAt);

                Ticket ticket = new Ticket(id, playerUUID, playerName, type, message, status,
                        adminResponse.isEmpty() ? null : adminResponse,
                        respondedBy.isEmpty() ? null : respondedBy,
                        createdAt, updatedAt);
                result.put(id, ticket);
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Ticket invalide ignoré (id=" + idStr + "): " + ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public synchronized void saveTicket(Ticket ticket) {
        String base = "tickets." + ticket.getId();
        config.set(base + ".playerUUID", ticket.getPlayerUUID().toString());
        config.set(base + ".playerName", ticket.getPlayerName());
        config.set(base + ".type", ticket.getType().name());
        config.set(base + ".message", ticket.getMessage());
        config.set(base + ".status", ticket.getStatus().name());
        config.set(base + ".adminResponse", ticket.getAdminResponse() == null ? "" : ticket.getAdminResponse());
        config.set(base + ".respondedBy", ticket.getRespondedBy() == null ? "" : ticket.getRespondedBy());
        config.set(base + ".createdAt", ticket.getCreatedAt());
        config.set(base + ".updatedAt", ticket.getUpdatedAt());
        save();
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Impossible de sauvegarder " + file, e);
        }
    }
}
