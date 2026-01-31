package com.frostnw.duels;

import com.frostnw.duels.models.Arena;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArenaManager {
    
    private final FrostNwDuels plugin;
    private Map<String, Arena> arenas = new HashMap<>();
    
    public ArenaManager(FrostNwDuels plugin) {
        this.plugin = plugin;
        loadArenas();
    }
    
    public void loadArenas() {
        // Config'den arenaları yükle
        if (plugin.getConfig().contains("arenas")) {
            for (String name : plugin.getConfig().getConfigurationSection("arenas").getKeys(false)) {
                Arena arena = new Arena(name);
                // Konumları yükle...
                arenas.put(name, arena);
            }
        }
    }
    
    public void createArena(String name, Location pos1, Location pos2) {
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        saveArena(arena);
    }
    
    public void deleteArena(String name) {
        arenas.remove(name);
        plugin.getConfig().set("arenas." + name, null);
        plugin.saveConfig();
    }
    
    public Arena getArena(String name) {
        return arenas.get(name);
    }
    
    public Arena getRandomAvailableArena() {
        for (Arena arena : arenas.values()) {
            if (arena.isAvailable()) return arena;
        }
        return null;
    }
    
    public void setArenaSpawn(String name, int pos, Location loc) {
        Arena arena = arenas.get(name);
        if (arena == null) return;
        
        if (pos == 1) arena.setSpawn1(loc);
        else arena.setSpawn2(loc);
        
        saveArena(arena);
    }
    
    private void saveArena(Arena arena) {
        String path = "arenas." + arena.getName() + ".";
        // Config'e kaydet...
        plugin.saveConfig();
    }
    
    public Map<String, Arena> getArenas() {
        return arenas;
    }
    
    public void listArenas(org.bukkit.entity.Player player) {
        player.sendMessage("§b§lArenalar:");
        for (Arena arena : arenas.values()) {
            String status = arena.isAvailable() ? "§aMüsait" : "§cDolu";
            player.sendMessage("§7- §b" + arena.getName() + " §7(" + status + "§7)");
        }
    }
}
