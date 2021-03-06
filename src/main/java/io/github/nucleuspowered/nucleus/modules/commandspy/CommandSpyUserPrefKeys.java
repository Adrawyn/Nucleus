/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandspy;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.internal.userprefs.PreferenceKey;
import io.github.nucleuspowered.nucleus.internal.userprefs.UserPrefKeys;
import io.github.nucleuspowered.nucleus.modules.commandspy.commands.CommandSpyCommand;

public class CommandSpyUserPrefKeys implements UserPrefKeys {

    private final static String COMMAND_SPY_PERMISSION =
            Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(CommandSpyCommand.class).getBase();

    public static final PreferenceKey<Boolean> COMMAND_SPY = new PreferenceKey.BooleanKey(
            NucleusKeysProvider.COMMAND_SPY_KEY,
            false,
            COMMAND_SPY_PERMISSION,
            "userpref.commandspy"
    );

}
