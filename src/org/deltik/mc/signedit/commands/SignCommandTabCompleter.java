package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SignCommandTabCompleter implements TabCompleter {
    @Inject
    public SignCommandTabCompleter() {}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null; // TODO
    }
}
