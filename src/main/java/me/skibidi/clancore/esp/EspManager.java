package me.skibidi.clancore.esp;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class EspManager {

    private static final String TEAM_PREFIX = "clancore_";

    private final ClanManager clanManager;
    private final WarManager warManager;
    private final Scoreboard scoreboard;

    public EspManager(ClanManager clanManager, WarManager warManager) {
        this.clanManager = clanManager;
        this.warManager = warManager;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void updateFor(Player player) {
        clear(player);

        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.setGlowing(false);
            return;
        }

        String teamName = TEAM_PREFIX + "clan_" + clan.getName().toLowerCase();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }

        // Nếu clan đang war thì để màu đỏ, ngược lại màu xám
        if (warManager.isClanAtWar(clan)) {
            team.setColor(ChatColor.RED);
        } else {
            team.setColor(ChatColor.GRAY);
        }

        team.addEntry(player.getName());
        player.setGlowing(true);
    }

    public void updateAll() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            updateFor(online);
        }
    }

    public void clear(Player player) {
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.removeEntry(player.getName());
            }
        }
        player.setGlowing(false);
    }
}

