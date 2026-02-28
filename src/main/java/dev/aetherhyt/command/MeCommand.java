package dev.aetherhyt.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import dev.aetherhyt.manager.NicknameManager;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MeCommand extends AbstractAsyncCommand {

    private final NicknameManager nicknameManager;
    private final RequiredArg<String> textArg;

    public MeCommand(NicknameManager nicknameManager) {
        // Command name "me", permission "customnicks.me"
        super("me", "customnicks.me");
        this.nicknameManager = nicknameManager;
        // The argument is the action text (e.g. "runs away")
        // Use JOINED_STRING if available or STRING and join manually?
        // ArgTypes.STRING usually takes one word. Ideally we want the rest of the line.
        // Hytale command system usually requires strings with spaces to be quoted if using STRING.
        // However, standard /me commands take everything after the command.
        // Let's use STRING for now and if available JOINED_STRING would be better, but assuming standard implementation:
        this.textArg = this.withRequiredArg("action", "The action description", ArgTypes.STRING);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Console cannot use /me"));
            return CompletableFuture.completedFuture(null);
        }

        String action = context.get(textArg);
        com.hypixel.hytale.component.Ref<EntityStore> senderRef = context.senderAsPlayerRef();

        assert senderRef != null;
        return CompletableFuture.runAsync(() -> {
            PlayerRef sender = senderRef.getStore().getComponent(senderRef, PlayerRef.getComponentType());
            
            String displayName = sender.getUsername();
            String nick = nicknameManager.getNickname(sender.getUuid());
            if (nick != null && !nick.isEmpty()) {
                displayName = nick;
            }

            String finalAction = stripQuotes(action);
            String broadcastMsg = "* " + displayName + " " + finalAction;

            for (PlayerRef p : Universe.get().getPlayers()) {
                p.sendMessage(Message.raw(broadcastMsg));
            }
            
        }, senderRef.getStore().getExternalData().getWorld());
    }

    private String stripQuotes(String str) {
        if (str != null && str.length() > 1 && str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}
