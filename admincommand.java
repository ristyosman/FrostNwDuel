package com.frostnw.duels;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    
    private final FrostNwDuels plugin;
    
    public AdminCommand(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sadece oyuncular!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("frostduels.admin")) {
            player.sendMessage(Messages.PREFIX + "§cYetkin yok!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "arena":
                // Arena yönetimi
                player.sendMessage("§eArena komutları: create, delete, setpos");
                break;
            case "kit":
                // Kit yönetimi
                player.sendMessage("§eKit komutları: create, delete");
                break;
            case "reload":
                plugin.reloadConfig();
                player.sendMessage("§aYenilendi!");
                break;
            default:
                sendHelp(player);
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§b§lFrostNw Duels §7- Admin Komutları");
        player.sendMessage("§e/dueladmin arena §7- Arena yönetimi");
        player.sendMessage("§e/dueladmin kit §7- Kit yönetimi");
        player.sendMessage("§e/dueladmin reload §7- Config yenile");
    }
}
