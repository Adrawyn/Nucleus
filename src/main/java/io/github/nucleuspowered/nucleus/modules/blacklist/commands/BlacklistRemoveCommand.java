/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedCatalogTypeArgument;
import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;

@Permissions(root = "blacklist", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"remove", "delete", "del"}, subcommandOf = BlacklistCommand.class)
public class BlacklistRemoveCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    private final String item = "item";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new ImprovedCatalogTypeArgument(Text.of(item), CatalogTypes.ITEM_TYPE))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        ItemType itemType = args.<ItemType>getOne(item).get();
        GeneralService dataStore = this.plugin.getGeneralService();

        if (dataStore.getBlacklistedTypes().contains(itemType)) {
            dataStore.removeBlacklistedType(itemType);
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.blacklist.remove.success", itemType.getTranslation().get()));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.blacklist.remove.notadded", itemType.getTranslation().get()));
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
