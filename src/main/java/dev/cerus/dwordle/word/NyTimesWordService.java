package dev.cerus.dwordle.word;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NyTimesWordService implements WordService {

    private static final String URL = "https://www.nytimes.com/games/wordle/main.bd4cb59c.js";
    private static final String START = "var Ma=[\"";
    private static final String END = "\"],Ra=";
    private static final String SPLIT = ",Oa=\\[\"";
    private static final String DELIMITER = "\",\"";

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

            // Trim response content
            String trimmed = response.substring(response.indexOf(START) + START.length());
            trimmed = trimmed.substring(0, trimmed.indexOf(END));
            final String[] split = trimmed.split(SPLIT);

            // Select words and inputs
            final String[] words = split[0].split(DELIMITER);
            final String[] inputs = split[1].split(DELIMITER);

            NyTimesWordService.this.words.addAll(Arrays.asList(words));
            NyTimesWordService.this.inputs.addAll(Arrays.asList(inputs));
            future.complete(null);
        });

        return future;
    }

    @Override
    public boolean isValidInput(final String str) {
        return this.inputs.contains(str.toLowerCase())
                || this.words.contains(str.toLowerCase());
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
