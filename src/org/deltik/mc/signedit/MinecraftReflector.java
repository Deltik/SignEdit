package org.deltik.mc.signedit;

import org.bukkit.Bukkit;

public class MinecraftReflector {
    public String MINECRAFT_SERVER_VERSION;

    public MinecraftReflector() {
        MINECRAFT_SERVER_VERSION = getMinecraftServerVersion();
    }

    private static String getMinecraftServerVersion() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
    }

    public Class<?> getMinecraftServerClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + MINECRAFT_SERVER_VERSION + "." + className);
    }
}
