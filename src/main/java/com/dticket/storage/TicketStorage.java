package com.dticket.storage;

import com.dticket.model.Ticket;

import java.util.Map;

public interface TicketStorage {

    void init() throws Exception;

    /**
     * Charge tous les tickets connus. Clé de la map = identifiant du ticket.
     */
    Map<Integer, Ticket> loadTickets() throws Exception;

    void saveTicket(Ticket ticket);

    void close();
}
