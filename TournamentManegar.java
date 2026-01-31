package com.frostnw.duels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class TournamentManager {
    
    private final FrostNwDuels plugin;
    private boolean tournamentActive = false;
    private List<UUID> participants = new ArrayList<>();
    private List<UUID> waitingPlayers = new ArrayList<>();
    private Map<Integer, Match> matches = new HashMap<>();
    private int currentRound = 0;
    private String tournamentName = "";
    private DuelMenu.DuelSettings tournamentSettings;
    
    // Bracket için
    private Map<UUID, Integer> playerSeeds = new HashMap<>();
    private Map<UUID, Boolean> playerEliminated = new HashMap<>();
    
    public TournamentManager(FrostNwDuels plugin) {
        this.plugin = plugin;
    }
    
    // ==================== TURNUVA BAŞLATMA ====================
    
    public void startTournament(Player starter, String name, DuelMenu.DuelSettings settings) {
        if (tournamentActive) {
            starter.sendMessage(Messages.PREFIX + "§cZaten aktif bir turnuva var!");
            return;
        }
        
        this.tournamentActive = true;
        this.tournamentName = name;
        this.tournamentSettings = settings;
        this.currentRound = 1;
        this.participants.clear();
        this.waitingPlayers.clear();
        this.matches.clear();
        this.playerSeeds.clear();
        this.playerEliminated.clear();
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8§m=====§b§l TURNUVA BAŞLADI §8§m=====");
        Bukkit.broadcastMessage("§b" + name);
        Bukkit.broadcastMessage("§7Turnuvaya katılmak için: §b/duel tournament join");
        Bukkit.broadcastMessage("§8§m===========================");
        Bukkit.broadcastMessage("");
        
        // 60 saniye kayıt süresi
        new org.bukkit.scheduler.BukkitRunnable() {
            int countdown = 60;
            
            @Override
            public void run() {
                if (!tournamentActive) {
                    cancel();
                    return;
                }
                
                if (countdown <= 0) {
                    cancel();
                    startBracket();
                    return;
                }
                
                if (countdown == 60 || countdown == 30 || countdown == 15 || countdown <= 5) {
                    Bukkit.broadcastMessage(Messages.PREFIX + "§3Turnuvaya kayıt için §b" + countdown + " §3saniye kaldı! §7(/duel tournament join)");
                }
                
                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    public void joinTournament(Player player) {
        if (!tournamentActive) {
            player.sendMessage(Messages.PREFIX + "§cAktif turnuva yok!");
            return;
        }
        
        if (participants.contains(player.getUniqueId())) {
            player.sendMessage(Messages.PREFIX + "§cZaten turnuvaya katıldın!");
            return;
        }
        
        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage(Messages.PREFIX + "§cDüellodayken katılamazsın!");
            return;
        }
        
        participants.add(player.getUniqueId());
        playerSeeds.put(player.getUniqueId(), participants.size());
        playerEliminated.put(player.getUniqueId(), false);
        
        player.sendMessage(Messages.PREFIX + "§aTurnuvaya katıldın! §7Sıran: #" + participants.size());
        
        // Broadcast
        Bukkit.broadcastMessage(Messages.PREFIX + "§b" + player.getName() + " §3turnuvaya katıldı! §7(" + participants.size() + " oyuncu)");
    }
    
    public void leaveTournament(Player player) {
        if (!participants.contains(player.getUniqueId())) {
            player.sendMessage(Messages.PREFIX + "§cTurnuvada değilsin!");
            return;
        }
        
        // Eğer maçta ise kaybet say
        for (Match match : matches.values()) {
            if (match.isPlayerInMatch(player.getUniqueId())) {
                forfeitMatch(player);
                return;
            }
        }
        
        participants.remove(player.getUniqueId());
        playerEliminated.put(player.getUniqueId(), true);
        player.sendMessage(Messages.PREFIX + "§cTurnuvadan ayrıldın.");
        
        Bukkit.broadcastMessage(Messages.PREFIX + "§b" + player.getName() + " §cturnuvadan ayrıldı.");
    }
    
    // ==================== BRACKET SİSTEMİ ====================
    
    private void startBracket() {
        if (participants.size() < 2) {
            Bukkit.broadcastMessage(Messages.PREFIX + "§cYeterli katılımcı yok! Turnuva iptal edildi.");
            endTournament(null);
            return;
        }
        
        // İlk turu başlat
        startRound();
    }
    
    private void startRound() {
        waitingPlayers.clear();
        matches.clear();
        
        // Elemeleri temizle
        List<UUID> activePlayers = new ArrayList<>();
        for (UUID uuid : participants) {
            if (!playerEliminated.getOrDefault(uuid, false)) {
                activePlayers.add(uuid);
            }
        }
        
        if (activePlayers.size() == 1) {
            // Kazanan belli oldu
            Player winner = Bukkit.getPlayer(activePlayers.get(0));
            endTournament(winner);
            return;
        }
        
        if (activePlayers.size() == 2) {
            // Final!
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§8§m=====§b§l FİNAL §8§m=====");
            Bukkit.broadcastMessage("§b" + Bukkit.getPlayer(activePlayers.get(0)).getName() + " §7vs §3" + Bukkit.getPlayer(activePlayers.get(1)).getName());
            Bukkit.broadcastMessage("§8§m==================");
            Bukkit.broadcastMessage("");
        } else {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§8§m=====§b§l TUR " + currentRound + " §8§m=====");
            Bukkit.broadcastMessage("§7Kalan oyuncu: §b" + activePlayers.size());
            Bukkit.broadcastMessage("§8§m==================");
            Bukkit.broadcastMessage("");
        }
        
        // Eşleştirme yap (rastgele)
        Collections.shuffle(activePlayers);
        
        for (int i = 0; i < activePlayers.size(); i += 2) {
            if (i + 1 >= activePlayers.size()) {
                // Tek kalan, bye geçsin
                waitingPlayers.add(activePlayers.get(i));
                Player byePlayer = Bukkit.getPlayer(activePlayers.get(i));
                if (byePlayer != null) {
                    byePlayer.sendMessage(Messages.PREFIX + "§aBu tur bye geçiyorsun!");
                }
                continue;
            }
            
            UUID p1 = activePlayers.get(i);
            UUID p2 = activePlayers.get(i + 1);
            
            Match match = new Match(p1, p2, currentRound);
            matches.put(match.getMatchId(), match);
            
            // Oyuncuları bilgilendir
            Player player1 = Bukkit.getPlayer(p1);
            Player player2 = Bukkit.getPlayer(p2);
            
            if (player1 != null) {
                player1.sendMessage(Messages.PREFIX + "§3Rakibin: §b" + player2.getName());
                player1.sendMessage("§7Düello başlamak için hazır ol!");
            }
            
            if (player2 != null) {
                player2.sendMessage(Messages.PREFIX + "§3Rakibin: §b" + player1.getName());
                player2.sendMessage("§7Düello başlamak için hazır ol!");
            }
            
            // 3 saniye sonra başlat
            final int matchId = match.getMatchId();
            Bukkit.getScheduler().runTaskLater(plugin, () -> startMatch(matchId), 60L);
        }
    }
    
    private void startMatch(int matchId) {
        Match match = matches.get(matchId);
        if (match == null) return;
        
        Player p1 = Bukkit.getPlayer(match.player1);
        Player p2 = Bukkit.getPlayer(match.player2);
        
        if (p1 == null || p2 == null) {
            // Biri çevrimdışı, diğeri kazansın
            if (p1 != null) winMatch(p1, null);
            else if (p2 != null) winMatch(p2, null);
            return;
        }
        
        match.started = true;
        
        // Turnuva ayarları ile düello başlat
        DuelMenu.DuelSettings settings = new DuelMenu.DuelSettings(p2.getUniqueId());
        settings.ownItems = tournamentSettings.ownItems;
        settings.kitName = tournamentSettings.kitName;
        settings.sure = tournamentSettings.sure;
        settings.mapName = "Rastgele"; // Turnuvada rastgele map
        
        plugin.getDuelManager().startTournamentDuel(p1, p2, settings, matchId);
    }
    
    // ==================== MAÇ SONUÇLARI ====================
    
    public void handleMatchEnd(int matchId, Player winner, Player loser) {
        Match match = matches.get(matchId);
        if (match == null) return;
        
        match.finished = true;
        
        // Eleme
        if (loser != null) {
            playerEliminated.put(loser.getUniqueId(), true);
            loser.sendMessage(Messages.PREFIX + "§cTurnuvadan elendin!");
            Bukkit.broadcastMessage(Messages.PREFIX + "§b" + loser.getName() + " §celendi! §7Kazanan: §a" + winner.getName());
        }
        
        // Kazananı bekleme listesine ekle
        if (winner != null) {
            waitingPlayers.add(winner.getUniqueId());
            winner.sendMessage(Messages.PREFIX + "§aTebrikler! Bir sonraki turdasın.");
        }
        
        // Tüm maçlar bitti mi kontrol et
        checkRoundEnd();
    }
    
    private void checkRoundEnd() {
        boolean allFinished = true;
        for (Match match : matches.values()) {
            if (!match.finished) {
                allFinished = false;
                break;
            }
        }
        
        if (allFinished) {
            // Sonraki tura geç
            currentRound++;
            
            // Bekleyenleri aktif listeye ekle
            for (UUID uuid : waitingPlayers) {
                if (!playerEliminated.getOrDefault(uuid, false)) {
                    // Hala aktif
                }
            }
            
            // 5 saniye sonra sonraki tur
            Bukkit.broadcastMessage(Messages.PREFIX + "§3Sonraki tur 5 saniye sonra başlıyor...");
            Bukkit.getScheduler().runTaskLater(plugin, this::startRound, 100L);
        }
    }
    
    private void forfeitMatch(Player player) {
        for (Match match : matches.values()) {
            if (match.isPlayerInMatch(player.getUniqueId()) && !match.finished) {
                Player opponent = Bukkit.getPlayer(match.getOpponent(player.getUniqueId()));
                handleMatchEnd(match.getMatchId(), opponent, player);
                return;
            }
        }
    }
    
    private void endTournament(Player winner) {
        tournamentActive = false;
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8§m=====§b§l TURNUVA BİTTİ §8§m=====");
        
        if (winner != null) {
            Bukkit.broadcastMessage("§b§l" + winner.getName() + " §a§lKAZANDI!");
            Bukkit.broadcastMessage("§7Turnuva: §b" + tournamentName);
            
            // Ödül ver
            giveTournamentReward(winner);
        } else {
            Bukkit.broadcastMessage("§cTurnuva iptal edildi.");
        }
        
        Bukkit.broadcastMessage("§8§m===========================");
        Bukkit.broadcastMessage("");
        
        // Temizlik
        participants.clear();
        waitingPlayers.clear();
        matches.clear();
        playerSeeds.clear();
        playerEliminated.clear();
    }
    
    private void giveTournamentReward(Player winner) {
        // Ödül verme mantığı
        winner.sendMessage(Messages.PREFIX + "§a§lTurnuva ödülün: §b500$ + Özel Kit!");
        // Burada ekonomi plugini veya item verme eklenebilir
    }
    
    // ==================== BİLGİ KOMUTLARI ====================
    
    public void showBracket(Player player) {
        if (!tournamentActive) {
            player.sendMessage(Messages.PREFIX + "§cAktif turnuva yok!");
            return;
        }
        
        player.sendMessage("§8§m=====§b§l " + tournamentName + " §8§m=====");
        player.sendMessage("§7Tur: §b" + currentRound);
        player.sendMessage("§7Katılımcı: §b" + participants.size());
        player.sendMessage("");
        
        // Aktif maçlar
        if (!matches.isEmpty()) {
            player.sendMessage("§b§lAktif Maçlar:");
            for (Match match : matches.values()) {
                if (!match.finished) {
                    String p1Name = Bukkit.getOfflinePlayer(match.player1).getName();
                    String p2Name = Bukkit.getOfflinePlayer(match.player2).getName();
                    player.sendMessage("§7- §b" + p1Name + " §7vs §3" + p2Name);
                }
            }
            player.sendMessage("");
        }
        
        // Kalan oyuncular
        player.sendMessage("§b§lKalan Oyuncular:");
        int count = 0;
        for (UUID uuid : participants) {
            if (!playerEliminated.getOrDefault(uuid, false)) {
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                player.sendMessage("§7- §a" + name);
                count++;
                if (count >= 10) {
                    player.sendMessage("§7... ve " + (participants.size() - count) + " oyuncu daha");
                    break;
                }
            }
        }
        
        player.sendMessage("§8§m===========================");
    }
    
    public void showStatus(Player player) {
        if (!tournamentActive) {
            player.sendMessage(Messages.PREFIX + "§cAktif turnuva yok!");
            return;
        }
        
        UUID uuid = player.getUniqueId();
        
        if (!participants.contains(uuid)) {
            player.sendMessage(Messages.PREFIX + "§7Turnuvaya katılmadın. §b/duel tournament join");
            return;
        }
        
        if (playerEliminated.getOrDefault(uuid, false)) {
            player.sendMessage(Messages.PREFIX + "§cTurnuvadan elendin!");
            return;
        }
        
        // Aktif maçta mı?
        for (Match match : matches.values()) {
            if (match.isPlayerInMatch(uuid) && !match.finished) {
                player.sendMessage(Messages.PREFIX + "§aŞu an maçtasın!");
                return;
            }
        }
        
        // Beklemede mi?
        if (waitingPlayers.contains(uuid)) {
            player.sendMessage(Messages.PREFIX + "§3Sonraki turu bekliyorsun...");
        }
    }
    
    // ==================== GETTER'LAR ====================
    
    public boolean isTournamentActive() {
        return tournamentActive;
    }
    
    public boolean isInTournament(Player player) {
        return participants.contains(player.getUniqueId());
    }
    
    public boolean isInMatch(Player player) {
        for (Match match : matches.values()) {
            if (match.isPlayerInMatch(player.getUniqueId()) && !match.finished) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== INNER CLASS ====================
    
    private static class Match {
        int matchId;
        UUID player1, player2;
        int round;
        boolean started = false;
        boolean finished = false;
        
        Match(UUID p1, UUID p2, int round) {
            this.matchId = (p1.toString() + p2.toString() + System.currentTimeMillis()).hashCode();
            this.player1 = p1;
            this.player2 = p2;
            this.round = round;
        }
        
        int getMatchId() {
            return matchId;
        }
        
        boolean isPlayerInMatch(UUID uuid) {
            return player1.equals(uuid) || player2.equals(uuid);
        }
        
        UUID getOpponent(UUID uuid) {
            return player1.equals(uuid) ? player2 : player1;
        }
    }
}
