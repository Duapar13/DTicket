package com.dticket.manager;

import com.dticket.model.Ticket;
import com.dticket.model.TicketStatus;
import com.dticket.model.TicketType;
import com.dticket.storage.TicketStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketManager {

    private final JavaPlugin plugin;
    private final TicketStorage storage;

    private final Map<Integer, Ticket> tickets = new HashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    private int maxOpenTicketsPerPlayer;

    public TicketManager(JavaPlugin plugin, TicketStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        this.maxOpenTicketsPerPlayer = cfg.getInt("limits.max-open-tickets-per-player", 3);
    }

    public void seed(Map<Integer, Ticket> loaded) {
        tickets.clear();
        tickets.putAll(loaded);
        int maxId = 0;
        for (int id : loaded.keySet()) {
            maxId = Math.max(maxId, id);
        }
        nextId.set(maxId + 1);
    }

    public Ticket create(Player player, TicketType type, String message) {
        int openCount = countOpen(player.getUniqueId());
        if (openCount >= maxOpenTicketsPerPlayer) {
            throw new TicketException("Tu as déjà " + openCount + " tickets ouverts (max "
                    + maxOpenTicketsPerPlayer + "). Attends une réponse ou ferme-en un avec /ticket.");
        }
        int id = nextId.getAndIncrement();
        long now = System.currentTimeMillis();
        Ticket ticket = new Ticket(id, player.getUniqueId(), player.getName(), type, message,
                TicketStatus.OPEN, null, null, now, now);
        tickets.put(id, ticket);
        storage.saveTicket(ticket);
        return ticket;
    }

    public Ticket reply(CommandSender admin, int id, String message) {
        Ticket ticket = getOrThrow(id);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new TicketException("Ce ticket est fermé, impossible d'y répondre.");
        }
        ticket.setAdminResponse(message);
        ticket.setRespondedBy(admin.getName());
        ticket.setStatus(TicketStatus.ANSWERED);
        ticket.setUpdatedAt(System.currentTimeMillis());
        storage.saveTicket(ticket);
        return ticket;
    }

    public Ticket closeAsAdmin(int id) {
        Ticket ticket = getOrThrow(id);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new TicketException("Ce ticket est déjà fermé.");
        }
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setUpdatedAt(System.currentTimeMillis());
        storage.saveTicket(ticket);
        return ticket;
    }

    public Ticket closeAsPlayer(Player player, int id) {
        Ticket ticket = getOrThrow(id);
        if (!ticket.getPlayerUUID().equals(player.getUniqueId())) {
            throw new TicketException("Ce n'est pas ton ticket.");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new TicketException("Ce ticket est déjà fermé.");
        }
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setUpdatedAt(System.currentTimeMillis());
        storage.saveTicket(ticket);
        return ticket;
    }

    public Ticket getOrThrow(int id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new TicketException("Ticket introuvable: #" + id);
        }
        return ticket;
    }

    public int countOpen(UUID playerId) {
        int count = 0;
        for (Ticket ticket : tickets.values()) {
            if (ticket.getPlayerUUID().equals(playerId) && ticket.isOpenOrAnswered()) {
                count++;
            }
        }
        return count;
    }

    public List<Ticket> getForPlayer(UUID playerId) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket.getPlayerUUID().equals(playerId)) {
                result.add(ticket);
            }
        }
        result.sort(Comparator.comparingLong(Ticket::getCreatedAt).reversed());
        return result;
    }

    public List<Ticket> getAll() {
        List<Ticket> result = new ArrayList<>(tickets.values());
        result.sort(Comparator
                .comparing((Ticket t) -> t.getStatus() == TicketStatus.CLOSED)
                .thenComparing(Comparator.comparingLong(Ticket::getCreatedAt).reversed()));
        return result;
    }
}
