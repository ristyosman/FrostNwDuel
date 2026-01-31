package com.frostnw.duels.models;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Kit {
    
    private String name;
    private String displayName;
    private ItemStack icon;
    private Map<Integer, ItemStack> items = new HashMap<>();
    private ItemStack[] armor = new ItemStack[4];
    
    public Kit(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    
    public void apply(Player player) {
        player.getInventory().clear();
        
        // Zırhları giydir
        player.getInventory().setArmorContents(armor);
        
        // İtemleri ver
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue());
        }
    }
    
    // Getter ve Setter'lar
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public ItemStack getIcon() { return icon; }
    public Map<Integer, ItemStack> getItems() { return items; }
    
    public void setIcon(ItemStack icon) { this.icon = icon; }
    public void setArmor(ItemStack[] armor) { this.armor = armor; }
}
