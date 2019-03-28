package org.deltik.mc.signedit.interactions;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.listeners.SignEditListener;

import java.lang.reflect.Field;

import static org.bukkit.Bukkit.getLogger;
import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class UiSignEditInteraction implements SignEditInteraction {
    private MinecraftReflector reflector;
    private SignEditListener listener;
    private Sign sign;

    public UiSignEditInteraction(MinecraftReflector reflector, SignEditListener listener) {
        this.reflector = reflector;
        this.listener = listener;
    }

    @Override
    public void cleanup() {
        formatSignForSave(sign);
    }

    @Override
    public void interact(Player player, Sign sign) {
        this.sign = sign;
        listener.setInProgressInteraction(player, this);
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
        Object entityPlayer = getEntityPlayer(player);
        attachEntityPlayerToSign(entityPlayer, sign);
        Object position = getBlockPosition(sign.getBlock());
        Object packet = createPositionalPacket(position, "PacketPlayOutOpenSignEditor");
        sendPacketToEntityPlayer(packet, entityPlayer);
    }

    private Object getEntityPlayer(Player player) throws Exception {
        Field entityPlayerField = getFirstFieldOfType(player,
                reflector.getMinecraftServerClass("Entity"));
        return entityPlayerField.get(player);
    }

    private void sendPacketToEntityPlayer(Object packet, Object entityPlayer) throws Exception {
        Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        connection
                .getClass()
                .getDeclaredMethod("sendPacket", reflector.getMinecraftServerClass("Packet"))
                .invoke(connection, packet);
    }

    private Object createPositionalPacket(Object position, String typeOfPacket) throws Exception {
        return createPositionalPacket(position, reflector.getMinecraftServerClass(typeOfPacket));
    }

    private Object createPositionalPacket(Object position, Class<?> typeOfPacket) throws Exception {
        return typeOfPacket
                .getConstructor(reflector.getMinecraftServerClass("BlockPosition"))
                .newInstance(position);
    }

    private Object getBlockPosition(Block block) throws Exception {
        return reflector.getMinecraftServerClass("BlockPosition")
                .getConstructor(int.class, int.class, int.class)
                .newInstance(block.getX(), block.getY(), block.getZ());
    }

    private void attachEntityPlayerToSign(Object entityPlayer, Sign sign) throws Exception {
        Object tileEntitySign = getTileEntitySign(sign);

        maketileEntitySignEditable(tileEntitySign);

        Field signEntityHumanField = getFirstFieldOfType(tileEntitySign,
                reflector.getMinecraftServerClass("EntityHuman"));
        signEntityHumanField.set(tileEntitySign, entityPlayer);
    }

    private Object getTileEntitySign(Sign sign) throws Exception {
        Field tileEntityField = getFirstFieldOfType(sign,
                reflector.getMinecraftServerClass("TileEntity"));
        return tileEntityField.get(sign);
    }

    private void maketileEntitySignEditable(Object tileEntitySign) throws Exception {
        Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
        signIsEditable.setAccessible(true);
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
