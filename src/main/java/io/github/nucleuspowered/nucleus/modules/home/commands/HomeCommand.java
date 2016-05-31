/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeParser;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.back.handlers.BackHandler;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("home")
public class HomeCommand extends CommandBase<Player> {

    private final String home = "home";

    @Inject private CoreConfigAdapter cca;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.optional(new HomeParser(Text.of(home), plugin, cca)))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Optional<WarpLocation> owl = args.getOne(home);
        if (!owl.isPresent()) {
            owl = plugin.getUserLoader().getUser(src).getHome("home");

            if (!owl.isPresent()) {
                src.sendMessage(Util.getTextMessageWithFormat("args.home.nohome", "home"));
                return CommandResult.empty();
            }
        }

        Transform<World> currentLocation = src.getTransform();
        WarpLocation wl = owl.get();

        if (wl.getLocation() == null) {
            // Fail
            src.sendMessage(Util.getTextMessageWithFormat("command.home.invalid", wl.getName()));
            return CommandResult.empty();
        }

        // Warp to it safely.
        if (src.setLocationAndRotationSafely(wl.getLocation(), wl.getRotation())) {
            setLastLocation(src, currentLocation);
            if (!wl.getName().equalsIgnoreCase("home")) {
                src.sendMessage(Util.getTextMessageWithFormat("command.home.success", wl.getName()));
            } else {
                src.sendMessage(Util.getTextMessageWithFormat("command.home.successdefault"));
            }

            return CommandResult.success();
        } else {
            src.sendMessage(Util.getTextMessageWithFormat("command.home.fail", wl.getName()));
            return CommandResult.empty();
        }
    }

    @SuppressWarnings("deprecation")
    private void setLastLocation(Player player, Transform<World> location) {
        Optional<BackHandler> backHandler = plugin.getInternalServiceManager().getService(BackHandler.class);
        if (backHandler.isPresent()) {
            backHandler.get().setLastLocationInternal(player, location);
        }
    }
}
