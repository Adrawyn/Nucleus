/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.dataservices.UserService;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions
@NoCooldown
@NoWarmup
@NoCost
@RegisterCommand({"god", "invuln", "invulnerability"})
public class GodCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    private final String playerKey = "player";
    private final String invulnKey = "invuln";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments
                        .requiringPermission(GenericArguments.player(Text.of(playerKey)), permissions.getPermissionWithSuffix("others")))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(invulnKey))))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, src, playerKey, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        Player pl = opl.get();

        UserService uc = plugin.getUserDataManager().get(pl).get();
        boolean god = args.<Boolean>getOne(invulnKey).orElse(!uc.isInvulnerable());

        uc.setInvulnerable(god);
        if (!pl.equals(src)) {
            src.sendMessages(plugin.getMessageProvider().getTextMessageWithFormat(god ? "command.god.player.on" : "command.god.player.off", pl.getName()));
        }

        pl.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat(god ? "command.god.on" : "command.god.off"));
        return CommandResult.success();
    }
}
