package dev.aetherhyt.listener;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;

import dev.aetherhyt.manager.NicknameManager;

import java.util.function.Consumer;

public class ChatListener implements Consumer<PlayerChatEvent> {

    private final NicknameManager nicknameManager;

    public ChatListener(NicknameManager nicknameManager) {
        this.nicknameManager = nicknameManager;
    }

    @Override
    public void accept(PlayerChatEvent event) {
        // Set a custom formatter to handle nickname display in chat
        event.setFormatter((sender, message) -> {
            // Check for nickname component using the correct reference chain
            com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref = sender.getReference();
            DisplayNameComponent component = null;
            
            if (ref != null && ref.getStore() != null) {
                try {
                     component = ref.getStore().getComponent(ref, DisplayNameComponent.getComponentType());
                } catch (Exception e) {
                    System.out.println("[CustomNicks] Error retrieving component: " + e.getMessage());
                }
            }
            
            String nameToDisplay = sender.getUsername();
            
            // Try nickname manager first - this is the source of truth
            String managerNick = nicknameManager.getNickname(sender.getUuid());
            if (managerNick != null && !managerNick.isEmpty()) {
                nameToDisplay = managerNick;
                System.out.println("[CustomNicks] ChatListener: Found nickname in manager: " + nameToDisplay);
            } else if (component != null && component.getDisplayName() != null) {
                // Fallback to component if present (e.g. set by other means)
                nameToDisplay = component.getDisplayName().getRawText();
                System.out.println("[CustomNicks] ChatListener: Found nickname in component: " + nameToDisplay);
            } else {
                System.out.println("[CustomNicks] ChatListener: No nickname found for " + sender.getUsername() + ", using default.");
            }

            // Return the formatted message
            // Format: <Nickname>: Message
            return Message.raw("<" + nameToDisplay + "> " + message);
        });
    }
}