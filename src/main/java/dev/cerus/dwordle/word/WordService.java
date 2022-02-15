package dev.cerus.dwordle.word;

import java.util.concurrent.CompletableFuture;

/**
 * Responsible for selecting words
 */
public interface WordService extends AutoCloseable {

    /**
     * Initialize this word service
     * Implementations should use this method to load all possible words.
     *
     * @return A callback
     */
    CompletableFuture<Void> initialize();

    /**
     * Checks if a string is a valid input
     *
     * @param str The input to check
     *
     * @return True or false
     */
    boolean isValidInput(String str);

    /**
     * Selects a random secret word
     *
     * @return A secret word
     */
    String getRandomSecretWord();

}
