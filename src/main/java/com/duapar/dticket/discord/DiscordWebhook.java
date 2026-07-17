package com.duapar.dticket.discord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;

/**
 * Envoie des notifications Discord (embed simple) via un webhook, sans dépendance
 * externe (java.net.http.HttpClient, intégré au JDK). Toujours envoyé de façon
 * asynchrone pour ne jamais bloquer le thread principal du serveur.
 */
public class DiscordWebhook {

    private final JavaPlugin plugin;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public DiscordWebhook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void send(String title, String description, int color) {
        String url = plugin.getConfig().getString("discord.webhook-url", "");
        if (url == null || url.isEmpty()) {
            return;
        }

        String payload = "{\"embeds\":[{\"title\":\"" + escape(title) + "\",\"description\":\""
                + escape(description) + "\",\"color\":" + color + "}]}";

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("URL de webhook Discord invalide: " + e.getMessage());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Échec de l'envoi du webhook Discord", e);
            }
        });
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
