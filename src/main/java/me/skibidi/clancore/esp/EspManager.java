package me.skibidi.clancore.esp;

import me.skibidi.clancore.nametag.NameTagManager;
import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class EspManager {

    private final NameTagManager nameTagManager;

    public EspManager(ClanManager clanManager, TeamManager teamManager, WarManager warManager) {
        // Sử dụng NameTagManager để quản lý cả ESP và name tag
        this.nameTagManager = new NameTagManager(clanManager, teamManager, warManager);
    }

    /**
     * Cập nhật ESP và name tag cho một player.
     * Priority: Team (xanh) > Clan War (đỏ) > Clan (xám)
     */
    public void updateFor(Player player) {
        nameTagManager.updateFor(player);
    }

    /**
     * Cập nhật ESP và name tag cho tất cả members của một team.
     */
    public void updateTeamMembers(me.skibidi.clancore.team.model.Team team) {
        nameTagManager.updateTeamMembers(team);
    }

    /**
     * Cập nhật ESP và name tag cho tất cả members của một clan.
     */
    public void updateClanMembers(me.skibidi.clancore.clan.model.Clan clan) {
        nameTagManager.updateClanMembers(clan);
    }

    public void updateAll() {
        nameTagManager.updateAll();
    }

    public void clear(Player player) {
        nameTagManager.clear(player);
    }
}

