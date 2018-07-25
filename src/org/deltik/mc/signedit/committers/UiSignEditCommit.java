package org.deltik.mc.signedit.committers;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.block.Block;
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
            openSignEditor(player, sign);
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

    private void openSignEditor(Player player, Sign sign) throws Exception {
        // Get implementation of Player (raw Bukkit player, EntityPlayer)
        Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);

        attachEntityPlayerToSign(entityPlayer, sign);
        Object position = getBlockPosition(sign.getBlock());
        Object packet = createPositionalPacket(position, "PacketPlayOutOpenSignEditor");
        sendPacketToEntityPlayer(packet, entityPlayer);
    }

    private void sendPacketToEntityPlayer(Object packet, Object entityPlayer) throws Exception {
        // Get instance of net.minecraft.server.*.PlayerConnection from EntityPlayer
        Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        // On the Player's connection, send the Packet we just created to open the sign editor client-side
        connection
                .getClass()
                .getDeclaredMethod("sendPacket", reflector.getMinecraftServerClass("Packet"))
                .invoke(connection, packet);
    }

    private Object createPositionalPacket(Object position, String typeOfPacket) throws Exception {
        return createPositionalPacket(position, reflector.getMinecraftServerClass(typeOfPacket));
    }

    private Object createPositionalPacket(Object position, Class<?> typeOfPacket) throws Exception {
        // Create a Packet to open the sign editor at the Sign's coordinates
        return typeOfPacket
                .getConstructor(reflector.getMinecraftServerClass("BlockPosition"))
                .newInstance(position);
    }

    private Object getBlockPosition(Block block) throws Exception {
        // Instantiate a BlockPosition at the Sign's coordinates
        return reflector.getMinecraftServerClass("BlockPosition")
                .getConstructor(int.class, int.class, int.class)
                .newInstance(block.getX(), block.getY(), block.getZ());
    }

    private void attachEntityPlayerToSign(Object entityPlayer, Sign sign) throws Exception {
        Object tileEntitySign = getTileEntitySign(sign);

        maketileEntitySignEditable(tileEntitySign);

        Field signEntityHumanField = getFirstFieldOfType(tileEntitySign,
                reflector.getMinecraftServerClass("EntityHuman"));
        // Designate the EntityPlayer as the editor (EntityHuman) of the TileEntitySign
        signEntityHumanField.set(tileEntitySign, entityPlayer);
    }

    private Object getTileEntitySign(Sign sign) throws Exception {
        Field tileEntityField = getFirstFieldOfType(sign,
                reflector.getMinecraftServerClass("TileEntity"));
        // Get instance of net.minecraft.server.*.TileEntitySign from Bukkit Sign implementation's tile entity
        return tileEntityField.get(sign);
    }

    private void maketileEntitySignEditable(Object tileEntitySign) throws Exception {
        Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
        signIsEditable.setAccessible(true);
        // Ensure TileEntitySign is editable
        signIsEditable.set(tileEntitySign, true);
    }

    private Field getFirstFieldOfType(Object source, Class<?> desiredType) throws NoSuchFieldException {
        return getFirstFieldOfType(source.getClass(), desiredType);
    }

    private Field getFirstFieldOfType(Class<?> source, Class<?> desiredType) throws NoSuchFieldException {
        Class<?> ancestor = source;
        while (ancestor != null) {
            Field[] fields = ancestor.getDeclaredFields();
            for (Field field : fields) {
                Class<?> candidateType = field.getType();
                if (desiredType.isAssignableFrom(candidateType)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            ancestor = ancestor.getSuperclass();
        }
        throw new NoSuchFieldException("Cannot match " + desiredType.getName() + " in ancestry of " + source.getName());
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
