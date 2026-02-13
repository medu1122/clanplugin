package me.skibidi.clancore.chat;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanChatManager {

    private final ClanManager clanManager;
    private final Map<UUID, Boolean> clanChatEnabled = new HashMap<>(); // true = clan chat, false = global

    public ClanChatManager(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void toggleClanChat(Player player) {
        UUID uuid = player.getUniqueId();
        clanChatEnabled.put(uuid, !clanChatEnabled.getOrDefault(uuid, false));
        boolean enabled = clanChatEnabled.get(uuid);
        player.sendMessage(enabled ? "§6[Clan] §eĐã bật chat clan. Dùng §f/clan chat <tin nhắn>" : "§6[Clan] §eĐã tắt chat clan.");
    }

    public boolean isClanChatEnabled(Player player) {
        return clanChatEnabled.getOrDefault(player.getUniqueId(), false);
    }

    public void sendClanMessage(Player sender, String message) {
        Clan clan = clanManager.getClan(sender);
        if (clan == null) {
            sender.sendMessage("§cBạn không ở trong clan nào!");
            return;
        }

        String formattedMessage = "§6[Clan] §e" + sender.getName() + "§7: §f" + message;

        // Send to all online members
        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(clan.getMembers())) {
            Player member = org.bukkit.Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
            }
        }
    }

    public void disableClanChat(Player player) {
        clanChatEnabled.remove(player.getUniqueId());
    }
}
