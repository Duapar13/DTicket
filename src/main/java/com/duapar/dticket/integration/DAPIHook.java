package com.duapar.dticket.integration;

import com.duapar.dapi.DAPI;
import com.duapar.dapi.service.FactionService;
import com.duapar.dapi.service.TicketService;
import com.duapar.dticket.manager.TicketManager;
import com.duapar.dticket.service.DTicketServiceImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Isole tout le code qui référence les classes de DAPI. Ne doit être chargée
 * (donc jamais référencée en dehors d'un bloc gardé par une vérification de
 * présence de DAPI) que si le plugin DAPI est effectivement installé - sinon la
 * JVM lèverait une NoClassDefFoundError dès qu'une méthode d'ici serait invoquée.
 */
public final class DAPIHook {

    private DAPIHook() {
    }

    public static void registerTicketService(JavaPlugin plugin, TicketManager ticketManager) {
        DAPI.registerPlugin(plugin, "TicketService");
        DAPI.registerService(TicketService.class, new DTicketServiceImpl(ticketManager), plugin);
    }

    /**
     * @return une ligne du style "Faction: NomDeLaFaction" (ou "Faction: aucune"),
     * ou {@code null} si aucun plugin ne fournit FactionService (ex: DFaction non installé).
     */
    public static String getFactionLine(UUID playerId) {
        FactionService factionService = DAPI.getService(FactionService.class);
        if (factionService == null) {
            return null;
        }
        String factionName = factionService.getFactionName(playerId);
        return "Faction: " + (factionName != null ? factionName : "aucune");
    }
}
