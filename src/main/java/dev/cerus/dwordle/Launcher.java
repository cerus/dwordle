package dev.cerus.dwordle;

import dev.cerus.dwordle.bot.DWordleBot;
import dev.cerus.dwordle.game.GameController;
import dev.cerus.dwordle.stats.SQLiteStatsService;
import dev.cerus.dwordle.stats.StatsService;
import dev.cerus.dwordle.word.NyTimesWordService;
import dev.cerus.dwordle.word.WordService;
import dev.cerus.dwordle.word.WordServiceController;
import dev.cerus.dwordle.word.WordleAtWordService;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.entities.Activity;

public class Launcher {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss z");

    public static void main(final String[] args) {
        // Initialize words
        final WordServiceController wordServiceController = new WordServiceController();
        final WordService officialWordService = new NyTimesWordService();
        officialWordService.initialize().whenComplete((unused, throwable) -> {
            wordServiceController.registerWordService("official", officialWordService);
            log("Official words initialized");
        });
        final WordService germanWordService = new WordleAtWordService();
        germanWordService.initialize().whenComplete((unused, throwable) -> {
            wordServiceController.registerWordService("german", germanWordService);
            log("German words initialized");
        });

        // Initialize stats
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        final StatsService statsService = new SQLiteStatsService();
        statsService.initialize(executor);

        // Initialize game controller and bot
        final GameController gameController = new GameController(wordServiceController, statsService);
        final DWordleBot bot = new DWordleBot();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                executor.shutdown();
                statsService.close();
                wordServiceController.close();
                bot.shutdown();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            log("Goodbye");
        }));

        // Attempt to start bot
        try {
            bot.initialize(System.getenv("TOKEN"), gameController, wordServiceController, statsService);
        } catch (final LoginException | InterruptedException e) {
            e.printStackTrace();
            log("ERROR: Failed to initialize bot");
            System.exit(1);
            return;
        }

        // Start presence update task
        executor.scheduleAtFixedRate(() -> {
            final long played = statsService.getAmountTotalGamesPlayed();
            final long won = statsService.getAmountTotalGamesWon();
            final long lost = statsService.getAmountTotalGamesLost();
            bot.updateActivity(Activity.ActivityType.DEFAULT, played + " games / " + won + " won / " + lost + " lost");
        }, 0, 1, TimeUnit.MINUTES);

        // Post bot stats
        executor.scheduleAtFixedRate(() -> {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL("https://discordbotlist.com/api/v1/bots/"
                        + bot.getId() + "/stats").openConnection();
                connection.setRequestProperty("Authorization", System.getenv("DBL_TOKEN"));
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                final OutputStream out = connection.getOutputStream();
                out.write(String.format(
                        "{\"guilds\": %d, \"users\": %d}",
                        bot.countGuilds(),
                        bot.countUsers()
                ).getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (final IOException e) {
                e.printStackTrace();
                log("ERROR: Failed to post stats");
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    public static void log(final String msg) {
        System.out.println("[" + DATE_FORMAT.format(new Date()) + "] " + msg);
    }

}
