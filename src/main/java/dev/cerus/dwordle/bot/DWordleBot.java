package dev.cerus.dwordle.bot;

import dev.cerus.dwordle.bot.listener.AdminCommandListener;
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * The actual bot
 */
public class DWordleBot {

    private JDA jda;
    private long adminUser;
    private long adminCommandId;
    private long commandId;
    private boolean safeStopEnabled;

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
        this.adminUser = Long.parseLong(System.getenv("ADMIN_USER"));
        final long privateGuildId = Long.parseLong(System.getenv("GUILD_ID"));

        this.jda = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES).build().awaitReady();
        this.jda.getGuildById(privateGuildId)
                .upsertCommand(new CommandData("wordle-admin", "DWordle admin commands")
                        .setDefaultEnabled(false)
                        .addSubcommands(
                                new SubcommandData("safestop", "Safely stop"),
                                new SubcommandData("guilds", "Show guilds")
                        )).queue(cmd -> {
                    this.adminCommandId = cmd.getIdLong();
                    this.jda.getGuildById(privateGuildId)
                            .updateCommandPrivilegesById(cmd.getIdLong(), CommandPrivilege.enableUser(this.adminUser))
                            .queue();
                });
        this.jda.upsertCommand(new CommandData("wordle", "DWordle Bot Command")
                .addSubcommands(
                        new SubcommandData("start", "Start a game")
                                .addOption(OptionType.STRING, "word-list",
                                        "The word list that will be used for the game", false),
                        new SubcommandData("end", "End a game"),
                        new SubcommandData("info", "DWordle info"),
                        new SubcommandData("stats", "DWordle stats"),
                        new SubcommandData("help", "DWordle help")
                )).queue(command -> {
            this.commandId = command.getIdLong();
        });


        // We need to know the id of the command, that's why we register the event listeners this late
        this.jda.addEventListener(
                new CommandListener(gameController, wordServiceController, statsService, this),
                new AdminCommandListener(gameController, this),
                new ReplyListener(gameController, wordServiceController)
        );
    }

    /**
     * Updates the bots presence
     *
     * @param type The type of activity to display
     * @param msg  The activity message
     */
    public void updateActivity(final OnlineStatus status, final String msg) {
        this.jda.getPresence().setPresence(status, Activity.of(Activity.ActivityType.DEFAULT, msg));
    }

    public void shutdown() {
        this.jda.shutdown();
    }

    public JDA getJda() {
        return this.jda;
    }

    public long getId() {
        return this.jda.getSelfUser().getIdLong();
    }

    public int countGuilds() {
        return this.jda.getGuilds().size();
    }

    public int countUsers() {
        return this.jda.getGuilds().stream()
                .mapToInt(Guild::getMemberCount)
                .sum();
    }

    public long getAdminUser() {
        return this.adminUser;
    }

    public long getCommandId() {
        return this.commandId;
    }

    public long getAdminCommandId() {
        return this.adminCommandId;
    }

    public boolean isSafeStopEnabled() {
        return this.safeStopEnabled;
    }

    public void setSafeStopEnabled(final boolean safeStopEnabled) {
        this.safeStopEnabled = safeStopEnabled;
    }

}
