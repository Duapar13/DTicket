package com.dticket.manager;

/**
 * Erreur "attendue" (mauvais usage, permission, état invalide...) destinée à être
 * affichée telle quelle à l'utilisateur par la commande qui l'a déclenchée.
 */
public class TicketException extends RuntimeException {

    public TicketException(String message) {
        super(message);
    }
}
