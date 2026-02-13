package me.skibidi.clancore.clan;

import me.skibidi.clancore.clan.model.Clan;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BuffManager {

    private final ClanManager clanManager;
    private final ClanPointManager pointManager;

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    public BuffManager(ClanManager clanManager, ClanPointManager pointManager) {
        this.clanManager = clanManager;
        this.pointManager = pointManager;
    }

    /**
     * Apply buffs cho player dựa trên clan level.
     */
    public void applyBuffs(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            removeBuffs(player);
            return;
        }

        int speedBuff = pointManager.getTotalSpeedBuff(clan);
        int healthBuff = pointManager.getTotalHealthBuff(clan);

        // Apply speed buff
        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Remove old modifier - collect to list first to avoid ConcurrentModificationException
            java.util.List<AttributeModifier> toRemove = speedAttr.getModifiers().stream()
                    .filter(mod -> mod.getUniqueId().equals(SPEED_MODIFIER_UUID))
                    .collect(java.util.stream.Collectors.toList());
            toRemove.forEach(speedAttr::removeModifier);

            // Add new modifier
            if (speedBuff > 0) {
                double multiplier = speedBuff / 100.0;
                AttributeModifier modifier = new AttributeModifier(
                        SPEED_MODIFIER_UUID,
                        "clan_speed_buff",
                        multiplier,
                        AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                speedAttr.addModifier(modifier);
            }
        }

        // Apply health buff
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            // Remove old modifier - collect to list first to avoid ConcurrentModificationException
            java.util.List<AttributeModifier> toRemove = healthAttr.getModifiers().stream()
                    .filter(mod -> mod.getUniqueId().equals(HEALTH_MODIFIER_UUID))
                    .collect(java.util.stream.Collectors.toList());
            toRemove.forEach(healthAttr::removeModifier);

            // Add new modifier
            if (healthBuff > 0) {
                double multiplier = healthBuff / 100.0;
                AttributeModifier modifier = new AttributeModifier(
                        HEALTH_MODIFIER_UUID,
                        "clan_health_buff",
                        multiplier,
                        AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                healthAttr.addModifier(modifier);
            }
        }
    }

    /**
     * Remove buffs khi player rời clan.
     */
    public void removeBuffs(Player player) {
        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Collect to list first to avoid ConcurrentModificationException
            java.util.List<AttributeModifier> toRemove = speedAttr.getModifiers().stream()
                    .filter(mod -> mod.getUniqueId().equals(SPEED_MODIFIER_UUID))
                    .collect(java.util.stream.Collectors.toList());
            toRemove.forEach(speedAttr::removeModifier);
        }

        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            // Collect to list first to avoid ConcurrentModificationException
            java.util.List<AttributeModifier> toRemove = healthAttr.getModifiers().stream()
                    .filter(mod -> mod.getUniqueId().equals(HEALTH_MODIFIER_UUID))
                    .collect(java.util.stream.Collectors.toList());
            toRemove.forEach(healthAttr::removeModifier);
        }
    }

    /**
     * Update buffs cho tất cả members của clan khi level up.
     */
    public void updateClanBuffs(Clan clan) {
        if (clan == null) return;
        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(clan.getMembers())) {
            Player member = org.bukkit.Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                applyBuffs(member);
            }
        }
    }
}
