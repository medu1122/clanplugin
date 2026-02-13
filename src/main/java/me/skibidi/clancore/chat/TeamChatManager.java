package me.skibidi.clancore.chat;

import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.team.model.Team;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamChatManager {

    private final TeamManager teamManager;
    private final Map<UUID, Boolean> teamChatEnabled = new HashMap<>();

    public TeamChatManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    public void toggleTeamChat(Player player) {
        UUID uuid = player.getUniqueId();
        teamChatEnabled.put(uuid, !teamChatEnabled.getOrDefault(uuid, false));
        boolean enabled = teamChatEnabled.get(uuid);
        player.sendMessage(enabled ? "§b[Team] §eĐã bật chat team. Dùng §f/team chat <tin nhắn>" : "§b[Team] §eĐã tắt chat team.");
    }

    public boolean isTeamChatEnabled(Player player) {
        return teamChatEnabled.getOrDefault(player.getUniqueId(), false);
    }

    public void sendTeamMessage(Player sender, String message) {
        Team team = teamManager.getTeam(sender);
        if (team == null) {
            sender.sendMessage("§cBạn không ở trong team nào!");
            return;
        }

        String formattedMessage = "§b[Team] §e" + sender.getName() + "§7: §f" + message;

        // Send to all online members
        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(team.getMembers())) {
            Player member = org.bukkit.Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
            }
        }
    }

    public void disableTeamChat(Player player) {
        teamChatEnabled.remove(player.getUniqueId());
    }
}
