package com.frostnw.duels;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SureCommand implements CommandExecutor {
    
    private final FrostNwDuels plugin;
    
    public SureCommand(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        
        Player player = (Player) sender;
        
        // Hızlı süre ayarlama menüsü
        // Bu komut /duelsure yazınca direkt süre menüsü açar
        
        return true;
    }
}
