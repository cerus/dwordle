package dev.cerus.dwordle.word;

import java.util.HashMap;
import java.util.Map;

public class WordServiceController {

    private final Map<String, WordService> wordServiceMap = new HashMap<>();

    public void registerWordService(final String name, final WordService wordService) {
        this.wordServiceMap.put(name, wordService);
    }

    public boolean isValidInput(final String name, final String str) {
        if (!this.wordServiceMap.containsKey(name)) {
            throw new IllegalArgumentException("Unknown word service");
        }
        return this.wordServiceMap.get(name).isValidInput(str);
    }

    public String getRandomSecretWord(final String name) {
        if (!this.wordServiceMap.containsKey(name)) {
            throw new IllegalArgumentException("Unknown word service");
        }
        return this.wordServiceMap.get(name).getRandomSecretWord();
    }

}
