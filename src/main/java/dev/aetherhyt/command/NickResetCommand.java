package dev.aetherhyt.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.component.Ref;

import dev.aetherhyt.manager.NicknameManager;

import java.util.concurrent.CompletableFuture;

public class NickResetCommand extends AbstractAsyncCommand {

    private final NicknameManager nicknameManager;
    private final OptionalArg<String> targetArg;

    public NickResetCommand(NicknameManager nicknameManager) {
        super("nickreset", "customnicks.nick");
        this.nicknameManager = nicknameManager;
        this.targetArg = this.withOptionalArg("target", "The target player", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        String targetName = context.get(targetArg);
        
        if (context.isPlayer()) {
             com.hypixel.hytale.component.Ref<EntityStore> senderRef = context.senderAsPlayerRef();
             World world = senderRef.getStore().getExternalData().getWorld();
             
             return CompletableFuture.runAsync(() -> {
                 PlayerRef target = null;
                 
                  if (targetName != null) {
                       // Target specified - requires OP
                      if (!context.sender().hasPermission("customnicks.nick.others")) {
                           context.sendMessage(Message.raw("You do not have permission to reset other players' nicknames."));
                           return;
                      }
                      
                      // Find player in world
                      for (PlayerRef p : world.getPlayerRefs()) {
                           if (p.getUsername().equalsIgnoreCase(targetName)) {
                               target = p;
                               break;
                           }
                      }
                  } else {
                      // No target -> Reset self
                      target = senderRef.getStore().getComponent(senderRef, PlayerRef.getComponentType());
                  }

                 if (target == null) {
                    context.sendMessage(Message.raw("Player not found: " + targetName));
                    return;
                }

                // Reset Logic
                nicknameManager.setNickname(target.getUuid(), null);
                
                DisplayNameComponent component = new DisplayNameComponent(Message.raw(target.getUsername()));
                Ref<EntityStore> ref = target.getReference();
                if (ref != null) {
                    System.out.println("[CustomNicks] NickResetCommand: Removing nickname for " + target.getUsername());
                    ref.getStore().replaceComponent(ref, DisplayNameComponent.getComponentType(), component);
                    nicknameManager.updateTabList(target);
                    context.sendMessage(Message.raw("Reset nickname for " + target.getUsername()));
                } else {
                    System.out.println("[CustomNicks] NickResetCommand: Failed to get reference for " + target.getUsername());
                    context.sendMessage(Message.raw("Could not find player reference."));
                }
            }, world);

        } else {
             context.sendMessage(Message.raw("Console must specify a target."));
             return CompletableFuture.completedFuture(null);
        }
    }
}