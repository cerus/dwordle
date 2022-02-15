package dev.cerus.dwordle.bot.util;

import dev.cerus.dwordle.Const;
import static dev.cerus.dwordle.Const.*;
import dev.cerus.dwordle.game.WordleGame;
import java.util.function.Consumer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Contains all the ugly code that's responsible for sending and editing game messages
 */
public class MessageUtil {

    private MessageUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sends a new game message
     *
     * @param userId   The user
     * @param channel  The channel
     * @param callback A callback to return the sent message
     */
    public static void sendGameStarted(final long userId, final MessageChannel channel, final Consumer<Message> callback) {
        final StringBuilder msgBuilder = new StringBuilder("<@!" + userId + ">\n\n");
        for (int i = 0; i < Const.INPUT_AMOUNT; i++) {
            final boolean start = i == 0;
            msgBuilder.append(start ? EMOTE_ARROW_RIGHT : EMOTE_BLUE).append("  ")
                    .append(EMOTE_GRAY.repeat(Const.WORD_LENGTH)).append("  ")
                    .append(NUMBER_EMOTES[i]).append("\n");
        }
        msgBuilder.append("\nYou can start guessing by replying to this message with your guess. "
                + "The game will automatically end in " + TIMEOUT + " minutes.");
        channel.sendMessage(msgBuilder.toString()).queue(callback);
    }

    /**
     * Edits a game messages to communicate the end of said game
     *
     * @param userId  The user
     * @param game    The game
     * @param message The game message
     */
    public static void sendGameEnded(final long userId, final WordleGame game, final Message message) {
        final StringBuilder msgBuilder = new StringBuilder("<@!").append(userId).append(">\n\n");
        for (int row = 0; row < Const.INPUT_AMOUNT; row++) {
            msgBuilder.append(row == game.getTries() ? EMOTE_ARROW_RIGHT : EMOTE_BLUE).append("  ");
            for (int col = 0; col < Const.WORD_LENGTH; col++) {
                msgBuilder.append(switch (game.getState(row, col)) {
                    case WordleGame.YELLOW -> EMOTE_YELLOW;
                    case WordleGame.GREEN -> EMOTE_GREEN;
                    default -> EMOTE_GRAY;
                });
            }
            msgBuilder.append("  ").append(NUMBER_EMOTES[row]);
            if (game.getInput(row) != null) {
                msgBuilder.append("  '").append(game.getInput(row)).append("'");
            }
            msgBuilder.append("\n");
        }
        msgBuilder.append("\nThe game has ended");
        message.editMessage(msgBuilder.toString()).queue();
    }

    /**
     * Updates a game message to communicate possible changes
     *
     * @param userId  The user
     * @param game    The game
     * @param message The message
     */
    public static void sendGameUpdate(final long userId, final WordleGame game, final Message message) {
        final StringBuilder wordleGridBuilder = new StringBuilder();
        for (int row = 0; row < Const.INPUT_AMOUNT; row++) {
            wordleGridBuilder.append(row == game.getTries() ? EMOTE_ARROW_RIGHT : EMOTE_BLUE).append("  ");
            for (int col = 0; col < Const.WORD_LENGTH; col++) {
                wordleGridBuilder.append(switch (game.getState(row, col)) {
                    case WordleGame.YELLOW -> EMOTE_YELLOW;
                    case WordleGame.GREEN -> EMOTE_GREEN;
                    default -> EMOTE_GRAY;
                });
            }
            wordleGridBuilder.append("  ").append(NUMBER_EMOTES[row]);
            if (game.getInput(row) != null) {
                wordleGridBuilder.append("  '").append(game.getInput(row)).append("'");
            }
            wordleGridBuilder.append("\n");
        }

        final String status;
        if (game.isDone()) {
            status = "You found the word! :partying_face: The word was `" + game.getSecretWord() + "`";
        } else if (game.isAtEnd()) {
            status = "You did not find the word in time :disappointed: The word was `" + game.getSecretWord() + "`";
        } else {
            final int triesLeft = Const.INPUT_AMOUNT - game.getTries();
            status = triesLeft + (triesLeft == 1 ? " try" : " tries") + " left";
        }

        message.editMessage("<@!" + userId + ">\n\n" + wordleGridBuilder + "\n" + status).queue();
    }

}
