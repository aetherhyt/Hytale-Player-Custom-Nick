package dev.aetherhyt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginConfig {
    public int minLength = 3;
    public int maxLength = 20;
    public boolean allowCyrillic = true;
    public boolean uniqueNicknames = true;
    public List<String> bannedWords = new ArrayList<>();
    
    // Configurable regex for allowed characters
    // Default allows A-Z, 0-9, Underscores, and Cyrillic/ASCII extended
    public String allowedRegex = "^[a-zA-Z0-9_\\u00C0-\\u017F\\u0400-\\u04FF ]+$";

    private transient File file;
    private static final transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static PluginConfig load(JavaPlugin plugin) {
        File f = plugin.getDataDirectory().resolve("config.json").toFile();
        
        if (!f.exists()) {
            PluginConfig config = new PluginConfig();
            config.file = f;
            config.bannedWords.add("admin");
            config.bannedWords.add("staff");
            config.bannedWords.add("badword");
            config.save();
            return config;
        }

        try (FileReader reader = new FileReader(f)) {
            PluginConfig config = gson.fromJson(reader, PluginConfig.class);
            config.file = f;
            return config;
        } catch (IOException e) {
            System.err.println("[CustomNicks] Failed to load config, using defaults.");
            e.printStackTrace();
            PluginConfig config = new PluginConfig();
            config.file = f;
            return config;
        }
    }

    public void save() {
        try {
            if (file != null) {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(this, writer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}