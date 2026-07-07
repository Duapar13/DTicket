package com.dticket.gui;

import com.dticket.model.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.function.Function;

public final class TicketGuiBuilder {

    private TicketGuiBuilder() {
    }

    public static Inventory buildPlayerGui(List<Ticket> tickets) {
        TicketGuiHolder holder = new TicketGuiHolder(TicketGuiHolder.Mode.PLAYER);
        int size = Math.min(54, Math.max(9, ((tickets.size() + 8) / 9) * 9));
        Inventory inventory = Bukkit.createInventory(holder, size, ChatColor.DARK_PURPLE + "Tes tickets");
        holder.setInventory(inventory);

        int slot = 0;
        for (Ticket ticket : tickets) {
            if (slot >= size) {
                break;
            }
            inventory.setItem(slot, TicketItemFactory.buildPlayerItem(ticket));
            holder.put(slot, ticket.getId());
            slot++;
        }
        return inventory;
    }

    public static Inventory buildAdminGui(List<Ticket> tickets, Function<Ticket, String> factionLineProvider) {
        TicketGuiHolder holder = new TicketGuiHolder(TicketGuiHolder.Mode.ADMIN);
        int size = 54;
        Inventory inventory = Bukkit.createInventory(holder, size, ChatColor.DARK_RED + "Tickets (admin)");
        holder.setInventory(inventory);

        int slot = 0;
        for (Ticket ticket : tickets) {
            if (slot >= size) {
                break;
            }
            inventory.setItem(slot, TicketItemFactory.buildAdminItem(ticket, factionLineProvider.apply(ticket)));
            holder.put(slot, ticket.getId());
            slot++;
        }
        return inventory;
    }
}
