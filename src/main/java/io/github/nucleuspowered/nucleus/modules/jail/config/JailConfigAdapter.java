/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.List;

public class JailConfigAdapter extends NucleusConfigAdapter<JailConfig> {

    @Override
    protected List<Transformation> getTransformations() {
        return Lists.newArrayList(
            new Transformation(new Object[] { "allowedCommands" }, ((inputPath, valueAtPath) -> new Object[] { "allowed-commands" }))
        );
    }

    @Override
    protected JailConfig getDefaultObject() {
        return new JailConfig();
    }

    @Override
    protected JailConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(JailConfig.class), new JailConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(JailConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(JailConfig.class), data);
    }
}
