package com.frostnw.duels;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    
    private final FrostNwDuels plugin;
    
    public DuelCommand(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.CONSOLE);
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(Messages.PREFIX + "§7Kullanım: §b/duel §3<oyuncu>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }
        
        if (target == player) {
            player.sendMessage(Messages.SELF_DUEL);
            return true;
        }
        
        if (plugin.getDuelManager().isInDuel(player) || plugin.getDuelManager().isInDuel(target)) {
            player.sendMessage(Messages.ALREADY_IN_DUEL);
            return true;
        }
        
        // 🎯 DİREKT MENÜ AÇILIYOR
        new DuelMenu(plugin).openDuelMenu(player, target);
        
        return true;
    }
}
