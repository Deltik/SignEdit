package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignEditPlugin;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

public class VersionSignSubcommand implements SignSubcommand {
    private final SignEditPlugin plugin;
    private final ChatComms comms;

    @Inject
    public VersionSignSubcommand(SignEditPlugin plugin, ChatComms comms) {
        this.plugin = plugin;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        String version = plugin.getDescription().getVersion();
        comms.tellPlayer(comms.primary() + comms.t("version", comms.reset() + " " + version));
        return null;
    }
}
