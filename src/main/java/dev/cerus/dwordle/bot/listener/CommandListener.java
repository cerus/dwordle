package dev.cerus.dwordle.bot.listener;

import dev.cerus.dwordle.game.GameController;
import dev.cerus.dwordle.stats.StatsService;
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * /wordle command listener
 */
public class CommandListener extends ListenerAdapter {

    private final GameController gameController;
    private final StatsService statsService;
    private final long commandId;

    public CommandListener(final GameController gameController, final StatsService statsService, final long commandId) {
        this.gameController = gameController;
        this.statsService = statsService;
        this.commandId = commandId;
    }

    @Override
    public void onSlashCommand(@NotNull final SlashCommandEvent event) {
        if (event.getCommandIdLong() != this.commandId || event.getSubcommandName() == null) {
            return;
        }

        switch (event.getSubcommandName()) {
            case "start" -> this.handleStartGame(event);
            case "end" -> this.handleEndGame(event);
            case "info" -> this.handleInfo(event);
            case "stats" -> this.handleStats(event);
        }
    }

    /**
     * /wordle stats
     *
     * @param event The event
     */
    private void handleStats(final SlashCommandEvent event) {
        event.deferReply(true).queue(hook -> {
            this.statsService.exec(() -> {
                hook.editOriginal(new MessageBuilder().setEmbed(new EmbedBuilder()
                        .setTitle("DWordle Stats")
                        .setDescription("These are the DWordle statistics:")
                        .addField("Games played", "You: " + this.statsService.getAmountTotalGamesPlayed(event.getUser().getIdLong())
                                + "\nTotal: " + this.statsService.getAmountTotalGamesPlayed(), true)
                        .addField("Games won", "You: " + this.statsService.getAmountTotalGamesWon(event.getUser().getIdLong())
                                + "\nTotal: " + this.statsService.getAmountTotalGamesWon(), true)
                        .addField("Games lost", "You: " + this.statsService.getAmountTotalGamesLost(event.getUser().getIdLong())
                                + "\nTotal: " + this.statsService.getAmountTotalGamesLost(), true)
                        .setColor(Color.ORANGE)
                        .setFooter("DWordle - Made by Cerus", "https://cerus.dev/favicon.png")
                        .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .build()).build()).queue();
            });
        });
    }

    /**
     * Handles /wordle info
     *
     * @param event The command event
     */
    private void handleInfo(final SlashCommandEvent event) {
        event.deferReply(true).queue(hook -> {
            hook.editOriginal(new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle("DWordle Bot Info")
                    .setDescription("""
                            DWordle bot by [Cerus](https://cerus.dev)

                            DWordle uses the same words as the original Wordle game.""")
                    .addField("DWordle on GitHub", "[Link](https://github.com/cerus/dwordle)", true)
                    .addField("Contact Cerus", "Cerus#5149", true)
                    .addField("Servers", String.valueOf(event.getJDA().getGuilds().size()), true)
                    .setColor(Color.ORANGE)
                    .setFooter("DWordle - Made by Cerus", "https://cerus.dev/favicon.png")
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .build()).build()).queue();
        });
    }

    /**
     * Handles /wordle start
     *
     * @param event The command event
     */
    private void handleStartGame(final SlashCommandEvent event) {
        final User user = event.getUser();
        if (this.gameController.hasGame(user.getIdLong())) {
            event.reply("You already have a game running.").queue();
            return;
        }

        event.reply("Ok").queue();
        this.gameController.startGame(user.getIdLong(), event.getChannel());
    }

    /**
     * Handles /wordle end
     *
     * @param event The command event
     */
    private void handleEndGame(final SlashCommandEvent event) {
        final User user = event.getUser();
        if (!this.gameController.hasGame(user.getIdLong())) {
            event.reply("You do not have a game running.").queue();
            return;
        }

        event.reply("Ok").queue();
        this.gameController.endGame(user.getIdLong());
    }

}
