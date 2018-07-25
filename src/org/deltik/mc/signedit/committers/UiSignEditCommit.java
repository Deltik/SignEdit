package org.deltik.mc.signedit.committers;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.listeners.Interact;

import java.lang.reflect.Field;

import static org.bukkit.Bukkit.getLogger;
import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class UiSignEditCommit implements SignEditCommit {
    private MinecraftReflector reflector;
    private Interact listener;
    private Sign sign;

    public UiSignEditCommit(MinecraftReflector reflector, Interact listener) {
        this.reflector = reflector;
        this.listener = listener;
    }

    @Override
    public void cleanup() {
        formatSignForSave(sign);
    }

    @Override
    public void commit(Player player, Sign sign) {
        this.sign = sign;
        listener.registerInProgressCommit(player, this);
        formatSignForEdit(sign);

        try {
            // Get implementation of Player (raw Bukkit player, EntityPlayer)
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            // Get instance of net.minecraft.server.*.PlayerConnection from EntityPlayer
            Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

            Field tileEntityField = getTileEntityField(sign);
            // Get instance of net.minecraft.server.*.TileEntitySign from Bukkit Sign implementation's tile entity
            Object tileEntitySign = tileEntityField.get(sign);

            Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
            signIsEditable.setAccessible(true);
            // Ensure TileEntitySign is editable
            signIsEditable.set(tileEntitySign, true);

            Field signEntityHumanField = getEntityHumanFieldFromTileEntity(tileEntitySign);
            // Designate the EntityPlayer as the editor (EntityHuman) of the TileEntitySign
            signEntityHumanField.set(tileEntitySign, entityPlayer);

            // Instantiate a BlockPosition at the Sign's coordinates
            Object position = reflector.getMinecraftServerClass("BlockPosition")
                    .getConstructor(int.class, int.class, int.class)
                    .newInstance(sign.getX(), sign.getY(), sign.getZ());

            // Create a Packet to open the sign editor at the Sign's coordinates
            Object packet = reflector.getMinecraftServerClass("PacketPlayOutOpenSignEditor")
                    .getConstructor(reflector.getMinecraftServerClass("BlockPosition"))
                    .newInstance(position);

            // On the Player's connection, send the Packet we just created to open the sign editor client-side
            connection.getClass().getDeclaredMethod("sendPacket", reflector.getMinecraftServerClass("Packet")).invoke(connection, packet);
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
    }

    private Field getTileEntityField(Sign sign) throws NoSuchFieldException {
        Field tileEntityField;
        try {
            // CraftBukkit v1.12.1 since commit 19507baf8b7903427bc3efab7118de6e7c1c931e
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/diff/src/main/java/org/bukkit/craftbukkit/block/CraftSign.java?until=19507baf8b7903427bc3efab7118de6e7c1c931e
            // commit 19507baf8b7903427bc3efab7118de6e7c1c931e
            // Author: Lukas Hennig <lukas@wirsindwir.de>
            // Date:   Sat Aug 5 14:37:19 2017 +1000
            tileEntityField = sign.getClass().getSuperclass().getDeclaredField("tileEntity");
        } catch (NoSuchFieldException e) {
            // CraftBukkit v1.12.1 before commit 19507baf8b7903427bc3efab7118de6e7c1c931e
            tileEntityField = sign.getClass().getDeclaredField("sign");
        }
        tileEntityField.setAccessible(true);
        return tileEntityField;
    }

    private Field getEntityHumanFieldFromTileEntity(Object tileEntity) throws ClassNotFoundException, NoSuchFieldException {
        Class entityHumanClass = reflector.getMinecraftServerClass("EntityHuman");
        Field[] tileEntityFields = tileEntity.getClass().getDeclaredFields();
        for (Field tileEntityField : tileEntityFields) {
            Class tileEntityFieldType = tileEntityField.getType();
            if (tileEntityFieldType.equals(entityHumanClass)) {
                tileEntityField.setAccessible(true);
                return tileEntityField;
            }
        }
        throw new NoSuchFieldException("Cannot find "+entityHumanClass.getName()+" in TileEntity");
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
