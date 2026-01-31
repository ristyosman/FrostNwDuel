package com.frostnw.duels;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final FrostNwDuels plugin;
    private FileConfiguration config;
    
    public ConfigManager(FrostNwDuels plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void setLobby(Location loc) {
        config.set("lobby.world", loc.getWorld().getName());
        config.set("lobby.x", loc.getX());
        config.set("lobby.y", loc.getY());
        config.set("lobby.z", loc.getZ());
        config.set("lobby.yaw", loc.getYaw());
        config.set("lobby.pitch", loc.getPitch());
        plugin.saveConfig();
    }
    
    public void setSpawn(Location loc) {
        config.set("spawn.world", loc.getWorld().getName());
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", loc.getYaw());
        config.set("spawn.pitch", loc.getPitch());
        plugin.saveConfig();
    }
    
    public Location getLobby() {
        return getLocation("lobby");
    }
    
    public Location getSpawn() {
        return getLocation("spawn");
    }
    
    private Location getLocation(String path) {
        if (!config.contains(path + ".world")) return null;
        
        String world = config.getString(path + ".world");
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");
        
        if (plugin.getServer().getWorld(world) == null) return null;
        
        return new Location(plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
    }
}
