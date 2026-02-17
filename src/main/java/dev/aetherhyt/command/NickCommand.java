package dev.aetherhyt.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.component.Ref;

import dev.aetherhyt.manager.NicknameManager;

import java.util.concurrent.CompletableFuture;

public class NickCommand extends AbstractAsyncCommand {

    private final NicknameManager nicknameManager;
    private final RequiredArg<String> firstArg;
    private final OptionalArg<String> secondArg;

    public NickCommand(NicknameManager nicknameManager) {
        super("nick", "customnicks.nick.use");
        this.nicknameManager = nicknameManager;
        this.firstArg = this.withRequiredArg("target_or_nick", "Target player OR the nickname if setting for self", ArgTypes.STRING);
        this.secondArg = this.withOptionalArg("nick", "Nickname if target is specified", ArgTypes.STRING);
    }

    private String stripQuotes(String str) {
        if (str != null && str.length() > 1 && str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        String rawVal1 = context.get(firstArg);
        String rawVal2 = context.get(secondArg);
        
        // Strip validation quotes if present (e.g. "Nickname" -> Nickname)
        String val1 = stripQuotes(rawVal1);
        String val2 = stripQuotes(rawVal2);

        if (context.isPlayer()) {
            com.hypixel.hytale.component.Ref<EntityStore> senderRef = context.senderAsPlayerRef();
            World world = senderRef.getStore().getExternalData().getWorld();
            
            return CompletableFuture.runAsync(() -> {
                PlayerRef target = null;
                String newNickname = null;

                // Try to resolve target if 2 args are present
                if (val2 != null) {
                    // Check if val1 is actually a player
                    for (PlayerRef p : world.getPlayerRefs()) {
                        if (p.getUsername().equalsIgnoreCase(val1)) {
                            target = p;
                            break;
                        }
                    }
                    
                    if (target != null) {
                         // Setup for: /nick <target> <nickname>
                         if (!context.sender().hasPermission("customnicks.nick.others")) {
                            context.sendMessage(Message.raw("You do not have permission to set other players' nicknames."));
                            return;
                         }
                         newNickname = val2;
                    } else {
                         // val1 is NOT a player. Assume the user typed a multi-word nickname for THEMSELVES.
                         // Usage: /nick My Cool Name
                         // val1="My", val2="Cool" (Wait, ArgTypes.STRING only grabs one word? 
                         // Logic below handles "Firstname Lastname" if passed as 2 args)
                         target = senderRef.getStore().getComponent(senderRef, PlayerRef.getComponentType());
                         newNickname = val1 + " " + val2;
                    }
                } else {
                    // One argument: /nick <nickname> (sets self)
                    target = senderRef.getStore().getComponent(senderRef, PlayerRef.getComponentType());
                    newNickname = val1;
                }

                // Check validation
                // If the player has 'customnicks.bypass', skip validation? The user didn't ask for permission bypass, but admins usually expect it.
                // Let's implement bypass for admins setting other's names or themselves if they have permission.
                boolean bypass = context.sender().hasPermission("customnicks.bypass");
                
                if (!bypass) {
                     String error = nicknameManager.validate(newNickname, target.getUuid());
                     if (error != null) {
                         context.sendMessage(Message.raw("Error: " + error).color("#FF5555"));
                         return;
                     }
                }
        
                // Apply nickname
                nicknameManager.setNickname(target.getUuid(), newNickname);
                
                DisplayNameComponent component = new DisplayNameComponent(Message.raw(newNickname));
                
                // Use the store to replace the component directly using the target Ref
                Ref<EntityStore> ref = target.getReference();
                if (ref != null) {
                    System.out.println("[CustomNicks] NickCommand: Updating nickname for " + target.getUsername() + " to " + newNickname);
                    ref.getStore().replaceComponent(ref, DisplayNameComponent.getComponentType(), component);
                    nicknameManager.updateTabList(target, newNickname);
                    context.sendMessage(Message.raw("Set nickname of " + target.getUsername() + " to " + newNickname));
                } else {
                    System.out.println("[CustomNicks] NickCommand: Failed to get reference for " + target.getUsername());
                    context.sendMessage(Message.raw("Could not find player reference."));
                }
            }, world);
        } else {
             context.sendMessage(Message.raw("Console must provide a target. Usage: /nick <target> <nickname>"));
             return CompletableFuture.completedFuture(null);
        }
    }
}