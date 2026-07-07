package com.dticket.model;

import org.bukkit.Material;

public enum TicketType {

    CHEAT("Triche", Material.BARRIER),
    BUG("Bug", Material.REDSTONE),
    QUESTION("Question", Material.PAPER),
    SUGGESTION("Suggestion", Material.WRITABLE_BOOK),
    REPORT("Signalement", Material.IRON_SWORD);

    private final String label;
    private final Material icon;

    TicketType(String label, Material icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public Material getIcon() {
        return icon;
    }
}
