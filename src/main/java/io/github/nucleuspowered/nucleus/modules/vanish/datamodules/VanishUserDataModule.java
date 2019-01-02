/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.storage.dataobjects.modules.IUserDataModule;

public class VanishUserDataModule implements IUserDataModule {

    @DataKey("vanish")
    private boolean vanish = false;

    public boolean isVanished() {
        return this.vanish;
    }

    public void setVanished(boolean vanished) {
        this.vanish = vanished;
    }
}
