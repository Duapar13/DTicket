package com.dticket.service;

import com.dapi.service.TicketService;
import com.dticket.manager.TicketManager;
import com.dticket.model.TicketType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

/**
 * Implémentation de TicketService (contrat DAPI) adossée à TicketManager, pour
 * qu'un autre plugin D(nom) (ex: un futur anti-cheat) puisse ouvrir un ticket
 * automatiquement sans dépendre de DTicket.
 */
public class DTicketServiceImpl implements TicketService {

    private final TicketManager ticketManager;

    public DTicketServiceImpl(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    @Override
    public int createTicket(UUID playerId, String playerName, String type, String message) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            throw new IllegalStateException("Le joueur " + playerName + " n'est pas en ligne.");
        }
        TicketType ticketType;
        try {
            ticketType = TicketType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            ticketType = TicketType.QUESTION;
        }
        return ticketManager.create(player, ticketType, message).getId();
    }

    @Override
    public int countOpenTickets(UUID playerId) {
        return ticketManager.countOpen(playerId);
    }
}
