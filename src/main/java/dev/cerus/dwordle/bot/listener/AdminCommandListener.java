package dev.cerus.dwordle.bot.listener;

import dev.cerus.dwordle.bot.DWordleBot;
import dev.cerus.dwordle.game.GameController;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * /wordle command listener
 */
public class AdminCommandListener extends ListenerAdapter {

    private final GameController gameController;
    private final DWordleBot bot;

    public AdminCommandListener(final GameController gameController,
                                final DWordleBot bot) {
        this.gameController = gameController;
        this.bot = bot;
    }

    @Override
    public void onSlashCommand(@NotNull final SlashCommandEvent event) {
        if (event.getCommandIdLong() != this.bot.getAdminCommandId() || event.getSubcommandName() == null) {
            return;
        }
        if (event.getUser().getIdLong() != this.bot.getAdminUser()) {
            return;
        }

        switch (event.getSubcommandName()) {
            case "safestop" -> this.handleSafeStop(event);
            case "guilds" -> this.handleGuilds(event);
        }
    }

    private void handleGuilds(final SlashCommandEvent event) {
        event.reply(event.getJDA().getGuilds().stream()
                .map(guild -> "`" + guild.getName() + "` [" + guild.getMemberCount() + "]")
                .collect(Collectors.joining(", "))).queue();
    }

    private void handleSafeStop(final SlashCommandEvent event) {
        event.reply("Ok").queue();
        this.bot.setSafeStopEnabled(true);
    }

}
