/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.net/>
 *
 * This file is part of SignEdit for Bukkit.
 *
 * SignEdit for Bukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SignEdit for Bukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SignEdit for Bukkit.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.deltik.mc.signedit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

@Singleton
public class Configuration {
    private final File configFile;
    private FileConfiguration bukkitConfig;
    private String template;
    private static final String CONFIG_CLICKING = "clicking";
    private static final String CONFIG_LINE_STARTS_AT = "line-starts-at";
    private static final String CONFIG_FORCE_LOCALE = "force-locale";
    private static final String CONFIG_LOCALE = "locale";
    private static final String CONFIG_COMPAT_SIGN_UI = "compatibility.sign-ui";
    private static final String CONFIG_COMPAT_EDIT_VALIDATION = "compatibility.edit-validation";
    private static final Map<String, Object> defaults;

    static {
        defaults = new HashMap<>();
        defaults.put(CONFIG_CLICKING, "auto");
        defaults.put(CONFIG_LINE_STARTS_AT, 1);
        defaults.put(CONFIG_FORCE_LOCALE, false);
        defaults.put(CONFIG_LOCALE, "en");
        defaults.put(CONFIG_COMPAT_SIGN_UI, "Auto");
        defaults.put(CONFIG_COMPAT_EDIT_VALIDATION, "Standard");
    }

    protected Object get(String key) {
        switch (key) {
            case CONFIG_CLICKING:
                return getClicking();
            case CONFIG_LINE_STARTS_AT:
                return getLineStartsAt();
            case CONFIG_FORCE_LOCALE:
                return getForceLocale();
            case CONFIG_LOCALE:
                return getLocale();
            case CONFIG_COMPAT_SIGN_UI:
                return getSignUi();
            case CONFIG_COMPAT_EDIT_VALIDATION:
                return getEditValidation();
        }
        return null;
    }

    @Inject
    public Configuration(Plugin plugin) {
        this("plugins//" + plugin.getName() + "//config.yml");
    }

    public Configuration(String f) {
        this(new File(f));
    }

    public Configuration(File f) {
        configFile = f;

        try {
            prepare();
        } catch (IOException e) {
            getLogger().severe(ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unrecoverable error while setting up plugin configuration");
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    public void prepare() throws IOException {
        InputStream templateStream = getClass().getResourceAsStream("/config.yml.j2");
        assert templateStream != null;
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = templateStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        template = result.toString("UTF-8");
        configHighstate();
    }

    public void reloadConfig() {
        bukkitConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public void configHighstate() throws IOException {
        if (!configFile.exists()) {
            writeDefaultConfig();
        }
        reloadConfig();
        writeSaneConfig();
    }

    public void writeDefaultConfig() throws IOException {
        bukkitConfig = new YamlConfiguration();
        mergeInDefaultConfig();
        writeSaneConfig();
    }

    private void mergeInDefaultConfig() {
        for (Map.Entry<String, Object> defaultConfigItem : defaults.entrySet()) {
            if (!bukkitConfig.contains(defaultConfigItem.getKey())) {
                bukkitConfig.set(defaultConfigItem.getKey(), defaultConfigItem.getValue());
            }
        }
    }

    public void writeSaneConfig() throws IOException {
        mergeInDefaultConfig();
        sanitizeConfig();
        Map<String, Object> yamlContext = bukkitConfig.getValues(true);
        String output = template;
        for (Map.Entry<String, Object> entry : yamlContext.entrySet()) {
            String key = entry.getKey();
            String newKey = key.replaceAll("-", "_").replaceAll("\\.", "__");
            output = output.replaceAll("\\{\\{[ ]*" + newKey + "[ ]*}}", yamlContext.get(key).toString());
        }

        File configFileParent = configFile.getCanonicalFile().getParentFile();
        if (configFileParent != null) {
            configFileParent.mkdirs();
        }
        Files.write(configFile.toPath(), output.getBytes());
    }

    public String getClicking() {
        String def = (String) defaults.get(CONFIG_CLICKING);
        String clicking = bukkitConfig.getString(CONFIG_CLICKING, def);
        if (clicking == null) return def;
        clicking = clicking.toLowerCase();
        if (!Arrays.asList(new String[]{"auto", "false", "true"}).contains(clicking)) {
            return def;
        }
        return clicking;
    }

    public void setClicking(String newValue) {
        bukkitConfig.set(CONFIG_CLICKING, newValue);
    }

    public boolean allowedToEditSignBySight() {
        String clicking = getClicking();
        return clicking.equalsIgnoreCase("false") || clicking.equalsIgnoreCase("auto");
    }

    public boolean allowedToEditSignByRightClick() {
        String clicking = getClicking();
        return clicking.equalsIgnoreCase("true") || clicking.equalsIgnoreCase("auto");
    }

    public int getLineStartsAt() {
        int def = (int) defaults.get(CONFIG_LINE_STARTS_AT);
        int lineStartsAt = bukkitConfig.getInt(CONFIG_LINE_STARTS_AT, def);
        if (lineStartsAt < 0 || lineStartsAt > 1) return def;
        return lineStartsAt;
    }

    public void setLineStartsAt(String newValue) {
        bukkitConfig.set(CONFIG_LINE_STARTS_AT, Integer.parseInt(newValue));
    }

    public int getMinLine() {
        return getLineStartsAt();
    }

    public int getMaxLine() {
        return getLineStartsAt() + 3;
    }

    public boolean getForceLocale() {
        return bukkitConfig.getBoolean(CONFIG_FORCE_LOCALE);
    }

    public Locale getLocale() {
        String def = (String) defaults.get(CONFIG_LOCALE);
        try {
            String languageTag = bukkitConfig.getString(CONFIG_LOCALE);
            if (languageTag == null) languageTag = def;
            return new Locale.Builder().setLanguageTag(languageTag).build();
        } catch (IllformedLocaleException | NullPointerException e) {
            return new Locale.Builder().setLanguageTag(def).build();
        }
    }

    public String getSignUi() {
        String def = (String) defaults.get(CONFIG_COMPAT_SIGN_UI);
        String signUi = bukkitConfig.getString(CONFIG_COMPAT_SIGN_UI, def);
        if (signUi == null) return def;
        if (!Arrays.asList(new String[]{"auto", "editablebook", "native"}).contains(signUi.toLowerCase())) {
            return def;
        }
        return signUi;
    }

    public String getEditValidation() {
        String def = (String) defaults.get(CONFIG_COMPAT_EDIT_VALIDATION);
        String editValidator = bukkitConfig.getString(CONFIG_COMPAT_EDIT_VALIDATION, def);
        if (editValidator == null) return def;
        if (!Arrays.asList(new String[]{"standard", "extra", "none"}).contains(editValidator.toLowerCase())) {
            return def;
        }
        return editValidator;
    }

    private void sanitizeConfig() {
        for (String configItem : defaults.keySet()) {
            bukkitConfig.set(configItem, get(configItem));
        }
    }
}