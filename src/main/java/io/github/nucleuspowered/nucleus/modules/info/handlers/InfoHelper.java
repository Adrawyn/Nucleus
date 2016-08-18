/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.handlers;

import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class InfoHelper {

    private static final Text padding = Text.of(TextColors.GOLD, "-");
    private static final Text emptyPadding = Text.of(" ");

    public static void sendInfo(TextFileController tfc, CommandSource src, ChatUtil chatUtil, String motdTitle) {
        sendInfo(tfc.getFileContents(), src, chatUtil, motdTitle);
    }

    public static void sendInfo(List<String> tfc, CommandSource src, ChatUtil chatUtil, String motdTitle) {
        // Get the text.
        List<Text> textList = chatUtil.getFromStrings(tfc, src);

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        PaginationList.Builder pb = ps.builder().contents(textList);

        if (!motdTitle.isEmpty()) {
            pb.title(TextSerializers.FORMATTING_CODE.deserialize(motdTitle)).padding(padding);
        } else {
            pb.padding(emptyPadding);
        }

        if (src instanceof ConsoleSource) {
            pb.linesPerPage(-1);
        }

        pb.sendTo(src);
    }
}