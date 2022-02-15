package dev.cerus.dwordle.bot.listener;

import dev.cerus.dwordle.game.GameController;
import dev.cerus.dwordle.word.WordService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Game message reply listener
 */
public class ReplyListener extends ListenerAdapter {

    private final GameController gameController;
    private final WordService wordService;

    public ReplyListener(final GameController gameController, final WordService wordService) {
        this.gameController = gameController;
        this.wordService = wordService;
    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        final Message message = event.getMessage();
        if (message.getType() != MessageType.INLINE_REPLY
                || message.getReferencedMessage() == null) {
            return;
        }
        if (!this.gameController.isGameMessage(
                message.getAuthor().getIdLong(),
                message.getReferencedMessage().getIdLong()
        )) {
            return;
        }

        // Check input validity
        final String input = message.getContentStripped().toLowerCase().trim();
        if (!this.wordService.isValidInput(input)) {
            message.reply("Invalid input").queue(msg ->
                    msg.delete().queueAfter(5, TimeUnit.SECONDS, v -> {
                        if (message.getChannel() instanceof TextChannel) {
                            message.delete().queue();
                        }
                    }));
            return;
        }

        // All good, process input
        this.gameController.handleInput(message.getAuthor().getIdLong(), input);
        if (message.getChannel() instanceof TextChannel) {
            message.delete().queue();
        }
    }

}
