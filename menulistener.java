package com.frostnw.duels;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {
    
    private final FrostNwDuels plugin;
    
    public MenuListener(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        
        if (title.contains("FrostNw Duel")) {
            e.setCancelled(true);
            
            if (e.getCurrentItem() == null) return;
            
            // Ana menü
            if (title.contains("Duel")) {
                handleMainMenu(player, e.getSlot());
            }
            // Kit menü
            else if (title.contains("Kit")) {
                handleKitMenu(player, e.getSlot());
            }
            // Süre menü
            else if (title.contains("Süre")) {
                handleSureMenu(player, e.getSlot());
            }
            // Map menü
            else if (title.contains("Map")) {
                handleMapMenu(player, e.getSlot());
            }
        }
        
        // Ölüm envanteri
        if (title.contains("Envanteri")) {
            // İzin ver - eşyaları alabilir
            // Ama 53. slottaki bilgi itemini alamaz
            if (e.getSlot() == 53) {
                e.setCancelled(true);
            }
        }
    }
    
    private void handleMainMenu(Player player, int slot) {
        DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
        if (settings == null) return;
        
        Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
        if (target == null) {
            player.sendMessage(Messages.PLAYER_NOT_FOUND);
            player.closeInventory();
            return;
        }
        
        switch (slot) {
            case 20: // Kendi eşyaları
                settings.ownItems = true;
                settings.kitName = null;
                player.sendMessage(Messages.PREFIX + "§3Mod: §bKendi Eşyaların");
                player.closeInventory();
                new DuelMenu(plugin).openDuelMenu(player, target);
                break;
                
            case 24: // Kit seçimi
                player.closeInventory();
                new DuelMenu(plugin).openKitMenu(player);
                break;
                
            case 30: // Süre ayarları
                player.closeInventory();
                new DuelMenu(plugin).openSureMenu(player);
                break;
                
            case 32: // Map ayarları
                player.closeInventory();
                new DuelMenu(plugin).openMapMenu(player);
                break;
                
            case 40: // Gönder
                player.closeInventory();
                plugin.getDuelManager().sendDuelRequest(player, target, settings);
                break;
                
            case 36: // İptal
                player.closeInventory();
                DuelMenu.settingsMap.remove(player.getUniqueId());
                player.sendMessage(Messages.PREFIX + "§cİptal edildi.");
                break;
        }
    }
    
    private void handleKitMenu(Player player, int slot) {
        if (slot == 0) {
            // Geri
            player.closeInventory();
            DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
            if (settings != null) {
                Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                if (target != null) {
                    new DuelMenu(plugin).openDuelMenu(player, target);
                }
            }
            return;
        }
        
        // Kit seçimi
        int index = 0;
        for (Kit kit : plugin.getKitManager().getKits().values()) {
            if (index == slot - 10) { // 10'dan başlıyor
                DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
                if (settings != null) {
                    settings.ownItems = false;
                    settings.kitName = kit.getName();
                    player.sendMessage(Messages.PREFIX + "§3Kit seçildi: §b" + kit.getDisplayName());
                    player.closeInventory();
                    
                    Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                    if (target != null) {
                        new DuelMenu(plugin).openDuelMenu(player, target);
                    }
                }
                return;
            }
            index++;
        }
    }
    
    private void handleSureMenu(Player player, int slot) {
        if (slot == 0) {
            // Geri
            player.closeInventory();
            DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
            if (settings != null) {
                Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                if (target != null) {
                    new DuelMenu(plugin).openDuelMenu(player, target);
                }
            }
            return;
        }
        
        int[] dakikalar = {3, 5, 10, 15, 20, 30};
        int[] slots = {10, 12, 14, 16, 20, 22};
        
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
                if (settings != null) {
                    settings.sure = dakikalar[i];
                    player.sendMessage(Messages.PREFIX + "§3Süre ayarlandı: §b" + dakikalar[i] + " dakika");
                    player.closeInventory();
                    
                    Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                    if (target != null) {
                        new DuelMenu(plugin).openDuelMenu(player, target);
                    }
                }
                return;
            }
        }
        
        // Sınırsız (slot 24)
        if (slot == 24) {
            DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
            if (settings != null) {
                settings.sure = 0;
                player.sendMessage(Messages.PREFIX + "§3Süre: §bSınırsız");
                player.closeInventory();
                
                Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                if (target != null) {
                    new DuelMenu(plugin).openDuelMenu(player, target);
                }
            }
        }
    }
    
    private void handleMapMenu(Player player, int slot) {
        if (slot == 0) {
            // Geri
            player.closeInventory();
            DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
            if (settings != null) {
                Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                if (target != null) {
                    new DuelMenu(plugin).openDuelMenu(player, target);
                }
            }
            return;
        }
        
        // Rastgele
        if (slot == 10) {
            DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
            if (settings != null) {
                settings.mapName = "Rastgele";
                player.sendMessage(Messages.PREFIX + "§3Map: §bRastgele");
                player.closeInventory();
                
                Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                if (target != null) {
                    new DuelMenu(plugin).openDuelMenu(player, target);
                }
            }
            return;
        }
        
        // Map seçimi
        int index = 0;
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (index == slot - 12) { // 12'den başlıyor
                DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(player.getUniqueId());
                if (settings != null) {
                    settings.mapName = arena.getName();
                    player.sendMessage(Messages.PREFIX + "§3Map seçildi: §b" + arena.getName());
                    player.closeInventory();
                    
                    Player target = org.bukkit.Bukkit.getPlayer(settings.targetUUID);
                    if (target != null) {
                        new DuelMenu(plugin).openDuelMenu(player, target);
                    }
                }
                return;
            }
            index++;
        }
    }
}
