/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigSerializable
public class ChatConfig {

    @Setting(value = "modifychat", comment = "loc:config.chat.modify")
    private boolean modifychat = true;

    @Setting(value = "template", comment = "loc:config.chat.default-template")
    private ChatTemplateConfig template = new ChatTemplateConfig();

    @Setting(value = "group-templates", comment = "loc:config.chat.group-templates")
    private Map<String, ChatTemplateConfig> groupTemplates = new HashMap<String, ChatTemplateConfig>() {{
        // We don't want this affecting the default group, but we need an example.
        put("DefaultTemplate", new ChatTemplateConfig());
    }};

    public boolean isModifychat() {
        return modifychat;
    }

    public ChatTemplateConfig getTemplate(Player player) {
        List<Subject> groups = player.getSubjectData().getAllParents().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        groups.sort((x, y) -> y.getParents().size() - x.getParents().size());

        // Iterate through all groups the player is in.
        for (Subject group : groups) {
            if (groupTemplates.containsKey(group.getIdentifier())) {
                return groupTemplates.get(group.getIdentifier());
            }
        }

        // Return the default.
        return template;
    }
}
