package dev.cerus.dwordle;

import dev.cerus.dwordle.bot.DWordleBot;
import dev.cerus.dwordle.game.GameController;
import dev.cerus.dwordle.stats.SQLiteStatsService;
import dev.cerus.dwordle.stats.StatsService;
import dev.cerus.dwordle.word.NyTimesWordService;
import dev.cerus.dwordle.word.WordService;
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
        final WordService wordService = new NyTimesWordService();
        wordService.initialize().whenComplete((unused, throwable) -> {
            log("Words initialized");
        });

        // Initialize stats
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        final StatsService statsService = new SQLiteStatsService();
        statsService.initialize(executor);

        // Initialize game controller and bot
        final GameController gameController = new GameController(wordService, statsService);
        final DWordleBot bot = new DWordleBot();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                executor.shutdown();
                statsService.close();
                wordService.close();
                bot.shutdown();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            log("Goodbye");
        }));

        // Attempt to start bot
        try {
            bot.initialize(System.getenv("TOKEN"), gameController, wordService, statsService);
        } catch (final LoginException | InterruptedException e) {
            e.printStackTrace();
            log("ERROR: Failed to initialize bot");
            System.exit(1);
            return;
        }

        // Start presence update task
        executor.scheduleAtFixedRate(() -> {
            final long played = statsService.getAmountTotalGamesPlayed();
            final long won = statsService.getAmountTotalGamesPlayed();
            final long lost = statsService.getAmountTotalGamesLost();
            bot.updateActivity(Activity.ActivityType.DEFAULT, played + " games / " + won + " won / " + lost + " lost");
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void log(final String msg) {
        System.out.println("[" + DATE_FORMAT.format(new Date()) + "] " + msg);
    }

}
