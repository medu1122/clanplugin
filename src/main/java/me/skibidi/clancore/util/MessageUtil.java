package me.skibidi.clancore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Gửi thông báo phản hồi: Title (chữ to, hiện lâu) để player dễ đọc.
 * Action bar vẫn dùng khi cần gợi ý ngắn.
 */
public final class MessageUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    /** Title: fade in 0.5s, hiển thị 4s, fade out 1s */
    private static final int TITLE_FADE_IN_TICKS = 10;
    private static final int TITLE_STAY_TICKS = 80;
    private static final int TITLE_FADE_OUT_TICKS = 20;

    private MessageUtil() {}

    /**
     * Gửi phản hồi lên màn hình bằng Title (chữ to, hiện lâu) để người chơi đọc kịp.
     * Nếu message dài thì cắt: dòng chính (title) tối đa ~40 ký tự, phần còn lại đưa vào subtitle.
     */
    public static void sendFeedback(Player player, String message) {
        sendFeedback(player, message, null);
    }

    public static void sendFeedback(Player player, String titleLine, String subtitleLine) {
        if (player == null || !player.isOnline()) return;
        String main = titleLine != null ? titleLine : "";
        String sub = subtitleLine != null ? subtitleLine : "";
        if (main.length() > 45) {
            sub = main.substring(45).trim();
            main = main.substring(0, 45).trim();
        }
        Component titleComp = LEGACY.deserialize("§l§f" + main);
        Component subComp = sub.isEmpty() ? Component.empty() : LEGACY.deserialize("§7" + sub);
        try {
            Title.Times times = Title.Times.times(
                Duration.ofMillis(TITLE_FADE_IN_TICKS * 50L),
                Duration.ofMillis(TITLE_STAY_TICKS * 50L),
                Duration.ofMillis(TITLE_FADE_OUT_TICKS * 50L)
            );
            player.showTitle(Title.title(titleComp, subComp, times));
        } catch (Throwable t) {
            player.sendMessage(titleLine != null ? titleLine : "");
            if (subtitleLine != null && !subtitleLine.isEmpty()) player.sendMessage(subtitleLine);
        }
    }

    public static void sendActionBar(Player player, String legacyMessage) {
        if (player == null || legacyMessage == null || !player.isOnline()) return;
        try {
            player.sendActionBar(LEGACY.deserialize(legacyMessage));
        } catch (Throwable t) {
            player.sendMessage(legacyMessage);
        }
    }
}
