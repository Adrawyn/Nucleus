/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.config.AbstractAdaptableConfig;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

public abstract class NucleusConfigAdapter<R> extends AbstractConfigAdapter<R> {

    @Inject private Nucleus plugin;

    @Override
    @SuppressWarnings("unchecked")
    public void onAttach(String module, AbstractAdaptableConfig<?, ?> adapter) {
        plugin.preInjectorUpdate((Class)this.getClass(), this);
    }

    public final R getNodeOrDefault() {
        try {
            R node = getNode();
            if (node != null) {
                return node;
            }
        } catch (ObjectMappingException e) {
            //
        }

        return getDefaultObject();
    }

    protected abstract R getDefaultObject();

    @Override
    @SuppressWarnings("unchecked")
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        R o = getDefaultObject();
        try {
            return node.setValue(TypeToken.of((Class<R>)o.getClass()), o);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return node;
        }
    }
}
