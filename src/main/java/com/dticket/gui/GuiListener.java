package com.dticket.gui;

import com.dticket.discord.DiscordWebhook;
import com.dticket.manager.TicketException;
import com.dticket.manager.TicketManager;
import com.dticket.model.Ticket;
import com.dticket.model.TicketStatus;
import com.dticket.util.Msg;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiListener implements Listener {

    private final JavaPlugin plugin;
    private final TicketManager ticketManager;
    private final DiscordWebhook discordWebhook;

    public GuiListener(JavaPlugin plugin, TicketManager ticketManager, DiscordWebhook discordWebhook) {
        this.plugin = plugin;
        this.ticketManager = ticketManager;
        this.discordWebhook = discordWebhook;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TicketGuiHolder)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
            return;
        }

        TicketGuiHolder holder = (TicketGuiHolder) event.getInventory().getHolder();
        Integer ticketId = holder.getTicketId(event.getSlot());
        if (ticketId == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (holder.getMode() == TicketGuiHolder.Mode.PLAYER) {
            handlePlayerClick(player, ticketId);
        } else {
            handleAdminClick(player, ticketId);
        }
    }

    private void handlePlayerClick(Player player, int ticketId) {
        try {
            Ticket ticket = ticketManager.getOrThrow(ticketId);
            if (ticket.getStatus() == TicketStatus.CLOSED) {
                Msg.send(player, "Ce ticket est déjà fermé.");
                return;
            }
            ticketManager.closeAsPlayer(player, ticketId);
            player.closeInventory();
            Msg.success(player, "Ticket #" + ticketId + " fermé.");

            if (plugin.getConfig().getBoolean("discord.notify-on-close", true)) {
                discordWebhook.send("Ticket fermé (#" + ticketId + ")",
                        ticket.getPlayerName() + " a fermé son propre ticket.", 0x95A5A6);
            }
        } catch (TicketException e) {
            Msg.error(player, e.getMessage());
        }
    }

    private void handleAdminClick(Player admin, int ticketId) {
        try {
            Ticket ticket = ticketManager.getOrThrow(ticketId);
            admin.closeInventory();

            admin.sendMessage(ChatColor.DARK_GRAY + "==== " + ChatColor.AQUA + "Ticket #" + ticket.getId() + ChatColor.DARK_GRAY + " ====");
            admin.sendMessage(ChatColor.GRAY + "Joueur: " + ChatColor.WHITE + ticket.getPlayerName());
            admin.sendMessage(ChatColor.GRAY + "Catégorie: " + ChatColor.WHITE + ticket.getType().getLabel());
            admin.sendMessage(ChatColor.GRAY + "Statut: " + TicketItemFactory.statusLabel(ticket.getStatus()));
            admin.sendMessage(ChatColor.GRAY + "Message: " + ChatColor.WHITE + ticket.getMessage());
            if (ticket.getAdminResponse() != null && !ticket.getAdminResponse().isEmpty()) {
                admin.sendMessage(ChatColor.GRAY + "Réponse actuelle (" + ticket.getRespondedBy() + "): "
                        + ChatColor.YELLOW + ticket.getAdminResponse());
            }
            admin.sendMessage(ChatColor.GRAY + "Répondre: " + ChatColor.GOLD + "/dticket reply " + ticket.getId() + " <message>");
            admin.sendMessage(ChatColor.GRAY + "Fermer: " + ChatColor.GOLD + "/dticket close " + ticket.getId());
        } catch (TicketException e) {
            Msg.error(admin, e.getMessage());
        }
    }
}
