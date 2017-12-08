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
            formatSignForEdit(sign);
        } else {
            player.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to invoke the editor!");
            return false;
        }
        try {
            // Get implementation of Player (raw Bukkit player, EntityPlayer)
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            // Get instance of net.minecraft.server.*.PlayerConnection from EntityPlayer
            Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

            Field tileEntityField = getTileEntityField(sign);
            tileEntityField.setAccessible(true);
            // Get instance of net.minecraft.server.*.TileEntitySign from Bukkit Sign implementation's tile entity
            Object tileEntitySign = tileEntityField.get(sign);

            Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
            signIsEditable.setAccessible(true);
            // Ensure TileEntitySign is editable
            signIsEditable.set(tileEntitySign, true);

            Field signEntityHumanField = tileEntitySign.getClass().getDeclaredField("h");
            signEntityHumanField.setAccessible(true);
            // Designate the EntityPlayer as the editor (EntityHuman) of the TileEntitySign
            signEntityHumanField.set(tileEntitySign, entityPlayer);

            // Instantiate a PooledBlockPosition at the Sign's coordinates
            Object position = reflector.getMinecraftServerClass("BlockPosition$PooledBlockPosition")
                    .getMethod("d", double.class, double.class, double.class)
                    .invoke(null, sign.getX(), sign.getY(), sign.getZ());

            // Create a Packet to open the sign editor at the Sign's coordinates
            Object packet = reflector.getMinecraftServerClass("PacketPlayOutOpenSignEditor").getConstructor(
                    reflector.getMinecraftServerClass("BlockPosition"))
                    .newInstance(position);

            // On the Player's connection, send the Packet we just created to open the sign editor client-side
            connection.getClass().getDeclaredMethod("sendPacket", reflector.getMinecraftServerClass("Packet")).invoke(connection, packet);

            return true;
        } catch (Exception e) {
            formatSignForSave(sign);
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

    private Field getTileEntityField(Sign sign) throws NoSuchFieldException {
        Field tileEntityField;
        try {
            // CraftBukkit v1.12.1 since commit 19507baf8b7903427bc3efab7118de6e7c1c931e and newer
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/diff/src/main/java/org/bukkit/craftbukkit/block/CraftSign.java?until=19507baf8b7903427bc3efab7118de6e7c1c931e
            // commit 19507baf8b7903427bc3efab7118de6e7c1c931e
            // Author: Lukas Hennig <lukas@wirsindwir.de>
            // Date:   Sat Aug 5 14:37:19 2017 +1000
            tileEntityField = sign.getClass().getSuperclass().getDeclaredField("tileEntity");
        } catch (NoSuchFieldException e) {
            // CraftBukkit v1.12.1 before commit 19507baf8b7903427bc3efab7118de6e7c1c931e and earlier
            tileEntityField = sign.getClass().getDeclaredField("sign");
        }
        return tileEntityField;
    }

    private void formatSignForEdit(Sign sign) {
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, sign.getLine(i).replace('§', '&'));
        }
        sign.update();
    }

    private void formatSignForSave(Sign sign) {
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, sign.getLine(i).replace('&', '§'));
        }
        sign.update();
    }
}
