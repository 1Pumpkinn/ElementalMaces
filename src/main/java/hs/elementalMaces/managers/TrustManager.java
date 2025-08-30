package hs.elementalMaces.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class TrustManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>();
    private final Map<UUID, Set<UUID>> pendingTrustRequests = new HashMap<>();

    public TrustManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean sendTrustRequest(Player sender, Player target) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (isTrusted(sender, target)) {
            return false; // Already trusted
        }

        pendingTrustRequests.computeIfAbsent(targetUUID, k -> new HashSet<>()).add(senderUUID);
        return true;
    }

    public boolean acceptTrustRequest(Player accepter, Player requester) {
        UUID accepterUUID = accepter.getUniqueId();
        UUID requesterUUID = requester.getUniqueId();

        Set<UUID> pendingRequests = pendingTrustRequests.get(accepterUUID);
        if (pendingRequests == null || !pendingRequests.contains(requesterUUID)) {
            return false; // No pending request
        }

        // Add both players to each other's trust lists
        trustedPlayers.computeIfAbsent(accepterUUID, k -> new HashSet<>()).add(requesterUUID);
        trustedPlayers.computeIfAbsent(requesterUUID, k -> new HashSet<>()).add(accepterUUID);

        // Remove pending request
        pendingRequests.remove(requesterUUID);

        return true;
    }

    public boolean removeTrust(Player player, Player target) {
        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Set<UUID> playerTrusted = trustedPlayers.get(playerUUID);
        Set<UUID> targetTrusted = trustedPlayers.get(targetUUID);

        boolean removed = false;

        if (playerTrusted != null) {
            removed = playerTrusted.remove(targetUUID);
        }

        if (targetTrusted != null) {
            targetTrusted.remove(playerUUID);
        }

        return removed;
    }

    public boolean isTrusted(Player player1, Player player2) {
        UUID uuid1 = player1.getUniqueId();
        UUID uuid2 = player2.getUniqueId();

        Set<UUID> trusted = trustedPlayers.get(uuid1);
        return trusted != null && trusted.contains(uuid2);
    }

    public Set<UUID> getTrustedPlayers(Player player) {
        return trustedPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public Set<UUID> getPendingRequests(Player player) {
        return pendingTrustRequests.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public boolean hasPendingRequest(Player from, Player to) {
        Set<UUID> pending = pendingTrustRequests.get(to.getUniqueId());
        return pending != null && pending.contains(from.getUniqueId());
    }
}