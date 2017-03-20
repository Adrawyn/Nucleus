/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.nickname.NicknameModule;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import io.github.nucleuspowered.nucleus.util.QuadFunction;
import io.github.nucleuspowered.nucleus.util.ThrownTriFunction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
public class NicknameArgument<T extends User> extends CommandElement {

    private final UserDataManager userDataManager;
    private final ThrownTriFunction<String, CommandSource, CommandArgs, List<?>, ArgumentParseException> parser;
    private final QuadFunction<String, CommandSource, CommandArgs, CommandContext, List<String>> completer;
    private final boolean onlyOne;
    private final UnderlyingType type;
    private final BiPredicate<CommandSource, T> filter;

    public NicknameArgument(@Nullable Text key, UnderlyingType<T> type) {
        this(key, type, true);
    }

    public NicknameArgument(@Nullable Text key, UnderlyingType<T> type, boolean onlyOne) {
        this(key, type, onlyOne, (s, c) -> true);
    }

    @SuppressWarnings("unchecked")
    public NicknameArgument(@Nullable Text key, UnderlyingType<T> type, boolean onlyOne,
            BiPredicate<CommandSource, T> filter) {
        super(key);

        this.onlyOne = onlyOne;
        this.userDataManager = Nucleus.getNucleus().getUserDataManager();
        this.type = type;
        this.filter = filter;

        if (type == UnderlyingType.USER) {
            parser = new UserParser(onlyOne, () -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class));
            completer = (s, cs, a, c) -> {
                Collection<String> onlinePlayers = Sponge.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                return Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                    .getAll()
                    .stream()
                    .filter(x -> x.getName().isPresent() && x.getName().get().toLowerCase().startsWith(s))
                    .filter(x -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(x).map(y -> filter.test(cs, (T)y))
                            .orElse(false))
                    .filter(x -> PlayerConsoleArgument.shouldShow(x.getUniqueId(), cs))
                    .map(x -> x.getName().get())
                    .distinct()
                    .sorted((first, second) -> {
                        boolean firstBool = onlinePlayers.contains(first);
                        boolean secondBool = onlinePlayers.contains(second);
                        if (firstBool == secondBool) {
                            return first.compareTo(second);
                        }

                        return firstBool ? -1 : 1;
                    })
                    .collect(Collectors.toList());
            };
        } else {
            PlayerConsoleArgument pca = new PlayerConsoleArgument(key, type == UnderlyingType.PLAYER_CONSOLE,
                    (BiPredicate<CommandSource, Player>)filter);
            parser = pca::parseInternal;
            completer = pca::completeInternal;
        }
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String name = args.next().toLowerCase();
        return parseInternal(name, source, args);
    }

    @SuppressWarnings("unchecked")
    List<?> parseInternal(String name, CommandSource src, CommandArgs args) throws ArgumentParseException {
        boolean playerOnly = name.startsWith("p:");

        final String fName;
        if (playerOnly) {
            fName = name.split(":", 2)[1];
        } else {
            fName = name;
        }

        List<?> obj = null;
        try {
            obj = parser.accept(fName, src, args);
        } catch (ArgumentParseException ex) {
            // ignored
        }

        if (obj != null && !obj.isEmpty()) {
            return obj;
        } else if (playerOnly) {
            // Rethrow;
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.nouser", fName));
        }

        // Now check user names
        // TODO: Display name
        Map<String, ModularUserService> allPlayers;
        if (Nucleus.getNucleus().isModuleLoaded(NicknameModule.ID)) {
            allPlayers = userDataManager.getOnlineUsersInternal().stream()
                    .filter(x -> x.getUser().isOnline() && x.get(NicknameUserDataModule.class).getNicknameAsString().isPresent())
                    .collect(Collectors.toMap(s -> TextSerializers.FORMATTING_CODE.stripCodes(s.get(NicknameUserDataModule.class)
                    .getNicknameAsString().get().toLowerCase()), s -> s));
        } else {
            allPlayers = Maps.newHashMap();
        }

        if (allPlayers.containsKey(fName.toLowerCase())) {
            return Lists.newArrayList(allPlayers.get(fName.toLowerCase()).getUser().getPlayer().get());
        }

        List<Player> players = allPlayers.entrySet().stream()
            .filter(x -> x.getKey().toLowerCase().startsWith(fName))
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .filter(x -> x.getValue().getUser().getPlayer().isPresent())
            .map(x -> x.getValue().getUser().getPlayer().get())
            .filter(x -> filter.test(src, (T)x))
            .collect(Collectors.toList());

        if (players.isEmpty()) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(type == UnderlyingType.PLAYER_CONSOLE ? "args.playerconsole.nouser" : "args.user.nouser", fName));
        } else if (players.size() > 1 && this.onlyOne) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.toomany", fName));
        }

        // We know they are online.
        return players;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String name;
        try {
            name = args.peek().toLowerCase();
        } catch (ArgumentParseException e) {
            name = "";
        }

        boolean playerOnly = name.startsWith("p:");
        final String fName;
        if (playerOnly) {
            fName = name.split(":", 2)[1];
        } else {
            fName = name;
        }

        List<String> original = completer.accept(fName, src, args, context);
        if (playerOnly) {
            return original.stream().map(x -> "p:" + x).collect(Collectors.toList());
        } else if (Nucleus.getNucleus().isModuleLoaded(NicknameModule.ID)) {
            original.addAll(userDataManager.getOnlineUsersInternal().stream()
                    .filter(x -> x.getUser().isOnline() && x.get(NicknameUserDataModule.class).getNicknameAsString().isPresent() &&
                            TextSerializers.FORMATTING_CODE.stripCodes(x.get(NicknameUserDataModule.class).getNicknameAsString().get())
                                    .toLowerCase().startsWith(fName))
                    .map(x -> TextSerializers.FORMATTING_CODE.stripCodes(x.get(NicknameUserDataModule.class).getNicknameAsString().get()))
                    .collect(Collectors.toList()));
        }

        return original;
    }

    public static class UnderlyingType<U extends User> {
        public static final UnderlyingType<Player> PLAYER = new UnderlyingType<>();
        public static final UnderlyingType<Player> PLAYER_CONSOLE = new UnderlyingType<>();
        public static final UnderlyingType<User> USER = new UnderlyingType<>();
    }

    public static final class UserParser implements ThrownTriFunction<String, CommandSource, CommandArgs, List<?>, ArgumentParseException> {

        private final boolean onlyOne;
        private final Supplier<UserStorageService> userStorageServiceSupplier;
        private final BiPredicate<CommandSource, User> filter;

        public UserParser(boolean onlyOne, Supplier<UserStorageService> userStorageServiceSupplier) {
            this(onlyOne, userStorageServiceSupplier, (c, s) -> true);
        }

        public UserParser(boolean onlyOne, Supplier<UserStorageService> userStorageServiceSupplier, BiPredicate<CommandSource, User> filter) {
            this.onlyOne = onlyOne;
            this.userStorageServiceSupplier = userStorageServiceSupplier;
            this.filter = filter;
        }

        @Override
        public List<?> accept(String s, CommandSource cs, CommandArgs a) throws ArgumentParseException {
            try {
                UserStorageService uss = userStorageServiceSupplier.get();
                if (onlyOne) {
                    return Lists.newArrayList(uss.get(s)
                        .orElseThrow(() -> a.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.toomany", s))));
                }

                List<User> users = uss.getAll().stream()
                        // Get the players who start with the string.
                        .filter(x -> x.getName().filter(y -> y.toLowerCase().startsWith(s.toLowerCase())).isPresent())
                        .map(uss::get)
                        // Remove players who have no user
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(x -> filter.test(cs, x))
                        .filter(x -> PlayerConsoleArgument.shouldShow(x.getUniqueId(), cs))
                        .collect(Collectors.toList());

                if (!users.isEmpty()) {
                    List<User> exactUser = users.stream().filter(x -> x.getName().equalsIgnoreCase(s)).collect(Collectors.toList());
                    if (exactUser.size() == 1) {
                        return exactUser;
                    }

                    return users;
                }
            } catch (Exception e) {
                // We want to rethrow this!
                if (e instanceof ArgumentParseException) {
                    throw e;
                }
            }

            return Lists.newArrayList();
        }
    }
}
