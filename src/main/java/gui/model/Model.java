package gui.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Model extends Observable {
    public enum CharacterStatus {
        CORRECT_POSITION, PRESENT_IN_WORD, NOT_PRESENT
    }

    public enum NotificationType {
        STATE_UPDATE, CONFIG_CHANGED, GAME_RESET, GAME_WON, ERROR
    }

    private final GameConfig config = new GameConfig();
    private WordLadderGame game;
    private final WordValidator validator;
    private String lastErrorMessage;

    public Model(String dictionaryPath) throws IOException {
        this.validator = new WordValidator(dictionaryPath);
    }

    public String getStartWord() {
        return game != null ? game.startWord() : "";
    }

    public void initializeGame(String startWord, String targetWord) {
        try {
            validateWords(startWord, targetWord);
            checkWordsInDictionary(startWord, targetWord);

            this.game = new WordLadderGame(
                    startWord.toLowerCase(),
                    targetWord.toLowerCase(),
                    validator,
                    config
            );
            notify(NotificationType.GAME_RESET);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            notify(NotificationType.ERROR);
        }
    }

    public boolean submitGuess(String word) {
        try {
            Objects.requireNonNull(word, "Input word cannot be null");
            checkGameInitialized();

            if (game.submitAttempt(word)) {
                notify(NotificationType.STATE_UPDATE);
                if (game.isWin()) {
                    notify(NotificationType.GAME_WON);
                }
                return true;
            }
            return false;
        } catch (IllegalStateException | IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            notify(NotificationType.ERROR);
            return false;
        }
    }

    public void resetGame() {
        checkGameInitialized();
        game.reset();
        notify(NotificationType.GAME_RESET);
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setErrorDisplayEnabled(boolean enabled) {
        config.setShowErrors(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }

    public void setPathDisplayEnabled(boolean enabled) {
        config.setShowPath(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }

    public String getCurrentWord() {
        return game.currentWord();
    }

    public String getTargetWord() {
        return game.targetWord();
    }

    public List<CharacterStatus> getCharacterFeedback(String word) {
        return game.calculateFeedback(word);
    }

    public Optional<List<String>> getGamePath() {
        return game.getPath();
    }

    public String[] generateValidWordPair() {
        return validator.getValidWordPair();
    }

    public int getAttemptCount() {
        return game != null ? game.attemptCount() : 0;
    }

    public void setUseRandomWords(boolean enabled) {
        config.setUseRandomWords(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }

    public boolean isRandomWordsEnabled() {
        return config.useRandomWords();
    }

    public boolean isErrorDisplayEnabled() {
        return config.showErrors();
    }

    public boolean isPathDisplayEnabled() {
        return config.showPath();
    }

    private void validateWords(String... words) {
        Arrays.stream(words).forEach(word -> {
            if (word == null || word.length() != 4) {
                throw new IllegalArgumentException("Invalid 4-letter word: " + word);
            }
        });
    }

    private void checkWordsInDictionary(String... words) {
        Arrays.stream(words)
                .filter(word -> !validator.isValid(word))
                .findFirst()
                .ifPresent(invalid -> {
                    throw new IllegalArgumentException("Invalid word: " + invalid);
                });
    }

    private void checkGameInitialized() {
        if (game == null) {
            throw new IllegalStateException("Game not initialized");
        }
    }

    private void notify(NotificationType type) {
        setChanged();
        super.notifyObservers(type);
        clearChanged();
    }

    private static class GameConfig {
        private boolean showErrors = true;
        private boolean showPath = true;
        private boolean useRandom = false;

        boolean showErrors() {
            return showErrors;
        }

        void setShowErrors(boolean show) {
            this.showErrors = show;
        }

        boolean showPath() {
            return showPath;
        }

        void setShowPath(boolean show) {
            this.showPath = show;
        }

        boolean useRandomWords() {
            return useRandom;
        }

        void setUseRandomWords(boolean enable) {
            this.useRandom = enable;
        }
    }

    private class WordLadderGame {
        private final String start;
        private final String target;
        private String current;
        private final List<String> path;
        private final WordValidator validator;
        private final GameConfig config;

        WordLadderGame(String start, String target, WordValidator validator, GameConfig config) {
            this.start = start;
            this.target = target;
            this.current = start;
            this.validator = validator;
            this.config = config;
            this.path = new ArrayList<>();
            this.path.add(start);
        }

        String startWord() {
            return start;
        }

        int attemptCount() {
            return path.size() - 1;
        }

        boolean submitAttempt(String attempt) {
            if (isValidAttempt(attempt)) {
                String newWord = attempt.toLowerCase();
                path.add(newWord);
                current = newWord;
                return true;
            }
            return false;
        }

        private boolean isValidAttempt(String attempt) {
            return attempt != null
                    && attempt.length() == 4
                    && validator.isValid(attempt)
                    && isSingleLetterChange(current, attempt)
                    && !attempt.equalsIgnoreCase(current);
        }

        private boolean isSingleLetterChange(String current, String attempt) {
            int diff = 0;
            for (int i = 0; i < 4; i++) {
                if (Character.toLowerCase(current.charAt(i)) != Character.toLowerCase(attempt.charAt(i))) {
                    if (++diff > 1) return false;
                }
            }
            return diff == 1;
        }

        List<CharacterStatus> calculateFeedback(String word) {
            char[] targetCopy = target.toCharArray().clone();
            CharacterStatus[] statuses = new CharacterStatus[4];
            Arrays.fill(statuses, CharacterStatus.NOT_PRESENT);

            for (int i = 0; i < 4; i++) {
                if (Character.toLowerCase(word.charAt(i)) == targetCopy[i]) {
                    statuses[i] = CharacterStatus.CORRECT_POSITION;
                    targetCopy[i] = 0;
                }
            }

            for (int i = 0; i < 4; i++) {
                if (statuses[i] == CharacterStatus.CORRECT_POSITION) continue;

                char c = Character.toLowerCase(word.charAt(i));
                for (int j = 0; j < 4; j++) {
                    if (targetCopy[j] == c) {
                        statuses[i] = CharacterStatus.PRESENT_IN_WORD;
                        targetCopy[j] = 0;
                        break;
                    }
                }
            }

            return Arrays.asList(statuses);
        }

        void reset() {
            current = start;
            path.clear();
            path.add(start);
            Model.this.notify(Model.NotificationType.STATE_UPDATE);
        }

        boolean isWin() {
            return current.equalsIgnoreCase(target);
        }

        Optional<List<String>> getPath() {
            return config.showPath()
                    ? Optional.of(Collections.unmodifiableList(path))
                    : Optional.empty();
        }

        String currentWord() {
            return current;
        }

        String targetWord() {
            return target;
        }
    }

    private static class WordValidator {
        private final Set<String> dictionary;
        private final Random random = new Random();

        WordValidator(String path) throws IOException {
            this.dictionary = loadDictionary(path);
        }

        private static Set<String> loadDictionary(String path) throws IOException {
            try (Stream<String> lines = Files.lines(Paths.get(path))) {
                return lines.map(String::trim)
                        .filter(word -> word.length() == 4)
                        .map(word -> word.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        boolean isValid(String word) {
            return word != null
                    && word.length() == 4
                    && dictionary.contains(word.toLowerCase());
        }

        String[] getValidWordPair() {
            List<String> words = new ArrayList<>(dictionary);
            if (words.size() < 2) {
                throw new IllegalStateException("Insufficient dictionary words");
            }
            Collections.shuffle(words);
            return new String[]{words.get(0), words.get(1)};
        }
    }
}