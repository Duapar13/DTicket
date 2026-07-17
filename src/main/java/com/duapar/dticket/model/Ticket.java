package com.duapar.dticket.model;

import java.util.UUID;

public class Ticket {

    private final int id;
    private final UUID playerUUID;
    private final String playerName;
    private final TicketType type;
    private final String message;
    private TicketStatus status;
    private String adminResponse;
    private String respondedBy;
    private final long createdAt;
    private long updatedAt;

    public Ticket(int id, UUID playerUUID, String playerName, TicketType type, String message,
                  TicketStatus status, String adminResponse, String respondedBy, long createdAt, long updatedAt) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.type = type;
        this.message = message;
        this.status = status;
        this.adminResponse = adminResponse;
        this.respondedBy = respondedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public TicketType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public String getRespondedBy() {
        return respondedBy;
    }

    public void setRespondedBy(String respondedBy) {
        this.respondedBy = respondedBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isOpenOrAnswered() {
        return status != TicketStatus.CLOSED;
    }
}
