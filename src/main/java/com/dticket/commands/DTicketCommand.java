package com.dticket.commands;

import com.dticket.discord.DiscordWebhook;
import com.dticket.gui.TicketGuiBuilder;
import com.dticket.integration.DAPIHook;
import com.dticket.manager.TicketException;
import com.dticket.manager.TicketManager;
import com.dticket.model.Ticket;
import com.dticket.model.TicketType;
import com.dticket.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DTicketCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "cheat", "bug", "question", "suggestion", "report", "admin", "reply", "close", "help"
    );

    private final JavaPlugin plugin;
    private final TicketManager ticketManager;
    private final DiscordWebhook discordWebhook;

    public DTicketCommand(JavaPlugin plugin, TicketManager ticketManager, DiscordWebhook discordWebhook) {
        this.plugin = plugin;
        this.ticketManager = ticketManager;
        this.discordWebhook = discordWebhook;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        try {
            switch (sub) {
                case "admin":
                    handleAdmin(sender);
                    break;
                case "reply":
                    handleReply(sender, args);
                    break;
                case "close":
                    handleClose(sender, args);
                    break;
                case "help":
                    sendHelp(sender);
                    break;
                default:
                    handleCreate(sender, args);
                    break;
            }
        } catch (TicketException e) {
            Msg.error(sender, e.getMessage());
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (!player.hasPermission("dticket.use")) {
            throw new TicketException("Tu n'as pas la permission d'ouvrir un ticket.");
        }

        TicketType type;
        try {
            type = TicketType.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new TicketException("Catégorie inconnue: " + args[0]
                    + ". Utilise: cheat, bug, question, suggestion, report.");
        }
        if (args.length < 2) {
            throw new TicketException("Utilisation: /dticket " + args[0].toLowerCase(Locale.ROOT) + " <message>");
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Ticket ticket = ticketManager.create(player, type, message);
        Msg.success(player, "Ticket #" + ticket.getId() + " créé (" + type.getLabel()
                + "). Consulte /ticket dès qu'un admin répond.");

        if (plugin.getConfig().getBoolean("discord.notify-on-create", true)) {
            discordWebhook.send("Nouveau ticket #" + ticket.getId() + " (" + type.getLabel() + ")",
                    "Joueur: " + player.getName() + "\nMessage: " + message, 0xE74C3C);
        }
    }

    private void handleAdmin(CommandSender sender) {
        if (!sender.hasPermission("dticket.admin")) {
            throw new TicketException("Tu n'as pas la permission de gérer les tickets.");
        }
        Player player = requirePlayer(sender);
        boolean showFaction = plugin.getConfig().getBoolean("integration.show-faction-in-admin-gui", true);
        List<Ticket> tickets = ticketManager.getAll();
        Inventory gui = TicketGuiBuilder.buildAdminGui(tickets, ticket -> factionLineFor(ticket.getPlayerUUID(), showFaction));
        player.openInventory(gui);
    }

    private String factionLineFor(java.util.UUID playerId, boolean enabled) {
        if (!enabled || !plugin.getServer().getPluginManager().isPluginEnabled("DAPI")) {
            return null;
        }
        return DAPIHook.getFactionLine(playerId);
    }

    private void handleReply(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dticket.admin")) {
            throw new TicketException("Tu n'as pas la permission de répondre à un ticket.");
        }
        if (args.length < 3) {
            throw new TicketException("Utilisation: /dticket reply <id> <message>");
        }
        int id = parseId(args[1]);
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Ticket ticket = ticketManager.reply(sender, id, message);
        Msg.success(sender, "Réponse envoyée pour le ticket #" + id + ".");

        Player target = Bukkit.getPlayer(ticket.getPlayerUUID());
        if (target != null) {
            Msg.send(target, "Un admin a répondu à ton ticket #" + id + " ! Regarde /ticket pour voir la réponse.");
        }
        if (plugin.getConfig().getBoolean("discord.notify-on-reply", true)) {
            discordWebhook.send("Réponse au ticket #" + id,
                    "Admin: " + sender.getName() + "\nRéponse: " + message, 0xF1C40F);
        }
    }

    private void handleClose(CommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new TicketException("Utilisation: /dticket close <id>");
        }
        int id = parseId(args[1]);

        Ticket ticket;
        if (sender.hasPermission("dticket.admin")) {
            ticket = ticketManager.closeAsAdmin(id);
        } else {
            ticket = ticketManager.closeAsPlayer(requirePlayer(sender), id);
        }
        Msg.success(sender, "Ticket #" + id + " fermé.");

        if (plugin.getConfig().getBoolean("discord.notify-on-close", true)) {
            discordWebhook.send("Ticket fermé (#" + id + ")",
                    sender.getName() + " a fermé le ticket de " + ticket.getPlayerName() + ".", 0x95A5A6);
        }
    }

    private int parseId(String raw) {
        String cleaned = raw.startsWith("#") ? raw.substring(1) : raw;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            throw new TicketException("Identifiant de ticket invalide: " + raw);
        }
    }

    private Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            throw new TicketException("Seul un joueur peut utiliser cette commande.");
        }
        return (Player) sender;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + "==== " + ChatColor.BLUE + "DTicket" + ChatColor.DARK_GRAY + " ====");
        sender.sendMessage(ChatColor.GOLD + "/dticket <cheat|bug|question|suggestion|report> <message>"
                + ChatColor.GRAY + " - Ouvrir un ticket.");
        sender.sendMessage(ChatColor.GOLD + "/ticket" + ChatColor.GRAY + " - Consulter tes tickets.");
        sender.sendMessage(ChatColor.GOLD + "/dticket close <id>" + ChatColor.GRAY + " - Fermer un de tes tickets.");
        if (sender.hasPermission("dticket.admin")) {
            sender.sendMessage(ChatColor.GOLD + "/dticket admin" + ChatColor.GRAY + " - Voir tous les tickets (GUI).");
            sender.sendMessage(ChatColor.GOLD + "/dticket reply <id> <message>" + ChatColor.GRAY + " - Répondre à un ticket.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(partial)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
