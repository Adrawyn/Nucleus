/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.craftinggui.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.modules.craftinggui.BasicCraftingCommand;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions
@RegisterCommand({"enchantingtable", "enchanttable", "etable"})
@NonnullByDefault
public class EnchantingTableCommand extends BasicCraftingCommand {

    // Max power is 32, when we can implement it.
    @Override protected InventoryArchetype getArchetype() {
        return InventoryArchetypes.ENCHANTING_TABLE;
    }
}
