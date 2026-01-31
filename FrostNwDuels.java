package com.frostnw.duels;

import org.bukkit.plugin.java.JavaPlugin;

public class FrostNwDuels extends JavaPlugin {
    
    private static FrostNwDuels instance;
    private ConfigManager configManager;
    private DuelManager duelManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        this.configManager = new ConfigManager(this);
        this.duelManager = new DuelManager(this);
        this.arenaManager = new ArenaManager(this);
        this.kitManager = new KitManager(this);
        
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("dueladmin").setExecutor(new AdminCommand(this));
        getCommand("duelsure").setExecutor(new SureCommand(this));
        
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        
        getLogger().info("§b§lFrostNw §3§lDuels §7- §aAktif!");
    }
    
    @Override
    public void onDisable() {
        duelManager.endAllDuels();
        getLogger().info("§b§lFrostNw §3§lDuels §7- §cDeaktif!");
    }
    
    public static FrostNwDuels getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public KitManager getKitManager() { return kitManager; }
}
