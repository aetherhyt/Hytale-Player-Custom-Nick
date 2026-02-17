package dev.aetherhyt.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.protocol.packets.interface_.AddToServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.RemoveFromServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.ServerPlayerListPlayer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.Message;

import dev.aetherhyt.config.PluginConfig;
import dev.aetherhyt.util.PlayerRefUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameManager {
    private final File file;
    private final Gson gson;
    private final PluginConfig config;
    private Map<UUID, String> nicknames;

    public NicknameManager(JavaPlugin plugin, PluginConfig config) {
        this.file = plugin.getDataDirectory().resolve("nicknames.json").toFile();
        this.gson = new Gson();
        this.config = config;
        this.nicknames = new ConcurrentHashMap<>();
        load();
    }

    public String validate(String nickname, UUID ignoreUuid) {
        if (nickname.length() < config.minLength) {
            return "Nickname is too short. Minimum " + config.minLength + ".";
        }
        if (nickname.length() > config.maxLength) {
            return "Nickname is too long. Maximum " + config.maxLength + ".";
        }
        
        if (!nickname.matches(config.allowedRegex)) {
             return "Nickname contains invalid characters.";
        }

        for (String banned : config.bannedWords) {
            if (nickname.toLowerCase().contains(banned.toLowerCase())) {
                return "Nickname contains banned word.";
            }
        }

        if (config.uniqueNicknames) {
            for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
                if (entry.getKey().equals(ignoreUuid)) continue; 
                if (entry.getValue().equalsIgnoreCase(nickname)) {
                    return "This nickname is already in use.";
                }
            }
        }
        
        return null;
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<UUID, String>>(){}.getType();
            Map<UUID, String> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                this.nicknames = new ConcurrentHashMap<>(loaded);
            } else {
                 this.nicknames = new ConcurrentHashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(nicknames, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNickname(UUID uuid, String nickname) {
        if (nickname == null) {
            this.nicknames.remove(uuid);
        } else {
            this.nicknames.put(uuid, nickname);
        }
        save();
    }

    public String getNickname(UUID uuid) {
        return this.nicknames.get(uuid);
    }
    
    public void applyNickname(PlayerRef player) {
        if (player == null || !player.isValid()) return;
        
        String nickname = getNickname(player.getUuid());
        String displayName = (nickname != null && !nickname.isEmpty()) ? nickname : player.getUsername();
        
        // Update Nameplate (above head)
        try {
             Ref<EntityStore> ref = player.getReference();
             if (ref != null) {
                 // 1. Update DisplayNameComponent
                 DisplayNameComponent displayComp = new DisplayNameComponent(Message.raw(displayName));
                 ref.getStore().putComponent(ref, DisplayNameComponent.getComponentType(), displayComp);
                 
                 // 2. Update Nameplate component
                 Nameplate nameplate = ref.getStore().ensureAndGetComponent(ref, Nameplate.getComponentType());
                 nameplate.setText(displayName);
             }
        } catch (Exception e) {
             System.out.println("[CustomNicks] Failed to update nameplate components: " + e.getMessage());
        }

        // Update Tab List
        updateTabList(player, displayName);
        
        // Update Internal Username (Optional, for map markers etc)
        if (PlayerRefUtil.isAvailable()) {
            // Strip colors/tags for internal use
             String plainName = displayName.replaceAll("<[^>]*>", "");
             PlayerRefUtil.setUsername(player, plainName);
        }
    }

    public void updateTabList(PlayerRef player) {
        updateTabList(player, null);
    }

    public void updateTabList(PlayerRef player, String name) {
        if (name == null || name.isEmpty()) {
            name = player.getUsername();
        }
        try {
            // Remove old entry
            UUID[] uuids = new UUID[]{player.getUuid()};
            RemoveFromServerPlayerList removePacket = new RemoveFromServerPlayerList(uuids);
            
            // Create list entry for new name
            int ping = 0; // Default ping, will be updated by system
            ServerPlayerListPlayer listPlayer = new ServerPlayerListPlayer(
                player.getUuid(),
                name,
                player.getWorldUuid(),
                ping
            );
            
            // Add new entry with custom name
            ServerPlayerListPlayer[] listPlayers = new ServerPlayerListPlayer[]{listPlayer};
            AddToServerPlayerList addPacket = new AddToServerPlayerList(listPlayers);
            
            // Broadcast to all players via Universe
            Universe.get().broadcastPacket(removePacket);
            Universe.get().broadcastPacket(addPacket);
            
            System.out.println("[CustomNicks] Updated tab list for " + player.getUsername() + " to use " + name);
        } catch (Exception e) {
            System.out.println("[CustomNicks] Failed to update tab list: " + e.getMessage());
            e.printStackTrace();
        }
    }
}