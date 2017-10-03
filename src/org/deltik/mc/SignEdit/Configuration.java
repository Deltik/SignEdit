package org.deltik.mc.SignEdit;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    File configFile;
    YamlConfiguration yamlConfig;
    private static final Map<String, Object> defaults;
    static {
        defaults = new HashMap<String, Object>();
        defaults.put("line-starts-at", 1);
        defaults.put("clicking", "false");
    }

    public File getConfigFile() {
        return configFile;
    }

    public YamlConfiguration getYamlConfig() {
        return yamlConfig;
    }

    public Configuration() {
        this("plugins//" + Main.instance.getName() + "//config.yml");
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
        return yamlConfig.getString("clicking", (String) defaults.get("clicking"));
    }

    public void setClicking(String newValue) {
        yamlConfig.set("clicking", newValue);
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
        return yamlConfig.getInt("line-starts-at", (int) defaults.get("line-starts-at"));
    }

    public void setLineStartsAt(String newValue) {
        yamlConfig.set("line-starts-at", Integer.parseInt(newValue));
        writeFullConfig(yamlConfig);
    }

    public int getMinLine() {
        return getLineStartsAt();
    }

    public int getMaxLine() {
        return getLineStartsAt()+3;
    }

    private void sanitizeConfig(YamlConfiguration c) {
        int lineStartsAt = c.getInt("line-starts-at");
        if (lineStartsAt < 0 || lineStartsAt > 1) setDefaultConfig("line-starts-at");

        String clicking = c.getString("clicking");
        if (clicking == null ||
                !(clicking.equalsIgnoreCase("true") ||
                clicking.equalsIgnoreCase("false") ||
                clicking.equalsIgnoreCase("auto"))) setDefaultConfig("clicking");
    }

    private void setDefaultConfig(String path) {
        yamlConfig.set(path, defaults.get(path));
    }
}
