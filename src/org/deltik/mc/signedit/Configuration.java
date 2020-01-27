/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit;

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
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;

@Singleton
public class Configuration {
    private File configFile;
    private FileConfiguration bukkitConfig;
    private String template;
    private static final String CONFIG_CLICKING = "clicking";
    private static final String CONFIG_LINE_STARTS_AT = "line-starts-at";
    private static final String CONFIG_FORCE_LOCALE = "force-locale";
    private static final String CONFIG_LOCALE = "locale";
    private static final Map<String, Object> defaults;

    static {
        defaults = new HashMap<>();
        defaults.put(CONFIG_CLICKING, "auto");
        defaults.put(CONFIG_LINE_STARTS_AT, 1);
        defaults.put(CONFIG_FORCE_LOCALE, false);
        defaults.put(CONFIG_LOCALE, "en");
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
    }

    public void prepare() throws IOException {
        InputStream templateStream = getClass().getResourceAsStream("/config.yml.j2");
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = templateStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        template = result.toString("UTF-8");
        reloadConfig();
    }

    public void reloadConfig() throws IOException {
        if (!configFile.exists()) {
            writeDefaultConfig();
        }
        bukkitConfig = YamlConfiguration.loadConfiguration(configFile);
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
            String newKey = key.replaceAll("-", "_");
            output = output.replaceAll("\\{\\{[ ]*" + newKey + "[ ]*}}", yamlContext.get(key).toString());
        }

        File configFileParent = configFile.getCanonicalFile().getParentFile();
        if (configFileParent != null) {
            configFileParent.mkdirs();
        }
        Files.write(configFile.toPath(), output.getBytes());
    }

    public String getClicking() {
        return bukkitConfig.getString(CONFIG_CLICKING, (String) defaults.get(CONFIG_CLICKING));
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
        return bukkitConfig.getInt(CONFIG_LINE_STARTS_AT, (int) defaults.get(CONFIG_LINE_STARTS_AT));
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

    public boolean getforceLocale() {
        return bukkitConfig.getBoolean(CONFIG_FORCE_LOCALE);
    }

    public Locale getLocale() {
        String languageTag = bukkitConfig.getString(CONFIG_LOCALE);
        if (languageTag == null) languageTag = (String) defaults.get(CONFIG_LOCALE);
        return new Locale.Builder().setLanguageTag(languageTag).build();
    }

    private void sanitizeConfig() {
        String clicking = bukkitConfig.getString(CONFIG_CLICKING);
        if (clicking == null ||
                !(clicking.equalsIgnoreCase("true") ||
                        clicking.equalsIgnoreCase("false") ||
                        clicking.equalsIgnoreCase("auto"))) setDefaultConfig(CONFIG_CLICKING);

        int lineStartsAt = bukkitConfig.getInt(CONFIG_LINE_STARTS_AT);
        if (lineStartsAt < 0 || lineStartsAt > 1) setDefaultConfig(CONFIG_LINE_STARTS_AT);

        try {
            getLocale();
        } catch (IllformedLocaleException | NullPointerException e) {
            setDefaultConfig(CONFIG_LOCALE);
        }

        if (!bukkitConfig.isBoolean(CONFIG_FORCE_LOCALE)) {
            setDefaultConfig(CONFIG_FORCE_LOCALE);
        }
    }

    private void setDefaultConfig(String path) {
        bukkitConfig.set(path, defaults.get(path));
    }
}