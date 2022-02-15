package dev.cerus.dwordle;

/**
 * A place for constants
 */
public class Const {

    public static final int WORD_LENGTH = 5;
    public static final int INPUT_AMOUNT = 6;
    public static final int TIMEOUT = 15;

    public static final String EMOTE_GRAY = ":black_large_square:";
    public static final String EMOTE_YELLOW = ":yellow_square:";
    public static final String EMOTE_GREEN = ":green_square:";
    public static final String EMOTE_BLUE = ":blue_square:";
    public static final String EMOTE_ARROW_RIGHT = ":arrow_forward:";
    public static final String[] NUMBER_EMOTES = new String[] {
            ":one:", ":two:", ":three:", ":four:", ":five:", ":six:"
    };

    private Const() {
        throw new UnsupportedOperationException();
    }

}
