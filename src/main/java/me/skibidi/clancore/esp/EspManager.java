package me.skibidi.clancore.esp;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.team.model.Team;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EspManager {

    private static final String TEAM_PREFIX = "clancore_";

    private final ClanManager clanManager;
    private final TeamManager teamManager;
    private final WarManager warManager;
    private final Scoreboard scoreboard;

    public EspManager(ClanManager clanManager, TeamManager teamManager, WarManager warManager) {
        this.clanManager = clanManager;
        this.teamManager = teamManager;
        this.warManager = warManager;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /**
     * Cập nhật ESP cho một player.
     * Priority: Team (xanh) > Clan War (đỏ) > Clan (xám)
     */
    public void updateFor(Player player) {
        clear(player);

        // Check team trước (ưu tiên cao nhất)
        Team team = teamManager.getTeam(player);
        if (team != null) {
            // Player có team -> màu xanh (AQUA)
            String teamName = TEAM_PREFIX + "team_" + team.getLeader().toString().replace("-", "");
            org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);
            if (scoreboardTeam == null) {
                scoreboardTeam = scoreboard.registerNewTeam(teamName);
                scoreboardTeam.setCanSeeFriendlyInvisibles(true);
                scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            }
            scoreboardTeam.setColor(ChatColor.AQUA); // Màu xanh cho team
            scoreboardTeam.addEntry(player.getName());
            player.setGlowing(true);
            return;
        }

        // Không có team, check clan
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.setGlowing(false);
            return;
        }

        // Player có clan -> màu xám hoặc đỏ (nếu war)
        String teamName = TEAM_PREFIX + "clan_" + clan.getName().toLowerCase().replace(" ", "_");
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamName);
            scoreboardTeam.setCanSeeFriendlyInvisibles(true);
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }

        // Nếu clan đang war thì để màu đỏ, ngược lại màu xám
        if (warManager.isClanAtWar(clan)) {
            scoreboardTeam.setColor(ChatColor.RED);
        } else {
            scoreboardTeam.setColor(ChatColor.GRAY); // Màu bạc/xám cho clan
        }

        scoreboardTeam.addEntry(player.getName());
        player.setGlowing(true);
    }

    /**
     * Cập nhật ESP cho tất cả members của một team.
     */
    public void updateTeamMembers(Team team) {
        if (team == null) return;
        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new HashSet<>(team.getMembers())) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                updateFor(member);
            }
        }
    }

    /**
     * Cập nhật ESP cho tất cả members của một clan.
     */
    public void updateClanMembers(Clan clan) {
        if (clan == null) return;
        // Create a copy to avoid ConcurrentModificationException
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
        // Create a copy to avoid ConcurrentModificationException
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
            // Clean up empty teams to prevent scoreboard pollution
            if (team.getEntries().isEmpty()) {
                team.unregister();
            }
        }
        player.setGlowing(false);
    }
}

