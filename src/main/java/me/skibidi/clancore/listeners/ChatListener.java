package me.skibidi.clancore.listeners;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.chat.ClanChatManager;
import me.skibidi.clancore.chat.TeamChatManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final ClanManager clanManager;
    private final ClanChatManager clanChatManager;
    private final TeamChatManager teamChatManager;
    private boolean luckPermsEnabled = false;

    public ChatListener(ClanManager clanManager, ClanChatManager clanChatManager, TeamChatManager teamChatManager) {
        this.clanManager = clanManager;
        this.clanChatManager = clanChatManager;
        this.teamChatManager = teamChatManager;
        
        // Check if LuckPerms is available
        try {
            if (org.bukkit.Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                LuckPermsProvider.get();
                luckPermsEnabled = true;
                org.bukkit.Bukkit.getLogger().info("[ClanCore] LuckPerms detected! Chat rank integration enabled.");
            } else {
                luckPermsEnabled = false;
                org.bukkit.Bukkit.getLogger().info("[ClanCore] LuckPerms not found. Chat rank integration disabled.");
            }
        } catch (Exception e) {
            luckPermsEnabled = false;
            org.bukkit.Bukkit.getLogger().info("[ClanCore] LuckPerms not found. Chat rank integration disabled.");
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Kiểm tra nếu player đang dùng clan chat hoặc team chat
        if (clanChatManager.isClanChatEnabled(player)) {
            // Clan chat được xử lý bởi ClanChatManager, không format ở đây
            return;
        }
        
        if (teamChatManager.isTeamChatEnabled(player)) {
            // Team chat được xử lý bởi TeamChatManager, không format ở đây
            return;
        }

        // Format chat message: [tên clan][rank] medu1122 : hi
        Clan clan = clanManager.getClan(player);
        String rankPrefix = getRankPrefix(player);
        
        String clanTag = "";
        if (clan != null) {
            clanTag = "§7[§6" + clan.getName() + "§7] ";
        }
        // Format: [ClanName] [rank] PlayerName: message (ưu tiên tên clan ở đầu)
        String format = clanTag + rankPrefix + "%1$s§7: %2$s";
        event.setFormat(format);
    }
}
