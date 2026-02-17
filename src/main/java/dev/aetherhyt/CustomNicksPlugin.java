package dev.aetherhyt;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import dev.aetherhyt.command.NickCommand;
import dev.aetherhyt.command.NickResetCommand;
import dev.aetherhyt.config.PluginConfig;
import dev.aetherhyt.listener.ChatListener;
import dev.aetherhyt.listener.PlayerListener;
import dev.aetherhyt.manager.NicknameManager;
import dev.aetherhyt.util.PlayerRefUtil;

import javax.annotation.Nonnull;

public class CustomNicksPlugin extends JavaPlugin {
    public CustomNicksPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }
    
    private NicknameManager nicknameManager;

    @Override
    protected void start() {
        super.start();
        PlayerRefUtil.init();
        
        PluginConfig config = PluginConfig.load(this);
        this.nicknameManager = new NicknameManager(this, config);
        
        this.getCommandRegistry().registerCommand(new NickCommand(nicknameManager));
        this.getCommandRegistry().registerCommand(new NickResetCommand(nicknameManager));
        this.getEventRegistry().registerGlobal(com.hypixel.hytale.event.EventPriority.LAST, PlayerReadyEvent.class, new PlayerListener(nicknameManager));
        this.getEventRegistry().registerGlobal(com.hypixel.hytale.event.EventPriority.NORMAL, com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent.class, new ChatListener(nicknameManager));
    }
}
