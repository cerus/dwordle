package dev.cerus.dwordle.stats;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Responsible for saving and retrieving stats
 */
public interface StatsService extends AutoCloseable {

    /**
     * Initializes this stats service
     *
     * @param exec Executor service for async operations
     */
    void initialize(ExecutorService exec);

    /**
     * Increments the users games_played statistic
     *
     * @param userId The user
     */
    void gamePlayed(long userId);

    /**
     * Increments the users games_won statistic
     *
     * @param userId The user
     */
    void gameWon(long userId);

    /**
     * Increments the users games_lost statistic
     *
     * @param userId The user
     */
    void gameLost(long userId);

    /**
     * Runs the specified operation async (using the provided executor)
     * <p>
     * Note: This is bad application design because you could pass non-stats
     * operations onto this method. I just can't be bothered to fix it right now.
     *
     * @param run The operation to run async
     *
     * @return A callback
     */
    CompletableFuture<Void> exec(Runnable run);

    /**
     * Runs the specified operation async (using the provided executor)
     * <p>
     * Note: This is bad application design because you could pass non-stats
     * operations onto this method. I just can't be bothered to fix it right now.
     *
     * @param func The operation to run async
     *
     * @return A callback
     */
    CompletableFuture<Long> get(Supplier<Long> func);

    /**
     * Gets the total amount of games played
     *
     * @return Total amount of games played
     */
    long getAmountTotalGamesPlayed();

    /**
     * Gets the total amount of games played by the user
     *
     * @param userId The user
     *
     * @return Total amount of games played by the user
     */
    long getAmountTotalGamesPlayed(long userId);

    /**
     * Gets the total amount of games won
     *
     * @return Total amount of games won
     */
    long getAmountTotalGamesWon();

    /**
     * Gets the total amount of games won by the user
     *
     * @param userId The user
     *
     * @return The total amount of games won by the user
     */
    long getAmountTotalGamesWon(long userId);

    /**
     * Gets the total amount of games lost
     *
     * @return The total amount of games lost
     */
    long getAmountTotalGamesLost();

    /**
     * Gets the total amount of games lost by the user
     *
     * @param userId The user
     *
     * @return The total amount of games lost by the user
     */
    long getAmountTotalGamesLost(long userId);

}
