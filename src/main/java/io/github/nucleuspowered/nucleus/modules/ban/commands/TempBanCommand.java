/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanParser;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RegisterCommand("tempban")
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoWarmup
@NoCooldown
@NoCost
public class TempBanCommand extends CommandBase<CommandSource> {

    private final String user = "user";
    private final String reason = "reason";
    private final String duration = "duration";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments.user(Text.of(user))), GenericArguments.onlyOne(new TimespanParser(Text.of(duration))),
                GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of(reason)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(user).get();
        Long time = args.<Long>getOne(duration).get();
        String r = args.<String>getOne(reason).orElse(Util.getMessageWithFormat("ban.defaultreason"));

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        if (service.isBanned(u.getProfile())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.ban.alreadyset", u.getName()));
            return CommandResult.empty();
        }

        // Expiration date
        Instant date = Instant.now().plus(time, ChronoUnit.SECONDS);

        // Create the ban.
        Ban bp = Ban.builder().type(BanTypes.PROFILE).profile(u.getProfile()).source(src).expirationDate(date).reason(Text.of(r)).build();
        service.addBan(bp);

        MutableMessageChannel send = MessageChannel.permission(BanCommand.notifyPermission).asMutable();
        send.addMember(src);
        send.send(Util.getTextMessageWithFormat("command.tempban.applied", u.getName(), Util.getTimeStringFromSeconds(time), src.getName()));
        send.send(Util.getTextMessageWithFormat("standard.reason", reason));

        return CommandResult.success();
    }
}
