package com.frostnw.duels;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelMenu {
    
    private final FrostNwDuels plugin;
    public static Map<UUID, DuelSettings> settingsMap = new HashMap<>();
    
    public DuelMenu(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    // 🎯 ANA DÜEL MENÜSÜ - /duel <oyuncu> yazınca açılır
    public void openDuelMenu(Player sender, Player target) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8§l◆ §b§lFrostNw §3§lDuel §8§l◆");
        
        // Cam paneller (Dekoratif)
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, createGlass(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        
        // Bilgi paneli
        inv.setItem(4, createItem(Material.BOOK, "§b§lDÜEL BİLGİSİ", 
            "§7Rakip: §b" + target.getName(),
            "",
            "§3Aşağıdan ayarları yapıp",
            "§3duel isteği gönderin!"
        ));
        
        // Rakibin kafası
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.setDisplayName("§b§l" + target.getName());
        headMeta.setLore(Arrays.asList("§7Bu oyuncuya duel", "§7isteği gönderilecek!"));
        head.setItemMeta(headMeta);
        inv.setItem(13, head);
        
        // 🎮 OYUN MODU SEÇİMİ
        inv.setItem(20, createItem(Material.DIAMOND_SWORD, "§b§lKENDİ EŞYALARINLA", 
            "§7Kendi envanterindeki",
            "§7eşyalarla düello yap!",
            "",
            "§3Durum: §a✔ Aktif",
            "",
            "§b▸ Tıkla ve seç!"
        ));
        
        inv.setItem(24, createItem(Material.CHEST, "§3§lKİT SEÇİMİ", 
            "§7Hazır bir kit seçerek",
            "§7düello yap!",
            "",
            "§3Durum: §c✘ Pasif",
            "",
            "§b▸ Tıkla ve seç!"
        ));
        
        // ⚙️ AYARLAR
        inv.setItem(30, createItem(Material.CLOCK, "§b§lSÜRE AYARLARI", 
            "§7Düello süresini ayarla",
            "",
            "§3Mevcut: §b5 Dakika",
            "",
            "§b▸ Tıkla ve ayarla!"
        ));
        
        inv.setItem(32, createItem(Material.BEACON, "§3§lMAPAYARLARI", 
            "§7Düello haritasını seç",
            "",
            "§3Mevcut: §bRastgele",
            "",
            "§b▸ Tıkla ve seç!"
        ));
        
        // 🚀 GÖNDER BUTONU
        inv.setItem(40, createGlowItem(Material.EMERALD_BLOCK, "§a§l✔ DÜEL İSTEĞİ GÖNDER", 
            "§7Tüm ayarları kaydet ve",
            "§b" + target.getName() + " §7adlı oyuncuya",
            "§7duel isteği gönder!",
            "",
            "§3▸ Tıkla ve gönder!"
        ));
        
        // İptal
        inv.setItem(36, createItem(Material.BARRIER, "§c§l✘ İPTAL", 
            "§7Menüyü kapat"
        ));
        
        // Ayarları başlat
        settingsMap.put(sender.getUniqueId(), new DuelSettings(target.getUniqueId()));
        
        sender.openInventory(inv);
    }
    
    // Kit seçim menüsü
    public void openKitMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§l◆ §b§lKit §3§lSeçimi §8§l◆");
        
        // Geri butonu
        inv.setItem(0, createItem(Material.ARROW, "§c§l← Geri", "§7Önceki menüye dön"));
        
        // Kitleri listele
        int slot = 10;
        for (Kit kit : plugin.getKitManager().getKits().values()) {
            if (slot >= 44) break;
            
            ItemStack icon = kit.getIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§b§l" + kit.getDisplayName());
            meta.setLore(Arrays.asList(
                "§7Bu kit ile düelloya",
                "§7girmek için tıkla!",
                "",
                "§3İçerik:",
                "§b" + kit.getItems().size() + " §7eşya",
                "",
                "§b▸ Tıkla ve seç!"
            ));
            icon.setItemMeta(meta);
            inv.setItem(slot++, icon);
            
            if (slot % 9 == 0) slot += 2;
        }
        
        // Boşlukları doldur
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, createGlass(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }
        
        player.openInventory(inv);
    }
    
    // Süre ayarlama menüsü
    public void openSureMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§l◆ §b§lSüre §3§lAyarları §8§l◆");
        
        // Geri
        inv.setItem(0, createItem(Material.ARROW, "§c§l← Geri", "§7Önceki menüye dön"));
        
        // Süre seçenekleri
        int[] dakikalar = {3, 5, 10, 15, 20, 30};
        int[] slots = {10, 12, 14, 16, 20, 22};
        
        for (int i = 0; i < dakikalar.length; i++) {
            inv.setItem(slots[i], createItem(Material.CLOCK, "§b§l" + dakikalar[i] + " DAKİKA", 
                "§7Düello süresi: §b" + dakikalar[i] + " dk",
                "",
                "§3▸ Tıkla ve seç!"
            ));
        }
        
        // Sınırsız
        inv.setItem(24, createItem(Material.BARRIER, "§c§lSINIRSIZ", 
            "§7Süre limiti olmadan",
            "§7düello yap!",
            "",
            "§3▸ Tıkla ve seç!"
        ));
        
        // Boşluklar
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, createGlass(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }
        
        player.openInventory(inv);
    }
    
    // Map seçim menüsü
    public void openMapMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8§l◆ §b§lMap §3§lSeçimi §8§l◆");
        
        // Geri
        inv.setItem(0, createItem(Material.ARROW, "§c§l← Geri", "§7Önceki menüye dön"));
        
        // Rastgele
        inv.setItem(10, createGlowItem(Material.COMPASS, "§b§lRASTGELE", 
            "§7Rastgele bir map seç!",
            "",
            "§3▸ Tıkla ve seç!"
        ));
        
        // Mapleri listele
        int slot = 12;
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (slot >= 35) break;
            
            Material mat = arena.isAvailable() ? Material.GRASS_BLOCK : Material.RED_WOOL;
            String status = arena.isAvailable() ? "§a✔ Müsait" : "§c✘ Dolu";
            
            inv.setItem(slot++, createItem(mat, "§b§l" + arena.getName(), 
                "§7Durum: " + status,
                "",
                "§3▸ Tıkla ve seç!"
            ));
        }
        
        // Boşluklar
        for (int i = 0; i < 36; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, createGlass(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }
        
        player.openInventory(inv);
    }
    
    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createGlowItem(Material mat, String name, String... lore) {
        ItemStack item = createItem(mat, name, lore);
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createGlass(Material mat, String name) {
        return createItem(mat, name);
    }
    
    public static class DuelSettings {
        public UUID targetUUID;
        public boolean ownItems = true;
        public String kitName = null;
        public int sure = 5; // dakika
        public String mapName = "Rastgele";
        
        public DuelSettings(UUID targetUUID) {
            this.targetUUID = targetUUID;
        }
    }
}
