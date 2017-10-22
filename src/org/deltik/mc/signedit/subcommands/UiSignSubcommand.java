package org.deltik.mc.signedit.subcommands;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.deltik.mc.signedit.MinecraftReflector;

import java.lang.reflect.Field;

import static org.bukkit.Bukkit.getLogger;
import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class UiSignSubcommand extends SignSubcommand {
    private MinecraftReflector reflector;

    // Create a MinecraftReflector (used in production)
    public UiSignSubcommand() {
        this(new MinecraftReflector());
    }

    // Provide a MinecraftReflector (useful in tests)
    public UiSignSubcommand(MinecraftReflector r) {
        reflector = r;
        getLogger().warning(reflector.MINECRAFT_SERVER_VERSION);
    }

    @Override
    public boolean execute() {
        Block block = getTargetBlockOfPlayer(player);
        Sign sign;
        // TODO: shouldDoClickingMode() support
        // TODO: Cleanly cancel sign edit when player uncleanly leaves UI
        if (block.getState() instanceof Sign) {
            sign = (Sign) block.getState();
            for (int i = 0; i < 4; i++) {
                sign.setLine(i, sign.getLine(i).replace('§', '&'));
            }
            sign.update();
        } else {
            player.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to invoke the editor!");
            return false;
        }
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

            Object tileEntitySign;
            Field tileField;
            try {
                // CraftBukkit v1.12.1 since commit 19507baf8b7903427bc3efab7118de6e7c1c931e and newer
                // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/diff/src/main/java/org/bukkit/craftbukkit/block/CraftSign.java?until=19507baf8b7903427bc3efab7118de6e7c1c931e
                // commit 19507baf8b7903427bc3efab7118de6e7c1c931e
                // Author: Lukas Hennig <lukas@wirsindwir.de>
                // Date:   Sat Aug 5 14:37:19 2017 +1000
                tileField = sign.getClass().getSuperclass().getDeclaredField("tileEntity");
            } catch (NoSuchFieldException e) {
                // CraftBukkit v1.12.1 before commit 19507baf8b7903427bc3efab7118de6e7c1c931e and earlier
                tileField = sign.getClass().getDeclaredField("sign");
            }
            tileField.setAccessible(true);
            tileEntitySign = tileField.get(sign);

            Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
            signIsEditable.setAccessible(true);
            signIsEditable.set(tileEntitySign, true);

            Field handler = tileEntitySign.getClass().getDeclaredField("h");
            handler.setAccessible(true);
            handler.set(tileEntitySign, entityPlayer);

            Object position = reflector.getMinecraftServerClass("BlockPosition$PooledBlockPosition")
                    .getMethod("d", double.class, double.class, double.class)
                    .invoke(null, sign.getX(), sign.getY(), sign.getZ());

            Object packet = reflector.getMinecraftServerClass("PacketPlayOutOpenSignEditor").getConstructor(
                    reflector.getMinecraftServerClass("BlockPosition"))
                    .newInstance(position);

            connection.getClass().getDeclaredMethod("sendPacket", reflector.getMinecraftServerClass("Packet")).invoke(connection, packet);

            return true;
        } catch (Exception e) {
            for (int i = 0; i < 4; i++) {
                sign.setLine(i, sign.getLine(i).replace('&', '§'));
            }
            sign.update();
            player.sendMessage(CHAT_PREFIX + "§c§lFailed to invoke sign editor!");
            player.sendMessage(CHAT_PREFIX + "§7Likely cause: §rMinecraft server API changed");
            player.sendMessage(CHAT_PREFIX + "§7Server admin: §rCheck for updates to this plugin");
            player.sendMessage(CHAT_PREFIX);
            player.sendMessage(CHAT_PREFIX + "§7Error code: §r" + e.toString());
            player.sendMessage(CHAT_PREFIX + "§6(More details logged in server console)");
            getLogger().severe(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }
}
