package com.dticket.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class TicketGuiHolder implements InventoryHolder {

    public enum Mode {
        PLAYER,
        ADMIN
    }

    private final Mode mode;
    private final Map<Integer, Integer> slotToTicketId = new HashMap<>();
    private Inventory inventory;

    public TicketGuiHolder(Mode mode) {
        this.mode = mode;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Mode getMode() {
        return mode;
    }

    public void put(int slot, int ticketId) {
        slotToTicketId.put(slot, ticketId);
    }

    public Integer getTicketId(int slot) {
        return slotToTicketId.get(slot);
    }
}
