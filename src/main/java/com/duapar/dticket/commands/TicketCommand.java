package com.duapar.dticket.commands;

import com.duapar.dticket.gui.TicketGuiBuilder;
import com.duapar.dticket.manager.TicketManager;
import com.duapar.dticket.model.Ticket;
import com.duapar.dticket.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TicketCommand implements CommandExecutor {

    private final TicketManager ticketManager;

    public TicketCommand(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Msg.error(sender, "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("dticket.use")) {
            Msg.error(player, "Tu n'as pas la permission de consulter tes tickets.");
            return true;
        }

        List<Ticket> tickets = ticketManager.getForPlayer(player.getUniqueId());
        if (tickets.isEmpty()) {
            Msg.send(player, "Tu n'as aucun ticket. Ouvre-en un avec /dticket <categorie> <message>.");
            return true;
        }
        player.openInventory(TicketGuiBuilder.buildPlayerGui(tickets));
        return true;
    }
}
