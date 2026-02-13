package me.skibidi.clancore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Subcommands với alias ngắn
            List<String> subcommands = Arrays.asList(
                    "create", "c", "invite", "i", "accept", "a", "leave", "l", "kick", "k",
                    "disband", "d", "info", "in", "list", "li", "transfer", "t",
                    "taccept", "ta", "tdeny", "td", "chat", "ch", "help", "h"
            );
            return subcommands.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "invite":
            case "kick":
            case "transfer":
            case "t":
                // Player names
                if (args.length == 2) {
                    return null; // Let Bukkit handle player name completion
                }
                break;

            case "chat":
                // No completion for message
                return new ArrayList<>();
        }

        return new ArrayList<>();
    }
}
