package org.deltik.mc.signedit;

import com.google.common.io.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;

@Singleton
public class Configuration {
    private File configFile;
    private FileConfiguration bukkitConfig;
    private JtwigTemplate template;
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
    public Configuration(SignEditPlugin plugin) {
        this("plugins//" + plugin.getName() + "//config.yml");
    }

    public Configuration(String f) {
        this(new File(f));
    }

    public Configuration(File f) {
        configFile = f;
        template = JtwigTemplate.classpathTemplate("config.yml.j2");
        try {
            reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() throws IOException {
        if (!configFile.exists()) {
            writeDefaultConfig();
        }
        bukkitConfig = YamlConfiguration.loadConfiguration(configFile);
        writeSaneConfig(bukkitConfig);
    }

    public void writeDefaultConfig() throws IOException {
        YamlConfiguration c = new YamlConfiguration();
        mergeInDefaultConfig(c);
        writeSaneConfig(c);
    }

    private void mergeInDefaultConfig(FileConfiguration c) {
        for (Map.Entry<String, Object> defaultConfigItem : defaults.entrySet()) {
            if (!c.contains(defaultConfigItem.getKey())) {
                c.set(defaultConfigItem.getKey(), defaultConfigItem.getValue());
            }
        }
    }

    public void writeSaneConfig(FileConfiguration c) throws IOException {
        mergeInDefaultConfig(c);
        sanitizeConfig(c);
        Map<String, Object> yamlContext = c.getValues(true);
        Map<String, Object> jinjaContext = new HashMap<>();
        for (Map.Entry<String, Object> entry : yamlContext.entrySet()) {
            String key = entry.getKey();
            String newKey = key.replaceAll("-", "_");
            jinjaContext.put(newKey, yamlContext.get(key));
        }
        JtwigModel model = JtwigModel.newModel(jinjaContext);
        Files.createParentDirs(configFile);
        OutputStream configFileOut = new FileOutputStream(configFile);
        template.render(model, configFileOut);
        configFileOut.close();
    }

    public void writeSaneConfig() throws IOException {
        writeSaneConfig(bukkitConfig);
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

    private void sanitizeConfig(FileConfiguration c) {
        String clicking = c.getString(CONFIG_CLICKING);
        if (clicking == null ||
                !(clicking.equalsIgnoreCase("true") ||
                        clicking.equalsIgnoreCase("false") ||
                        clicking.equalsIgnoreCase("auto"))) setDefaultConfig(CONFIG_CLICKING);

        int lineStartsAt = c.getInt(CONFIG_LINE_STARTS_AT);
        if (lineStartsAt < 0 || lineStartsAt > 1) setDefaultConfig(CONFIG_LINE_STARTS_AT);

        try {
            getLocale();
        } catch (IllformedLocaleException | NullPointerException e) {
            setDefaultConfig(CONFIG_LOCALE);
        }

        if (!c.isBoolean(CONFIG_FORCE_LOCALE)) {
            setDefaultConfig(CONFIG_FORCE_LOCALE);
        }
    }

    private void setDefaultConfig(String path) {
        bukkitConfig.set(path, defaults.get(path));
    }
}