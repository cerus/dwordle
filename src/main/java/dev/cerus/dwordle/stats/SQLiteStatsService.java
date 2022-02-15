package dev.cerus.dwordle.stats;

import dev.cerus.dwordle.Launcher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class SQLiteStatsService implements StatsService {

    private ExecutorService exec;
    private String fileName;

    @Override
    public void initialize(final ExecutorService exec) {
        this.exec = exec;
        this.fileName = System.getenv("STATS_FILE");
        if (this.fileName == null) {
            this.fileName = "./stats.db";
        }

        // Create table
        try (final Connection connection = this.getConnection()) {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `stats` (user_id BIGINT PRIMARY KEY, " +
                            "games_played BIGINT DEFAULT 0, games_won BIGINT DEFAULT 0, games_lost BIGINT DEFAULT 0)")
                    .executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> exec(final Runnable run) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.exec.execute(() -> {
            try {
                run.run();
                future.complete(null);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Long> get(final Supplier<Long> func) {
        final CompletableFuture<Long> future = new CompletableFuture<>();
        this.exec.execute(() -> {
            try {
                final long val = func.get();
                future.complete(val);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public void gamePlayed(final long userId) {
        this.incrementLong("games_played", userId);
    }

    @Override
    public void gameWon(final long userId) {
        this.incrementLong("games_won", userId);
    }

    @Override
    public void gameLost(final long userId) {
        this.incrementLong("games_lost", userId);
    }

    @Override
    public long getAmountTotalGamesPlayed() {
        return this.queryLong("games_played", null);
    }

    @Override
    public long getAmountTotalGamesPlayed(final long userId) {
        return this.queryLong("games_played", userId);
    }

    @Override
    public long getAmountTotalGamesWon() {
        return this.queryLong("games_won", null);
    }

    @Override
    public long getAmountTotalGamesWon(final long userId) {
        return this.queryLong("games_won", userId);
    }

    @Override
    public long getAmountTotalGamesLost() {
        return this.queryLong("games_lost", null);
    }

    @Override
    public long getAmountTotalGamesLost(final long userId) {
        return this.queryLong("games_lost", userId);
    }

    private void incrementLong(final String field, final long user) {
        // Increment specific field
        try (final Connection connection = this.getConnection()) {
            final PreparedStatement statement = connection.prepareStatement("INSERT INTO `stats` (user_id, " + field
                    + ") VALUES (?, 1) ON CONFLICT(user_id) DO UPDATE SET " + field + " = " + field + " + 1");
            statement.setLong(1, user);
            statement.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private long queryLong(final String field, final Long user) {
        try (final Connection connection = this.getConnection()) {
            // Make statement
            final PreparedStatement statement;
            if (user == null) {
                // No user specified; Sum all rows up
                statement = connection.prepareStatement("SELECT  SUM(" + field + ") AS `sum` FROM `stats`");
            } else {
                // User specified; Get only row of user
                statement = connection.prepareStatement("SELECT " + field + " FROM `stats` WHERE `user_id` = ?");
                statement.setLong(1, user);
            }

            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (user == null) {
                    return resultSet.getLong("sum");
                } else {
                    return resultSet.getLong(field);
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            Launcher.log("ERROR: SQL query failed (queryLong(" + field + "))");
        }
        return 0;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + this.fileName);
    }

    @Override
    public void close() throws Exception {
    }

}
