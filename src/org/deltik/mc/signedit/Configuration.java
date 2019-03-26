package org.deltik.mc.signedit;

import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class Configuration {
    private File configFile;
    private YamlConfiguration yamlConfig;
    private static final String CONFIG_LINE_STARTS_AT = "line-starts-at";
    private static final String CONFIG_CLICKING = "clicking";
    private static final Map<String, Object> defaults;

    static {
        defaults = new HashMap<>();
        defaults.put(CONFIG_LINE_STARTS_AT, 1);
        defaults.put(CONFIG_CLICKING, "auto");
    }

    public File getConfigFile() {
        return configFile;
    }

    public YamlConfiguration getYamlConfig() {
        return yamlConfig;
    }

    @Inject
    public Configuration(SignEditPlugin plugin) {
        this("plugins//" + plugin.getName() + "//config.yml");
    }

    public Configuration(String f) {
        this(new File(f));
    }

    public Configuration(File f) {
        configFile = f;
        if (!configFile.exists()) {
            writeDefaultConfig();
        }
        yamlConfig = YamlConfiguration.loadConfiguration(configFile);
        sanitizeConfig(yamlConfig);
    }

    public boolean writeDefaultConfig() {
        YamlConfiguration c = new YamlConfiguration();
        mergeInDefaultConfig(c);
        return writeFullConfig(c);
    }

    private void mergeInDefaultConfig(YamlConfiguration c) {
        for (Map.Entry<String, Object> defaultConfigItem : defaults.entrySet()) {
            if (!c.contains(defaultConfigItem.getKey())) {
                c.set(defaultConfigItem.getKey(), defaultConfigItem.getValue());
            }
        }
    }

    public boolean writeFullConfig(YamlConfiguration c) {
        mergeInDefaultConfig(c);
        sanitizeConfig(c);
        try {
            c.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean writeFullConfig() {
        return writeFullConfig(yamlConfig);
    }

    public String getClicking() {
        return yamlConfig.getString(CONFIG_CLICKING, (String) defaults.get(CONFIG_CLICKING));
    }

    public void setClicking(String newValue) {
        yamlConfig.set(CONFIG_CLICKING, newValue);
        writeFullConfig(yamlConfig);
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
        return yamlConfig.getInt(CONFIG_LINE_STARTS_AT, (int) defaults.get(CONFIG_LINE_STARTS_AT));
    }

    public void setLineStartsAt(String newValue) {
        yamlConfig.set(CONFIG_LINE_STARTS_AT, Integer.parseInt(newValue));
        writeFullConfig(yamlConfig);
    }

    public int getMinLine() {
        return getLineStartsAt();
    }

    public int getMaxLine() {
        return getLineStartsAt() + 3;
    }

    private void sanitizeConfig(YamlConfiguration c) {
        int lineStartsAt = c.getInt(CONFIG_LINE_STARTS_AT);
        if (lineStartsAt < 0 || lineStartsAt > 1) setDefaultConfig(CONFIG_LINE_STARTS_AT);

        String clicking = c.getString(CONFIG_CLICKING);
        if (clicking == null ||
                !(clicking.equalsIgnoreCase("true") ||
                        clicking.equalsIgnoreCase("false") ||
                        clicking.equalsIgnoreCase("auto"))) setDefaultConfig(CONFIG_CLICKING);
    }

    private void setDefaultConfig(String path) {
        yamlConfig.set(path, defaults.get(path));
    }
}