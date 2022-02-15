package dev.cerus.dwordle.game;

import static dev.cerus.dwordle.Const.TIMEOUT;
import dev.cerus.dwordle.bot.util.MessageUtil;
import dev.cerus.dwordle.stats.StatsService;
import dev.cerus.dwordle.word.WordService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.jodah.expiringmap.ExpiringMap;

public class GameController {

    private final Map<Long, WordleGame> gameMap = new HashMap<>();    private final Map<Long, Object> timeoutMap = ExpiringMap.builder()
            .expiration(TIMEOUT, TimeUnit.MINUTES)
            .expirationListener((o, o2) -> this.endGame((Long) o))
            .build();
    private final Map<Long, Message> messageMap = new HashMap<>();
    private final WordService wordService;
    private final StatsService statsService;
    public GameController(final WordService wordService, final StatsService statsService) {
        this.wordService = wordService;
        this.statsService = statsService;
    }

    /**
     * Makes a call to the game object to process the input and updates the Discord message
     * Will also save stats if this causes the game to end
     *
     * @param userId The user
     * @param input  The input
     */
    public void handleInput(final long userId, final String input) {
        final WordleGame game = this.gameMap.get(userId);
        game.handleInput(input);

        MessageUtil.sendGameUpdate(userId, game, this.messageMap.get(userId));

        if (game.isAtEnd() || game.isDone()) {
            this.gameMap.remove(userId);
            this.messageMap.remove(userId);
            this.timeoutMap.remove(userId);

            // Update stats
            this.statsService.exec(() -> {
                this.statsService.gamePlayed(userId);
                if (game.isDone()) {
                    this.statsService.gameWon(userId);
                } else {
                    this.statsService.gameLost(userId);
                }
            });
        }
    }

    /**
     * Ends the users running game, updates the Discord message and saves the stats
     *
     * @param userId The user
     */
    public void endGame(final long userId) {
        if (!this.hasGame(userId)) {
            return;
        }

        this.timeoutMap.remove(userId);
        final WordleGame game = this.gameMap.remove(userId);
        final Message message = this.messageMap.remove(userId);

        MessageUtil.sendGameEnded(userId, game, message);
        this.statsService.exec(() -> this.statsService.gameLost(userId));
    }

    /**
     * Starts a new game for the user
     *
     * @param userId  The user
     * @param channel The channel where the game is going to be played
     */
    public void startGame(final long userId, final MessageChannel channel) {
        this.timeoutMap.put(userId, new Object());
        this.gameMap.put(userId, new WordleGame(this.wordService.getRandomSecretWord()));

        System.out.println(this.gameMap.get(userId).getSecretWord());

        MessageUtil.sendGameStarted(userId, channel, message -> this.messageMap.put(userId, message));
    }

    /**
     * Does the user have a running game?
     *
     * @param userId The user
     *
     * @return True if user has a running game
     */
    public boolean hasGame(final long userId) {
        return this.gameMap.containsKey(userId);
    }

    /**
     * Is this message a game message?
     *
     * @param userId The user
     * @param msgId  The message
     *
     * @return True or false
     */
    public boolean isGameMessage(final long userId, final long msgId) {
        if (!this.messageMap.containsKey(userId)) {
            return false;
        }
        return this.messageMap.get(userId).getIdLong() == msgId;
    }



}
