package dev.aetherhyt.manager;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.aetherhyt.config.PluginConfig;

import java.util.function.Consumer;

public class ChatConfigManager implements Consumer<PlayerReadyEvent> {

    private final PluginConfig config;

    public ChatConfigManager(PluginConfig config) {
        this.config = config;
    }

    @Override
    public void accept(PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        if (ref == null) return;
        
        PlayerRef player = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        if (player == null) return;

        int duration = config.chatMessageDuration;
        
        String style = 
            "<style>" +
            // Attempt to override animation duration for chat messages
            // These selectors are educated guesses based on common UI structures
            ".chat-message, .chat-line, .message-entry {" +
            "  animation-duration: " + duration + "s !important;" +
            "  transition-duration: " + duration + "s !important;" +
            "  animation-delay: " + (duration - 1) + "s !important;" + // Delay fade out
            "}" +
            "</style>";

        // Send CustomUI command to append this style to the body
        CustomUICommand injectStyle = new CustomUICommand(
            CustomUICommandType.Append,
            "body",
            style,
            null
        );

        CustomHud packet = new CustomHud(false, new CustomUICommand[]{injectStyle});
        if (player.getPacketHandler() != null) {
            player.getPacketHandler().write(packet);
            System.out.println("[CustomNicks] Injected chat configuration for " + player.getUsername());
        }
    }
}
