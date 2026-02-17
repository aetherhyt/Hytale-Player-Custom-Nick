package dev.aetherhyt.listener;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;

import dev.aetherhyt.manager.NicknameManager;

import java.util.function.Consumer;

public class PlayerListener implements Consumer<PlayerReadyEvent> {

    private final NicknameManager nicknameManager;

    public PlayerListener(NicknameManager nicknameManager) {
        this.nicknameManager = nicknameManager;
    }

    @Override
    public void accept(PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        if (ref == null) return;
        
        // Find the player component from the store
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        
        if (playerRef != null) {
            System.out.println("[CustomNicks] PlayerReady: " + playerRef.getUsername());
            
            // Re-apply nickname to ensure everything is synced (Tab, Nameplate, Map)
            nicknameManager.applyNickname(playerRef);

            // Send welcome message if they have a nick
            String nickname = nicknameManager.getNickname(playerRef.getUuid());
            if (nickname != null && !nickname.isEmpty()) {
                playerRef.sendMessage(Message.raw("Your nickname is currently set to: " + nickname));
            }
        }
    }
}