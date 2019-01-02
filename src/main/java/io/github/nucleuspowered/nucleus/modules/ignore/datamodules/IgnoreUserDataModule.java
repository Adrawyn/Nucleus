/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ignore.datamodules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.storage.dataobjects.modules.IUserDataModule;

import java.util.List;
import java.util.UUID;

public class IgnoreUserDataModule implements IUserDataModule {

    @DataKey("ignoreList")
    private List<UUID> ignoreList = Lists.newArrayList();

    public List<UUID> getIgnoreList() {
        return ImmutableList.copyOf(this.ignoreList);
    }

    public boolean addToIgnoreList(UUID uuid) {
        if (!this.ignoreList.contains(uuid)) {
            this.ignoreList.add(uuid);
            return true;
        }

        return false;
    }

    public boolean removeFromIgnoreList(UUID uuid) {
        return this.ignoreList.remove(uuid);
    }

}
