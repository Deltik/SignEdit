package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.exceptions.SignEditorInvocationException;
import org.deltik.mc.signedit.listeners.SignEditListener;

import java.lang.reflect.Field;

public class UiSignEditInteraction implements SignEditInteraction {
    private final MinecraftReflector reflector;
    private final SignEditListener listener;
    private final ChatComms comms;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;

    public UiSignEditInteraction(
            MinecraftReflector reflector,
            SignEditListener listener,
            ChatComms comms,
            SignText signText,
            SignTextHistoryManager historyManager
    ) {
        this.reflector = reflector;
        this.listener = listener;
        this.comms = comms;
        this.signText = signText;
        this.historyManager = historyManager;
    }

    @Override
    public String getName() {
        return "Open sign editor";
    }

    @Override
    public void cleanup(Event event) {
        formatSignTextForSave(signText);
        if (!(event instanceof SignChangeEvent)) {
            return;
        }

        SignChangeEvent signChangeEvent = (SignChangeEvent) event;
        String[] lines = signChangeEvent.getLines();
        for (int i = 0; i < lines.length; i++) {
            signText.setLine(i, lines[i]);
        }
        signText.applySign(signChangeEvent);
        if (signText.signChanged()) {
            historyManager.getHistory(signChangeEvent.getPlayer()).push(signText);
        }

        comms.compareSignText(signText);
    }

    @Override
    public void interact(Player player, Sign sign) {
        signText.setTargetSign(sign);
        signText.importSign();

        formatSignTextForEdit(signText);
        listener.setInProgressInteraction(player, this);

        try {
            openSignEditor(player, sign);
        } catch (Exception e) {
            formatSignTextForSave(signText);
            throw new SignEditorInvocationException(e);
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
                .getConstructor(position.getClass())
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

    private void formatSignTextForEdit(SignText signText) {
        for (int i = 0; i < 4; i++) {
            signText.setLineLiteral(i, signText.getLineParsed(i));
        }
        signText.applySign();
    }

    private void formatSignTextForSave(SignText signText) {
        for (int i = 0; i < 4; i++) {
            signText.setLine(i, signText.getLine(i));
        }
        signText.applySign();
    }
}
