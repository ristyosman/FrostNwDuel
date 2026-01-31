package com.frostnw.duels;

import com.frostnw.duels.models.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class KitManager {
    
    private final FrostNwDuels plugin;
    private Map<String, Kit> kits = new HashMap<>();
    
    public KitManager(FrostNwDuels plugin) {
        this.plugin = plugin;
        loadKits();
    }
    
    public void loadKits() {
        // kits.yml'den yükle
    }
    
    public void createKit(String name, String displayName, org.bukkit.inventory.PlayerInventory inv) {
        Kit kit = new Kit(name, displayName);
        
        // İkon olarak elindeki item
        kit.setIcon(inv.getItemInMainHand());
        
        // Zırhlar
        kit.setArmor(inv.getArmorContents());
        
        // İtemler
        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                kit.getItems().put(i, item);
            }
        }
        
        kits.put(name, kit);
        saveKit(kit);
    }
    
    public void deleteKit(String name) {
        kits.remove(name);
    }
    
    public Kit getKit(String name) {
        return kits.get(name);
    }
    
    public void saveKit(Kit kit) {
        // kits.yml'e kaydet
    }
    
    public void listKits(Player player) {
        player.sendMessage("§b§lKitler:");
        for (Kit kit : kits.values()) {
            player.sendMessage("§7- §b" + kit.getDisplayName());
        }
    }
    
    public Map<String, Kit> getKits() {
        return kits;
    }
}
