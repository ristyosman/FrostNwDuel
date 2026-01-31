package com.frostnw.duels;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {
    
    private final FrostNwDuels plugin;
    private Map<UUID, Duel> activeDuels = new HashMap<>();
    private Map<UUID, UUID> duelRequests = new HashMap<>();
    private Map<UUID, Location> oldLocations = new HashMap<>();
    private Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    
    public DuelManager(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    public void sendDuelRequest(Player sender, Player target, DuelMenu.DuelSettings settings) {
        // İsteği kaydet
        duelRequests.put(sender.getUniqueId(), target.getUniqueId());
        
        // Mesajlar
        sender.sendMessage(Messages.REQUEST_SENT(target));
        
        target.sendMessage(Messages.REQUEST_RECEIVED(sender));
        target.sendMessage(Messages.REQUEST_INFO(sender, settings));
        
        // Action bar
        sendActionBar(target, "§b§lFrostNw §3§lDuel §8| §b" + sender.getName() + " §3sana duel isteği gönderdi!");
        
        // 60 saniye sonra sil
        new BukkitRunnable() {
            @Override
            public void run() {
                if (duelRequests.containsKey(sender.getUniqueId())) {
                    duelRequests.remove(sender.getUniqueId());
                    sender.sendMessage(Messages.REQUEST_EXPIRED);
                    target.sendMessage(Messages.REQUEST_EXPIRED_TARGET(sender));
                }
            }
        }.runTaskLater(plugin, 1200L);
    }
    
    public void acceptDuel(Player accepter, Player sender) {
        if (!duelRequests.containsKey(sender.getUniqueId()) || 
            !duelRequests.get(sender.getUniqueId()).equals(accepter.getUniqueId())) {
            accepter.sendMessage(Messages.NO_REQUEST);
            return;
        }
        
        DuelMenu.DuelSettings settings = DuelMenu.settingsMap.get(sender.getUniqueId());
        if (settings == null) settings = new DuelMenu.DuelSettings(accepter.getUniqueId());
        
        duelRequests.remove(sender.getUniqueId());
        DuelMenu.settingsMap.remove(sender.getUniqueId());
        
        startDuel(sender, accepter, settings);
    }
    
    public void startDuel(Player p1, Player p2, DuelMenu.DuelSettings settings) {
        // Eski konumları kaydet
        oldLocations.put(p1.getUniqueId(), p1.getLocation());
        oldLocations.put(p2.getUniqueId(), p2.getLocation());
        
        // Envanterleri kaydet (eğer kit kullanılıyorsa)
        if (!settings.ownItems) {
            savedInventories.put(p1.getUniqueId(), p1.getInventory().getContents());
            savedInventories.put(p2.getUniqueId(), p2.getInventory().getContents());
            p1.getInventory().clear();
            p2.getInventory().clear();
            
            // Kit ver
            Kit kit = plugin.getKitManager().getKit(settings.kitName);
            if (kit != null) {
                kit.apply(p1);
                kit.apply(p2);
            }
        }
        
        // Arena seç
        Arena arena;
        if (settings.mapName.equals("Rastgele")) {
            arena = plugin.getArenaManager().getRandomAvailableArena();
        } else {
            arena = plugin.getArenaManager().getArena(settings.mapName);
        }
        
        if (arena == null) {
            p1.sendMessage(Messages.ARENA_NOT_FOUND);
            p2.sendMessage(Messages.ARENA_NOT_FOUND);
            return;
        }
        
        arena.setAvailable(false);
        
        // Teleport
        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());
        
        // Duel oluştur
        Duel duel = new Duel(p1.getUniqueId(), p2.getUniqueId(), arena, settings);
        activeDuels.put(p1.getUniqueId(), duel);
        activeDuels.put(p2.getUniqueId(), duel);
        
        // 🎯 5 SANİYE GERİ SAYIM
        startCountdown(p1, p2, arena, settings);
    }
    
    private void startCountdown(Player p1, Player p2, Arena arena, DuelSettings settings) {
        new BukkitRunnable() {
            int count = 5;
            
            @Override
            public void run() {
                if (count > 0) {
                    // Titreşim ve ses
                    String color = count <= 2 ? "§c§l" : "§e§l";
                    String msg = color + count;
                    
                    sendTitle(p1, msg, "§bHazır Ol!", 0, 20, 0);
                    sendTitle(p2, msg, "§bHazır Ol!", 0, 20, 0);
                    
                    p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    
                    // Kör ve yavaşlat (hareket etmesinler)
                    p1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                    p2.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                    p1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 255));
                    p2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 255));
                    
                    count--;
                } else {
                    // BAŞLA!
                    sendTitle(p1, "§a§lBAŞLA!", "§bDüello Başladı!", 10, 40, 10);
                    sendTitle(p2, "§a§lBAŞLA!", "§bDüello Başladı!", 10, 40, 10);
                    
                    p1.playSound(p1.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                    p2.playSound(p2.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                    
                    // Efektleri kaldır
                    p1.removePotionEffect(PotionEffectType.BLINDNESS);
                    p2.removePotionEffect(PotionEffectType.BLINDNESS);
                    p1.removePotionEffect(PotionEffectType.SLOW);
                    p2.removePotionEffect(PotionEffectType.SLOW);
                    
                    // Duel başladı mesajı
                    Bukkit.broadcastMessage(Messages.DUEL_STARTED(p1, p2, arena));
                    
                    // Süre sınırlaması varsa başlat
                    if (settings.sure > 0) {
                        startTimeLimit(p1, p2, settings.sure);
                    }
                    
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    private void startTimeLimit(Player p1, Player p2, int minutes) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isInDuel(p1) || !isInDuel(p2)) {
                    cancel();
                    return;
                }
                
                // Berabere bitir
                endDuelAsDraw(p1, p2);
            }
        }.runTaskLater(plugin, minutes * 60 * 20);
    }
    
    public void handleDeath(Player dead, Player killer) {
        if (!isInDuel(dead)) return;
        
        Duel duel = activeDuels.get(dead.getUniqueId());
        Player winner = killer != null ? killer : 
            (duel.p1.equals(dead.getUniqueId()) ? Bukkit.getPlayer(duel.p2) : Bukkit.getPlayer(duel.p1));
        
        if (winner == null) return;
        
        // 🎯 ÖLEN KİŞİNİN ENVANTERİNİ GÖSTER
        openDeathInventory(winner, dead);
        
        // Kazananı ilan et
        Bukkit.broadcastMessage(Messages.DUEL_END(winner, dead));
        
        // Temizlik
        endDuelCleanup(winner, dead);
    }
    
    // 🎯 ÖLEN KİŞİNİN ENVANTERİNİ AÇ
    private void openDeathInventory(Player winner, Player dead) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§l◆ §c" + dead.getName() + " §7Envanteri §8§l◆");
        
        ItemStack[] items = dead.getInventory().getContents();
        for (int i = 0; i < items.length && i < 54; i++) {
            if (items[i] != null) {
                inv.setItem(i, items[i]);
            }
        }
        
        // Bilgi itemi
        inv.setItem(53, createInfoItem(dead));
        
        winner.openInventory(inv);
        
        // Ölen kişiye mesaj
        dead.sendMessage(Messages.DEATH_INVENTORY_OPENED(winner));
        
        // Alınmayan eşyaları geri vermek için kaydet
        final UUID deadUUID = dead.getUniqueId();
        final ItemStack[] originalItems = items.clone();
        
        // 30 saniye sonra kapanırsa veya envanter kapanırsa kalanları geri ver
        new BukkitRunnable() {
            @Override
            public void run() {
                returnRemainingItems(winner, deadUUID, originalItems, inv);
            }
        }.runTaskLater(plugin, 600L); // 30 saniye
    }
    
    private void returnRemainingItems(Player winner, UUID deadUUID, ItemStack[] original, Inventory openedInv) {
        Player dead = Bukkit.getPlayer(deadUUID);
        if (dead == null || !dead.isOnline()) return;
        
        // Açılan envanterdeki mevcut itemleri kontrol et
        // Winner'ın envanterine eklenenleri bul
        
        for (int i = 0; i < original.length && i < 54; i++) {
            if (original[i] != null) {
                ItemStack current = openedInv.getItem(i);
                if (current != null) {
                    // Hala orada, winner almamış
                    dead.getInventory().addItem(current);
                }
                // Eğer null ise winner almış, bir şey yapma
            }
        }
        
        dead.sendMessage(Messages.REMAINING_ITEMS_RETURNED);
        winner.closeInventory();
    }
    
    private ItemStack createInfoItem(Player dead) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lBİLGİ");
        meta.setLore(Arrays.asList(
            "§7" + dead.getName() + " adlı oyuncunun",
            "§7eşyalarını alabilirsin!",
            "",
            "§3Alınmayan eşyalar 30 saniye",
            "§3sonra sahibine geri verilecek!"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    public void endDuelCleanup(Player winner, Player loser) {
        Duel duel = activeDuels.get(winner.getUniqueId());
        if (duel == null) return;
        
        // Arena boşalt
        duel.arena.setAvailable(true);
        
        // Teleport
        String tpMode = plugin.getConfig().getString("teleport-after-duel", "SPAWN");
        
        teleportPlayer(winner, tpMode);
        teleportPlayer(loser, tpMode);
        
        // Envanterleri geri yükle
        if (!duel.settings.ownItems) {
            restoreInventory(winner);
            restoreInventory(loser);
        }
        
        // Listelerden temizle
        activeDuels.remove(winner.getUniqueId());
        activeDuels.remove(loser.getUniqueId());
        oldLocations.remove(winner.getUniqueId());
        oldLocations.remove(loser.getUniqueId());
        savedInventories.remove(winner.getUniqueId());
        savedInventories.remove(loser.getUniqueId());
    }
    
    private void teleportPlayer(Player player, String mode) {
        switch (mode.toUpperCase()) {
            case "SPAWN":
                player.performCommand("spawn");
                break;
            case "LOBBY":
            case "HUB":
                player.performCommand("hub");
                break;
            case "BACK":
                if (oldLocations.containsKey(player.getUniqueId())) {
                    player.teleport(oldLocations.get(player.getUniqueId()));
                }
                break;
            default:
                player.performCommand("spawn");
        }
    }
    
    private void restoreInventory(Player player) {
        if (savedInventories.containsKey(player.getUniqueId())) {
            player.getInventory().setContents(savedInventories.get(player.getUniqueId()));
            savedInventories.remove(player.getUniqueId());
        }
    }
    
    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }
    
    public void endAllDuels() {
        for (Duel duel : new HashSet<>(activeDuels.values())) {
            Player p1 = Bukkit.getPlayer(duel.p1);
            Player p2 = Bukkit.getPlayer(duel.p2);
            if (p1 != null && p2 != null) {
                endDuelCleanup(p1, p2);
            }
        }
    }
    
    private void sendTitle(Player p, String title, String sub, int in, int stay, int out) {
        p.sendTitle(title, sub, in, stay, out);
    }
    
    private void sendActionBar(Player p, String msg) {
        p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg));
    }
    
    public static class Duel {
        public UUID p1, p2;
        public Arena arena;
        public DuelMenu.DuelSettings settings;
        public long startTime;
        
        public Duel(UUID p1, UUID p2, Arena arena, DuelMenu.DuelSettings settings) {
            this.p1 = p1;
            this.p2 = p2;
            this.arena = arena;
            this.settings = settings;
            this.startTime = System.currentTimeMillis();
        }
    }
}
