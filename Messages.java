package com.frostnw.duels;

import org.bukkit.entity.Player;

public class Messages {
    
    public static String PREFIX = "§8§l[§b§lFrostNw§3§lDuels§8§l] §7";
    
    // Genel
    public static String CONSOLE = PREFIX + "§cBu komut sadece oyuncular için!";
    public static String PLAYER_NOT_FOUND = PREFIX + "§cOyuncu bulunamadı!";
    public static String SELF_DUEL = PREFIX + "§cKendine duel isteği gönderemezsin!";
    public static String ALREADY_IN_DUEL = PREFIX + "§cSen veya rakibin zaten bir düelloda!";
    public static String NO_REQUEST = PREFIX + "§cBu oyuncudan duel isteği almadın!";
    public static String ARENA_NOT_FOUND = PREFIX + "§cMüsait arena bulunamadı!";
    
    // İstekler
    public static String REQUEST_SENT(Player target) {
        return PREFIX + "§b" + target.getName() + " §3adlı oyuncuya duel isteği gönderildi! §8(§bMenü§8)";
    }
    
    public static String REQUEST_RECEIVED(Player sender) {
        return PREFIX + "§b" + sender.getName() + " §3sana duel isteği gönderdi!";
    }
    
    public static String REQUEST_INFO(Player sender, DuelMenu.DuelSettings settings) {
        String kit = settings.ownItems ? "§bKendi Eşyaları" : "§3Kit: §b" + settings.kitName;
        return PREFIX + "§7Mod: " + kit + " §8| §7Süre: §b" + settings.sure + "dk §8| §7Map: §b" + settings.mapName;
    }
    
    public static String REQUEST_EXPIRED = PREFIX + "§cDuel isteğin zaman aşımına uğradı.";
    public static String REQUEST_EXPIRED_TARGET(Player sender) {
        return PREFIX + "§b" + sender.getName() + " §3adlı oyuncunun isteği iptal oldu.";
    }
    
    // Duel başlangıç
    public static String DUEL_STARTED(Player p1, Player p2, Arena arena) {
        return "§8§m=====§b§l FrostNw §3§lDuel §8§m=====\n" +
               "§b" + p1.getName() + " §7vs §3" + p2.getName() + "\n" +
               "§7Harita: §b" + arena.getName() + "\n" +
               "§8§m========================";
    }
    
    // Duel bitiş
    public static String DUEL_END(Player winner, Player loser) {
        return "§8§m=====§b§l FrostNw §3§lDuel §8§m=====\n" +
               "§b" + winner.getName() + " §a§lKAZANDI!\n" +
               "§3" + loser.getName() + " §cmağlup oldu.\n" +
               "§8§m========================";
    }
    
    // Ölüm envanteri
    public static String DEATH_INVENTORY_OPENED(Player winner) {
        return PREFIX + "§b" + winner.getName() + " §3adlı oyuncu eşyalarına bakıyor...";
    }
    
    public static String REMAINING_ITEMS_RETURNED = PREFIX + "§3Alınmayan eşyaların envanterine geri eklendi.";
}
