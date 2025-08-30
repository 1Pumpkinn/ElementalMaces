package hs.elementalMaces.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import hs.elementalMaces.managers.MaceManager;

public class MaceGiveCommand implements CommandExecutor {

    private final MaceManager maceManager;

    public MaceGiveCommand(MaceManager maceManager) {
        this.maceManager = maceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /mace <air|fire|ocean|earth>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "air":
                player.getInventory().addItem(maceManager.createAirMace());
                player.sendMessage(ChatColor.AQUA + "You received an Air Mace!");
                break;
            case "fire":
                player.getInventory().addItem(maceManager.createFireMace());
                player.sendMessage(ChatColor.RED + "You received a Fire Mace!");
                break;
            case "ocean":
                player.getInventory().addItem(maceManager.createOceanMace());
                player.sendMessage(ChatColor.BLUE + "You received an Ocean Mace!");
                break;
            case "earth":
                player.getInventory().addItem(maceManager.createEarthMace());
                player.sendMessage(ChatColor.GREEN + "You received an Earth Mace!");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid mace type! Use: air, fire, ocean, or earth");
                break;
        }

        return true;
    }
}