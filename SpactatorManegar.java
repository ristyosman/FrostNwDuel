package com.frostnw.duels;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpectatorManager {
    
    private final FrostNwDuels plugin;
    private Map<UUID, Set<UUID>> duelSpectators = new HashMap<>(); // Duel -> Spectators
    private Map<UUID, SpectatorData> spectatorData = new HashMap<>(); // Player -> Old Data
    
    public SpectatorManager(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    public void startSpectating(Player spectator, Player player1, Player player2) {
        UUID duelId = getDuelId(player1, player2);
        
        // Eski verileri kaydet
        spectatorData.put(spectator.getUniqueId(), new SpectatorData(
            spectator.getLocation(),
            spectator.getGameMode(),
            spectator.getInventory().getContents(),
            spectator.getInventory().getArmorContents(),
            spectator.getActivePotionEffects()
        ));
        
        // Temizle ve spectatör yap
        spectator.getInventory().clear();
        spectator.getInventory().setArmorContents(null);
        spectator.setGameMode(GameMode.SPECTATOR);
        
        // İlk oyuncuya teleport
        spectator.teleport(player1.getLocation());
        
        // Listeye ekle
        duelSpectators.computeIfAbsent(duelId, k -> new HashSet<>()).add(spectator.getUniqueId());
        
        // Bilgi mesajı
        spectator.sendMessage(Messages.PREFIX + "§3Düello izlemeye başladın!");
        spectator.sendMessage("§7/duel spec leave §8- §cİzlemeyi bırak");
        
        // Oyunculara bildir
        player1.sendMessage(Messages.PREFIX + "§b" + spectator.getName() + " §3düellonu izliyor!");
        player2.sendMessage(Messages.PREFIX + "§b" + spectator.getName() + " §3düellonu izliyor!");
        
        // Boss bar veya action bar ile göster
        startSpectatorBar(spectator, player1, player2);
    }
    
    public void stopSpectating(Player spectator) {
        UUID specUUID = spectator.getUniqueId();
        
        if (!spectatorData.containsKey(specUUID)) return;
        
        SpectatorData data = spectatorData.get(specUUID);
        
        // Duel listesinden çıkar
        for (Set<UUID> specs : duelSpectators.values()) {
            specs.remove(specUUID);
        }
        
        // Eski haline döndür
        spectator.teleport(data.location);
        spectator.setGameMode(data.gameMode);
        spectator.getInventory().setContents(data.inventory);
        spectator.getInventory().setArmorContents(data.armor);
        
        // Efektleri geri ver
        for (PotionEffect effect : spectator.getActivePotionEffects()) {
            spectator.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : data.potionEffects) {
            spectator.addPotionEffect(effect);
        }
        
        spectatorData.remove(specUUID);
        spectator.sendMessage(Messages.PREFIX + "§cİzleme durduruldu.");
    }
    
    public void stopAllSpectating(UUID player1, UUID player2) {
        UUID duelId = getDuelId(Bukkit.getPlayer(player1), Bukkit.getPlayer(player2));
        Set<UUID> specs = duelSpectators.get(duelId);
        
        if (specs == null) return;
        
        for (UUID specUUID : new HashSet<>(specs)) {
            Player spec = Bukkit.getPlayer(specUUID);
            if (spec != null) {
                stopSpectating(spec);
                spec.sendMessage(Messages.PREFIX + "§cDüello bitti, izleme durduruldu.");
            }
        }
        
        duelSpectators.remove(duelId);
    }
    
    private void startSpectatorBar(Player spectator, Player p1, Player p2) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!spectatorData.containsKey(spectator.getUniqueId())) {
                    cancel();
                    return;
                }
                
                if (!p1.isOnline() || !p2.isOnline()) {
                    stopSpectating(spectator);
                    cancel();
                    return;
                }
                
                String msg = "§b" + p1.getName() + " §f" + (int)p1.getHealth() + "❤ §7vs §b" + p2.getName() + " §f" + (int)p2.getHealth() + "❤";
                sendActionBar(spectator, msg);
            }
        }.runTaskTimer(plugin, 0, 5); // Her 5 tick (0.25 saniye)
    }
    
    private UUID getDuelId(Player p1, Player p2) {
        // Her iki oyuncunun UUID'sini kullanarak benzersiz ID
        return p1.getUniqueId().compareTo(p2.getUniqueId()) < 0 
            ? UUID.nameUUIDFromBytes((p1.getUniqueId().toString() + p2.getUniqueId().toString()).getBytes())
            : UUID.nameUUIDFromBytes((p2.getUniqueId().toString() + p1.getUniqueId().toString()).getBytes());
    }
    
    private void sendActionBar(Player p, String msg) {
        p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg));
    }
    
    public boolean isSpectating(Player player) {
        return spectatorData.containsKey(player.getUniqueId());
    }
    
    public Set<UUID> getSpectators(UUID player1, UUID player2) {
        return duelSpectators.getOrDefault(getDuelId(Bukkit.getPlayer(player1), Bukkit.getPlayer(player2)), new HashSet<>());
    }
    
    private static class SpectatorData {
        Location location;
        GameMode gameMode;
        ItemStack[] inventory;
        ItemStack[] armor;
        Collection<PotionEffect> potionEffects;
        
        SpectatorData(Location loc, GameMode gm, ItemStack[] inv, ItemStack[] arm, Collection<PotionEffect> effects) {
            this.location = loc;
            this.gameMode = gm;
            this.inventory = inv;
            this.armor = arm;
            this.potionEffects = effects;
        }
    }
}
