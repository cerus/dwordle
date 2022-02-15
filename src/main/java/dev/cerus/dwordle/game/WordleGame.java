package dev.cerus.dwordle.game;

import static dev.cerus.dwordle.Const.INPUT_AMOUNT;
import static dev.cerus.dwordle.Const.WORD_LENGTH;
import java.util.HashSet;
import java.util.Set;

/**
 * Game logic
 */
public class WordleGame {

    public static final int GRAY = 0;
    public static final int YELLOW = 1;
    public static final int GREEN = 2;

    private final int[][] grid = new int[INPUT_AMOUNT][WORD_LENGTH];
    private final String[] inputs = new String[INPUT_AMOUNT];
    private final String wordList;
    private final String secretWord;
    private int index;
    private boolean done;

    public WordleGame(String wordList, final String secretWord) {
        this.wordList = wordList;
        this.secretWord = secretWord.toLowerCase();
        this.index = 0;
        for (int i = 0; i < WORD_LENGTH; i++) {
            for (int j = 0; j < WORD_LENGTH; j++) {
                this.grid[i][j] = GRAY;
            }
        }
    }

    /**
     * Processes the input and advances the game by one step.
     *
     * @param input The word to process
     */
    public void handleInput(final String input) {
        // Check input validity
        final char[] processedInput = input.toLowerCase().trim().toCharArray();
        if (processedInput.length != WORD_LENGTH) {
            return;
        }
        if (this.index >= INPUT_AMOUNT) {
            return;
        }

        // I don't like using sets here, is there a better way to solve this?
        final Set<Character> greens = new HashSet<>();
        final Set<Character> yellows = new HashSet<>();

        // First iteration: Calculate all greens
        int greenAmount = 0;
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (processedInput[i] == this.secretWord.charAt(i)) {
                this.grid[this.index][i] = GREEN;
                greens.add(processedInput[i]);
                greenAmount++;
            }
        }
        // Second iteration: Calculate all yellows
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (this.secretWord.contains(String.valueOf(processedInput[i]))) {
                if (greens.contains(processedInput[i]) || yellows.contains(processedInput[i])) {
                    continue;
                }
                this.grid[this.index][i] = YELLOW;
                yellows.add(processedInput[i]);
            }
        }

        // Check if user has won
        if (greenAmount == WORD_LENGTH) {
            this.done = true;
        }

        // Save input
        this.inputs[this.index++] = input.toLowerCase().trim();
    }

    /**
     * Gets the state of a specific character in a specific row
     * <p>
     * The state is either 0 (gray), 1 (yellow) or 2 (green)
     *
     * @param row    The row
     * @param column The column
     *
     * @return The state
     */
    public int getState(final int row, final int column) {
        return this.grid[row][column];
    }

    /**
     * Gets the amount of tries the user has used so far
     *
     * @return Amount of tries
     */
    public int getTries() {
        return this.index;
    }

    /**
     * Has the user won this game?
     *
     * @return True if user has won
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * Has the user used all of their tries?
     * <p>
     * If this returns true and {@link WordleGame#isDone()} returns false the user has lost
     *
     * @return True if user has used all of their tries
     */
    public boolean isAtEnd() {
        return this.index >= INPUT_AMOUNT;
    }

    /**
     * Gets the provided input for the specified row
     *
     * @param row The row
     *
     * @return The input
     */
    public String getInput(final int row) {
        return this.inputs[row];
    }

    /**
     * Gets the word list name
     *
     * @return Word list name
     */
    public String getWordList() {
        return wordList;
    }

    /**
     * Gets the secret word
     *
     * @return The secret word
     */
    public String getSecretWord() {
        return this.secretWord;
    }

}
