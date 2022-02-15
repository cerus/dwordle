package dev.cerus.dwordle.word;

import dev.cerus.dwordle.Const;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Word service for the German Wordle clone wordle.at
 * <p>
 * https://wordle.at/
 */
public class WordleAtWordService implements WordService {

    private static final String URL = "https://wordle.at/word-list.js";

    private final Random random = new Random();
    private ExecutorService executorService;
    private List<String> words;
    private List<String> inputs;

    @Override
    public CompletableFuture<Void> initialize() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.inputs = new ArrayList<>();
        this.words = new ArrayList<>();
        final CompletableFuture<Void> future = new CompletableFuture<>();

        this.executorService.execute(() -> {
            final String response;
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setDoInput(true);

                // Read response content
                final StringBuilder builder = new StringBuilder();
                final InputStream in = connection.getInputStream();
                int b;
                while ((b = in.read()) != -1) {
                    builder.append((char) b);
                }
                response = builder.toString();
            } catch (final IOException e) {
                future.completeExceptionally(e);
                return;
            }

            try {
                // Parse inputs and words
                final String[] lines = response.split("\n");
                final String encodedInputs = lines[1].substring(lines[1].indexOf("\"") + 1, lines[1].length() - 2);
                final String encodedWords = lines[2].substring(lines[2].indexOf("\"") + 1, lines[2].length() - 2);

                // Decode inputs and words
                this.inputs.addAll(this.decode(encodedInputs));
                this.words.addAll(this.decode(encodedWords));

                future.complete(null);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });

        return future;
    }

    /**
     * Decodes and splits encoded strings with the wordle.at algorithm
     *
     * @param input String to decode
     *
     * @return Decoded strings
     */
    private Collection<String> decode(final String input) {
        final AtomicInteger inc = new AtomicInteger(1);
        final String result = Arrays.stream(input.split(""))
                .map(s -> String.valueOf((char) ((s.charAt(0) - 97 + 7 * inc.getAndIncrement()) % 26 + 65)))
                .collect(Collectors.joining(""));

        int index = 0;
        final List<String> list = new ArrayList<>();
        while (index < result.length()) {
            list.add(result.substring(index, index + Const.WORD_LENGTH).toLowerCase());
            index += Const.WORD_LENGTH;
        }
        return list;
    }

    @Override
    public boolean isValidInput(final String str) {
        return this.inputs.contains(str) || this.words.contains(str);
    }

    @Override
    public String getRandomSecretWord() {
        return this.words.get(this.random.nextInt(this.words.size()));
    }

    @Override
    public void close() throws Exception {
        this.words.clear();
        this.inputs.clear();
        this.executorService.shutdown();
    }

}
