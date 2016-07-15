/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.LocatedSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.inject.Inject;
import java.util.Optional;

@Permissions(root = "world.border")
@RegisterCommand(value = {"gen", "genchunks", "generatechunks", "chunkgen"}, subcommandOf = BorderCommand.class)
public class GenerateChunksCommand extends CommandBase<CommandSource> {

    private final String worldKey = "world";

    @Inject
    private WorldHelper worldHelper;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.world(Text.of(worldKey))))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<WorldProperties> owp = args.getOne(worldKey);
        WorldProperties wp;
        if (owp.isPresent()) {
            wp = owp.get();
        } else {
            if (src instanceof LocatedSource) {
                wp = ((LocatedSource) src).getWorld().getProperties();
            } else {
                src.sendMessage(Util.getTextMessageWithFormat("command.world.setborder.noworld"));
                return CommandResult.empty();
            }
        }

        if (worldHelper.isPregenRunningForWorld(wp.getUniqueId())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.world.gen.alreadyrunning", wp.getWorldName()));
            return CommandResult.empty();
        }

        Optional<World> w = Sponge.getServer().getWorld(wp.getUniqueId());
        if (!w.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.world.gen.notloaded", wp.getWorldName()));
            return CommandResult.empty();
        }

        // Create the task.
        worldHelper.startPregenningForWorld(w.get());
        src.sendMessage(Util.getTextMessageWithFormat("command.world.gen.started", wp.getWorldName()));

        return CommandResult.success();
    }
}
