package org.deltik.mc.signedit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.bukkit.Bukkit.getLogger;

public class MinecraftReflector {
    public String MINECRAFT_SERVER_VERSION;

    public MinecraftReflector() {
        MINECRAFT_SERVER_VERSION = getMinecraftServerVersion();
    }

    private static String getMinecraftServerVersion() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
    }

    public Class<?> getMinecraftServerClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + MINECRAFT_SERVER_VERSION + "." + className);
        } catch (ClassNotFoundException | NullPointerException e) {
            getLogger().severe(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
