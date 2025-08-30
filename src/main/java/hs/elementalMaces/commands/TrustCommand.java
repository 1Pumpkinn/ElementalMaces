package hs.elementalMaces.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import hs.elementalMaces.managers.TrustManager;

import java.util.Set;
import java.util.UUID;

public class TrustCommand implements CommandExecutor {

    private final TrustManager trustManager;

    public TrustCommand(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "trust":
                return handleTrustCommand(player, args);
            case "untrust":
                return handleUntrustCommand(player, args);
            case "trustlist":
                return handleTrustListCommand(player);
            case "trustaccept":
                return handleTrustAcceptCommand(player, args);
        }

        return false;
    }

    private boolean handleTrustCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /trust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot trust yourself!");
            return true;
        }

        if (trustManager.isTrusted(player, target)) {
            player.sendMessage(ChatColor.YELLOW + "You are already trusted with " + target.getName());
            return true;
        }

        if (trustManager.hasPendingRequest(player, target)) {
            player.sendMessage(ChatColor.YELLOW + "You already have a pending trust request with " + target.getName());
            return true;
        }

        if (trustManager.sendTrustRequest(player, target)) {
            player.sendMessage(ChatColor.GREEN + "Trust request sent to " + target.getName());
            target.sendMessage(ChatColor.YELLOW + player.getName() + " wants to trust you!");
            target.sendMessage(ChatColor.YELLOW + "Type /trustaccept " + player.getName() + " to accept");
        }

        return true;
    }

    private boolean handleTrustAcceptCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /trustaccept <player>");
            return true;
        }

        Player requester = Bukkit.getPlayer(args[0]);
        if (requester == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (trustManager.acceptTrustRequest(player, requester)) {
            player.sendMessage(ChatColor.GREEN + "You are now trusted with " + requester.getName());
            requester.sendMessage(ChatColor.GREEN + player.getName() + " accepted your trust request!");
        } else {
            player.sendMessage(ChatColor.RED + "No pending trust request from " + requester.getName());
        }

        return true;
    }

    private boolean handleUntrustCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /untrust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (trustManager.removeTrust(player, target)) {
            player.sendMessage(ChatColor.GREEN + "Trust removed with " + target.getName());
            target.sendMessage(ChatColor.YELLOW + player.getName() + " removed trust with you");
        } else {
            player.sendMessage(ChatColor.RED + "You are not trusted with " + target.getName());
        }

        return true;
    }

    private boolean handleTrustListCommand(Player player) {
        Set<UUID> trustedPlayers = trustManager.getTrustedPlayers(player);

        if (trustedPlayers.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no trusted players");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Trusted Players:");
        for (UUID uuid : trustedPlayers) {
            Player trustedPlayer = Bukkit.getPlayer(uuid);
            if (trustedPlayer != null) {
                player.sendMessage(ChatColor.WHITE + "- " + trustedPlayer.getName() + " (Online)");
            } else {
                player.sendMessage(ChatColor.GRAY + "- " + Bukkit.getOfflinePlayer(uuid).getName() + " (Offline)");
            }
        }

        Set<UUID> pendingRequests = trustManager.getPendingRequests(player);
        if (!pendingRequests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Pending Trust Requests:");
            for (UUID uuid : pendingRequests) {
                Player requester = Bukkit.getPlayer(uuid);
                if (requester != null) {
                    player.sendMessage(ChatColor.WHITE + "- " + requester.getName());
                }
            }
        }

        return true;
    }
}