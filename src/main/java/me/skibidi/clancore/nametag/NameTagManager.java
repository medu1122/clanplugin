package me.skibidi.clancore.nametag;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.team.model.Team;
import me.skibidi.clancore.war.WarManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NameTagManager {

    private static final String TEAM_PREFIX = "clancore_";
    
    private final ClanManager clanManager;
    private final TeamManager teamManager;
    private final WarManager warManager;
    private final Scoreboard scoreboard;
    private boolean luckPermsEnabled = false;

    public NameTagManager(ClanManager clanManager, TeamManager teamManager, WarManager warManager) {
        this.clanManager = clanManager;
        this.teamManager = teamManager;
        this.warManager = warManager;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        
        // Check if LuckPerms is available
        try {
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                LuckPermsProvider.get();
                luckPermsEnabled = true;
                Bukkit.getLogger().info("[ClanCore] LuckPerms detected! Rank integration enabled.");
            } else {
                luckPermsEnabled = false;
                Bukkit.getLogger().info("[ClanCore] LuckPerms not found. Rank integration disabled.");
            }
        } catch (Exception e) {
            luckPermsEnabled = false;
            Bukkit.getLogger().info("[ClanCore] LuckPerms not found. Rank integration disabled.");
        }
    }

    /**
     * Lấy rank/prefix từ LuckPerms
     */
    private String getRankPrefix(Player player) {
        if (!luckPermsEnabled) {
            return "";
        }
        
        try {
            LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return "";
            }
            
            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                return ChatColor.translateAlternateColorCodes('&', prefix);
            }
        } catch (Exception e) {
            // LuckPerms not available or error
        }
        
        return "";
    }

    /**
     * Cập nhật name tag cho một player.
     * Format:
     * - Dòng 1 (prefix): [rank] (từ LuckPerms)
     * - Dòng 2 (suffix): [tên clan]Tên người dùng
     */
    public void updateFor(Player player) {
        clear(player);

        String rankPrefix = getRankPrefix(player);
        Clan clan = clanManager.getClan(player);
        Team team = teamManager.getTeam(player);
        
        String clanTag = "";
        ChatColor nameColor = ChatColor.WHITE;
        
        // Check team trước (ưu tiên cao nhất)
        if (team != null) {
            String teamName = TEAM_PREFIX + "team_" + team.getLeader().toString().replace("-", "");
            org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);
            if (scoreboardTeam == null) {
                scoreboardTeam = scoreboard.registerNewTeam(teamName);
                scoreboardTeam.setCanSeeFriendlyInvisibles(true);
                scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            }
            nameColor = ChatColor.AQUA; // Màu xanh cho team
            scoreboardTeam.setColor(nameColor);
            scoreboardTeam.setPrefix(rankPrefix); // Dòng 1: rank
            scoreboardTeam.setSuffix(""); // Không có clan tag cho team
            scoreboardTeam.addEntry(player.getName());
            player.setGlowing(true);
            return;
        }

        // Không có team, check clan
        if (clan == null) {
            player.setGlowing(false);
            return;
        }

        // Player có clan -> màu xám hoặc đỏ (nếu war)
        clanTag = "§7[§6" + clan.getName() + "§7]";
        String teamName = TEAM_PREFIX + "clan_" + clan.getName().toLowerCase().replace(" ", "_");
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamName);
            scoreboardTeam.setCanSeeFriendlyInvisibles(true);
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }

        // Nếu clan đang war thì để màu đỏ, ngược lại màu xám
        if (warManager.isClanAtWar(clan)) {
            nameColor = ChatColor.RED;
            clanTag = "§c[§6" + clan.getName() + "§c]";
        } else {
            nameColor = ChatColor.GRAY; // Màu bạc/xám cho clan
        }

        scoreboardTeam.setColor(nameColor);
        scoreboardTeam.setPrefix(rankPrefix); // Dòng 1: rank
        scoreboardTeam.setSuffix(clanTag); // Dòng 2: [tên clan]
        scoreboardTeam.addEntry(player.getName());
        player.setGlowing(true);
    }

    /**
     * Cập nhật name tag cho tất cả members của một team.
     */
    public void updateTeamMembers(Team team) {
        if (team == null) return;
        for (UUID memberUuid : new HashSet<>(team.getMembers())) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                updateFor(member);
            }
        }
    }

    /**
     * Cập nhật name tag cho tất cả members của một clan.
     */
    public void updateClanMembers(Clan clan) {
        if (clan == null) return;
        for (UUID memberUuid : new HashSet<>(clan.getMembers())) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                updateFor(member);
            }
        }
    }

    public void updateAll() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            updateFor(online);
        }
    }

    public void clear(Player player) {
        Set<org.bukkit.scoreboard.Team> teamsToRemove = new HashSet<>();
        for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                if (team.hasEntry(player.getName())) {
                    teamsToRemove.add(team);
                }
            }
        }
        for (org.bukkit.scoreboard.Team team : teamsToRemove) {
            team.removeEntry(player.getName());
            if (team.getEntries().isEmpty()) {
                team.unregister();
            }
        }
        player.setGlowing(false);
    }
}
