package dev.cerus.dwordle.bot;

import dev.cerus.dwordle.bot.listener.CommandListener;
import dev.cerus.dwordle.bot.listener.ReplyListener;
import dev.cerus.dwordle.game.GameController;
import dev.cerus.dwordle.stats.StatsService;
import dev.cerus.dwordle.word.WordServiceController;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * The actual bot
 */
public class DWordleBot {

    private JDA jda;

    /**
     * Initialize the bot
     *
     * @param token                 The bot token
     * @param gameController        The game controller
     * @param wordServiceController The word service controller
     * @param statsService          The stats service
     *
     * @throws LoginException       See JDA
     * @throws InterruptedException See JDA
     */
    public void initialize(final String token,
                           final GameController gameController,
                           final WordServiceController wordServiceController,
                           final StatsService statsService) throws LoginException, InterruptedException {
        this.jda = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES).build().awaitReady();
        this.jda.upsertCommand(new CommandData("wordle", "DWordle Bot Command")
                .addSubcommands(
                        new SubcommandData("start", "Start a game")
                                .addOption(OptionType.STRING, "word-list",
                                        "The word list that will be used for the game", false),
                        new SubcommandData("end", "End a game"),
                        new SubcommandData("info", "DWordle info"),
                        new SubcommandData("stats", "DWordle stats")
                )).queue(command -> {
            // We need to know the id of the command, that's why we register the event listeners this late
            this.jda.addEventListener(
                    new CommandListener(gameController, wordServiceController, statsService, command.getIdLong()),
                    new ReplyListener(gameController, wordServiceController)
            );
        });
    }

    /**
     * Updates the bots presence
     *
     * @param type The type of activity to display
     * @param msg  The activity message
     */
    public void updateActivity(final Activity.ActivityType type, final String msg) {
        this.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(type, msg));
    }

    public void shutdown() {
        this.jda.shutdown();
    }

}
