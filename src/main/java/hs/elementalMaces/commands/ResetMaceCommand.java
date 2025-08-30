package hs.elementalMaces.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import hs.elementalMaces.managers.MaceManager;

import java.util.Arrays;
import java.util.UUID;

public class ResetMaceCommand implements CommandExecutor {

    private final MaceManager maceManager;

    public ResetMaceCommand(MaceManager maceManager) {
        this.maceManager = maceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("elementalmaces.resetmace")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /resetmace <air|fire|ocean|earth|all>");
            return true;
        }

        String type = args[0].toLowerCase();
        if (type.equals("all")) {
            maceManager.resetAllMaces();
            sender.sendMessage(ChatColor.GREEN + "All maces reset!");
        } else if (Arrays.asList("air", "fire", "ocean", "earth").contains(type)) {
            if (!maceManager.isMaceCrafted(type)) {
                sender.sendMessage(ChatColor.YELLOW + type + " mace not crafted yet!");
                return true;
            }
            UUID owner = maceManager.getMaceOwner(type);
            String ownerName = owner != null ? Bukkit.getOfflinePlayer(owner).getName() : "Unknown";
            maceManager.resetMace(type);
            sender.sendMessage(ChatColor.GREEN + type + " mace reset! (was owned by " + ownerName + ")");
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid type! Use: air, fire, ocean, earth, all");
        }
        return true;
    }
}