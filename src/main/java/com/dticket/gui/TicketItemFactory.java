package com.dticket.gui;

import com.dticket.model.Ticket;
import com.dticket.model.TicketStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

final class TicketItemFactory {

    private static final int WRAP_LENGTH = 40;

    private TicketItemFactory() {
    }

    static ItemStack buildPlayerItem(Ticket ticket) {
        ItemStack item = new ItemStack(ticket.getType().getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(statusColor(ticket.getStatus()) + "#" + ticket.getId() + " - " + ticket.getType().getLabel());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Statut: " + statusLabel(ticket.getStatus()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Ton message:");
        for (String line : wrap(ticket.getMessage())) {
            lore.add(ChatColor.WHITE + line);
        }
        appendResponse(lore, ticket);
        lore.add("");
        lore.add(ticket.getStatus() != TicketStatus.CLOSED
                ? ChatColor.RED + "Clique pour fermer ce ticket."
                : ChatColor.DARK_GRAY + "Ticket fermé.");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack buildAdminItem(Ticket ticket, String factionLine) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(ticket.getPlayerUUID()));
        meta.setDisplayName(statusColor(ticket.getStatus()) + "#" + ticket.getId() + " - " + ticket.getType().getLabel()
                + ChatColor.GRAY + " (" + ticket.getPlayerName() + ")");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Statut: " + statusLabel(ticket.getStatus()));
        if (factionLine != null) {
            lore.add(ChatColor.GRAY + factionLine);
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Message:");
        for (String line : wrap(ticket.getMessage())) {
            lore.add(ChatColor.WHITE + line);
        }
        appendResponse(lore, ticket);
        lore.add("");
        lore.add(ChatColor.AQUA + "Clique pour voir les commandes disponibles.");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static void appendResponse(List<String> lore, Ticket ticket) {
        if (ticket.getAdminResponse() != null && !ticket.getAdminResponse().isEmpty()) {
            lore.add("");
            lore.add(ChatColor.GOLD + "Réponse de " + ticket.getRespondedBy() + ":");
            for (String line : wrap(ticket.getAdminResponse())) {
                lore.add(ChatColor.YELLOW + line);
            }
        }
    }

    static String statusLabel(TicketStatus status) {
        switch (status) {
            case OPEN:
                return ChatColor.RED + "Ouvert";
            case ANSWERED:
                return ChatColor.GOLD + "Répondu";
            case CLOSED:
                return ChatColor.GREEN + "Fermé";
            default:
                return status.name();
        }
    }

    private static ChatColor statusColor(TicketStatus status) {
        switch (status) {
            case OPEN:
                return ChatColor.RED;
            case ANSWERED:
                return ChatColor.GOLD;
            case CLOSED:
                return ChatColor.GRAY;
            default:
                return ChatColor.WHITE;
        }
    }

    private static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            if (current.length() + word.length() + 1 > WRAP_LENGTH) {
                lines.add(current.toString());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(word);
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
