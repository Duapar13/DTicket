package com.duapar.dticket;

import com.duapar.dticket.commands.DTicketCommand;
import com.duapar.dticket.commands.TicketCommand;
import com.duapar.dticket.discord.DiscordWebhook;
import com.duapar.dticket.gui.GuiListener;
import com.duapar.dticket.integration.DAPIHook;
import com.duapar.dticket.manager.TicketManager;
import com.duapar.dticket.storage.MySQLTicketStorage;
import com.duapar.dticket.storage.TicketStorage;
import com.duapar.dticket.storage.YamlTicketStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DTicket extends JavaPlugin {

    private TicketStorage storage;
    private TicketManager ticketManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String storageType = getConfig().getString("storage.type", "local");
        if ("mysql".equalsIgnoreCase(storageType)) {
            storage = new MySQLTicketStorage(this,
                    getConfig().getString("storage.mysql.host", "localhost"),
                    getConfig().getInt("storage.mysql.port", 3306),
                    getConfig().getString("storage.mysql.database", "dticket"),
                    getConfig().getString("storage.mysql.username", "root"),
                    getConfig().getString("storage.mysql.password", ""),
                    getConfig().getBoolean("storage.mysql.useSSL", false));
        } else {
            storage = new YamlTicketStorage(getDataFolder(), getLogger());
        }

        try {
            storage.init();
        } catch (Exception e) {
            getLogger().severe("Impossible d'initialiser le stockage (" + storageType + "): " + e.getMessage());
            getLogger().severe("Le plugin va se désactiver.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ticketManager = new TicketManager(this, storage);
        ticketManager.loadConfig();

        try {
            ticketManager.seed(storage.loadTickets());
        } catch (Exception e) {
            getLogger().severe("Erreur lors du chargement des tickets existants: " + e.getMessage());
        }

        DiscordWebhook discordWebhook = new DiscordWebhook(this);

        DTicketCommand dticketCommand = new DTicketCommand(this, ticketManager, discordWebhook);
        PluginCommand dticket = getCommand("dticket");
        if (dticket != null) {
            dticket.setExecutor(dticketCommand);
            dticket.setTabCompleter(dticketCommand);
        }

        PluginCommand ticket = getCommand("ticket");
        if (ticket != null) {
            ticket.setExecutor(new TicketCommand(ticketManager));
        }

        getServer().getPluginManager().registerEvents(new GuiListener(this, ticketManager, discordWebhook), this);

        if (getServer().getPluginManager().isPluginEnabled("DAPI")) {
            DAPIHook.registerTicketService(this, ticketManager);
        } else {
            getLogger().info("DAPI non détecté : DTicket fonctionne en mode autonome (pas de TicketService partagé, pas d'affichage de faction).");
        }

        getLogger().info("DTicket activé (stockage: " + storageType + ").");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
    }
}
