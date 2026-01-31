package com.frostnw.duels;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {
    
    private final FrostNwDuels plugin;
    
    public DuelListener(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();
        
        if (plugin.getDuelManager().isInDuel(dead)) {
            event.setDeathMessage(null); // Ölüm mesajını kapat
            plugin.getDuelManager().handleDeath(dead, killer);
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getDuelManager().isInDuel(player)) {
            // Rakip kazansın
            plugin.getDuelManager().handleLeave(player);
        }
    }
}
