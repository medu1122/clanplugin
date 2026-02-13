package me.skibidi.clancore.commands;

import me.skibidi.clancore.clan.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClanAdminTabCompleter implements TabCompleter {

    private final ClanManager clanManager;

    public ClanAdminTabCompleter(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Chỉ hiển thị tab completion cho người có OP
        if (!sender.isOp()) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Subcommands với alias ngắn
            List<String> subcommands = Arrays.asList(
                    "givepoints", "gp", "setlevel", "sl", "tpall", "tp"
            );
            return subcommands.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            // Suggest clan names for givepoints, setlevel, tpall
            if (sub.equals("givepoints") || sub.equals("gp") || 
                sub.equals("setlevel") || sub.equals("sl") || 
                sub.equals("tpall") || sub.equals("tp")) {
                return clanManager.getAllClans().stream()
                        .map(clan -> clan.getName())
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            // Suggest numbers for givepoints and setlevel
            if (sub.equals("givepoints") || sub.equals("gp")) {
                return Arrays.asList("100", "500", "1000", "5000");
            }
            if (sub.equals("setlevel") || sub.equals("sl")) {
                return Arrays.asList("1", "2", "3", "4", "5");
            }
        }

        return new ArrayList<>();
    }
}
