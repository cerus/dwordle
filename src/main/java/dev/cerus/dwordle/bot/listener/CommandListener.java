package dev.cerus.dwordle.bot.listener;

import dev.cerus.dwordle.bot.DWordleBot;
import dev.cerus.dwordle.game.GameController;
import dev.cerus.dwordle.stats.StatsService;
import dev.cerus.dwordle.word.WordServiceController;
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * /wordle command listener
 */
public class CommandListener extends ListenerAdapter {

    private final GameController gameController;
    private final WordServiceController wordServiceController;
    private final StatsService statsService;
    private final DWordleBot bot;

    public CommandListener(final GameController gameController,
                           final WordServiceController wordServiceController,
                           final StatsService statsService,
                           final DWordleBot bot) {
        this.gameController = gameController;
        this.wordServiceController = wordServiceController;
        this.statsService = statsService;
        this.bot = bot;
    }

    @Override
    public void onSlashCommand(@NotNull final SlashCommandEvent event) {
        if (event.getCommandIdLong() != this.bot.getCommandId() || event.getSubcommandName() == null) {
            return;
        }

        switch (event.getSubcommandName()) {
            case "start" -> this.handleStartGame(event);
            case "end" -> this.handleEndGame(event);
            case "info" -> this.handleInfo(event);
            case "stats" -> this.handleStats(event);
            case "help" -> this.handleHelp(event);
        }
    }

    /**
     * /wordle help
     *
     * @param event The event
     */
    private void handleHelp(final SlashCommandEvent event) {
        event.deferReply(true).queue(hook -> {
            hook.editOriginal(new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle("DWordle Bot Help")
                    .setDescription("""
                            DWordle commands:
                                                        
                            `/wordle info`: Shows bot info
                            `wordle stats`: Shows DWordle stats
                            `/wordle start`: Starts a wordle game
                            `/wordle end`: Ends your running wordle game""")
                    .addField("How do I submit guesses?", "Reply to the game message " +
                            "with your guess. ([GIF](https://i.imgur.com/e3DDiP2.gif))", false)
                    .setColor(new Color(127, 38, 146))
                    .setFooter("DWordle - Made by Cerus", "https://cerus.dev/favicon.png")
                    .build()).build()).queue();
        });
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

                            Available word lists: `official`, `german`
                            DWordle uses the same words as the original Wordle game by default.""")
                    .addField("GitHub", "[Link](https://github.com/cerus/dwordle)", true)
                    .addField("Contact Cerus", "Cerus#5149", true)
                    .addField("Servers", String.valueOf(event.getJDA().getGuilds().size()), true)
                    .addField("Users", String.valueOf(event.getJDA().getGuilds().stream()
                            .mapToInt(Guild::getMemberCount)
                            .sum()), true)
                    .addField("Running games", String.valueOf(this.gameController.getRunningGames()), true)
                    .addField("Tech", "Java 16, JDA framework", true)
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
        if (this.bot.isSafeStopEnabled()) {
            event.deferReply(true).queue(h -> h.editOriginal(new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("You can't start a game right now")
                            .setDescription("The bot is currently waiting for all running games " +
                                    "to end in order to restart. Please wait a few minutes.")
                            .setColor(Color.RED)
                            .build())
                    .build()).queue());
            return;
        }

        final User user = event.getUser();
        if (this.gameController.hasGame(user.getIdLong())) {
            event.reply("You already have a game running.").queue();
            return;
        }

        final String wordListName = event.getOption("word-list") == null ? "official"
                : event.getOption("word-list").getAsString();
        if (!this.wordServiceController.isValidWordList(wordListName)) {
            event.reply("Unknown word list").queue();
            return;
        }

        event.reply("Ok").queue();
        this.gameController.startGame(user.getIdLong(), event.getChannel(), wordListName);
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
