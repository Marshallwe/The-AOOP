package gui.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * INVARIANTS:
 * 1. config != null
 * 2. validator != null && properly initialized with dictionary
 * 3. lastErrorMessage == null || contains last error description
 * 4. game == null || (startWord() and targetWord() are 4-letter lowercase)
 * 5. game == null || path maintains valid word ladder transitions
 * 6. config settings remain consistent between notifications
 */
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
    /**
     * @param dictionaryPath valid path to dictionary file
     * @throws IOException if file cannot be read
     * @pre dictionaryPath != null && file exists && readable
     * @post validator initialized with words from file
     */
    public Model(String dictionaryPath) throws IOException {
        if (dictionaryPath == null) {
            throw new IllegalArgumentException("Dictionary path cannot be null");
        }
        this.validator = new WordValidator(dictionaryPath);
        assert validator != null : "Validator must be initialized";
        assert config != null : "Config should never be null";
    }
    /**
     * @return current start word or empty string
     * @pre None
     * @post returns start word if initialized, else empty string
     */
    public String getStartWord() {
        return game != null ? game.startWord() : "";
    }
    /**
     * @param startWord initial word (4 letters)
     * @param targetWord target word (4 letters)
     * @throws IllegalArgumentException for invalid words
     * @pre startWord != null && targetWord != null
     * @pre startWord.length() == 4 && targetWord.length() == 4
     * @pre validator.isValid(startWord) && validator.isValid(targetWord)
     * @pre !startWord.equalsIgnoreCase(targetWord)
     * @post new game initialized with specified words
     */
    public void initializeGame(String startWord, String targetWord) {
        try {
            assert startWord != null : "Start word cannot be null";
            assert targetWord != null : "Target word cannot be null";
            assert startWord.length() == 4 : "Start word must be 4 letters";
            assert targetWord.length() == 4 : "Target word must be 4 letters";
            validateWords(startWord, targetWord);
            checkWordsInDictionary(startWord, targetWord);
            this.game = new WordLadderGame(
                    startWord.toLowerCase(),
                    targetWord.toLowerCase(),
                    validator,
                    config
            );
            assert game != null : "Game instance not created";
            assert game.attemptCount() == 0 : "Initial attempt count must be 0";
            lastErrorMessage = null;
            notify(NotificationType.GAME_RESET);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            notify(NotificationType.ERROR);
        }
    }

    /**
     * @param word guessed word
     * @return true if valid attempt
     * @throws IllegalStateException if game not initialized
     * @pre word != null && word.length() == 4
     * @pre validator.isValid(word)
     * @pre differs from current word by exactly 1 character
     * @post attempt count increments if valid
     */
    public boolean submitGuess(String word) {
        try {
            Objects.requireNonNull(word, "Input word cannot be null");
            checkGameInitialized();
            int prevAttempts = game.attemptCount();
            if (game.submitAttempt(word)) {
                lastErrorMessage = null;
                assert game.attemptCount() == prevAttempts + 1 : "Attempt count not incremented";
                assert game.currentWord().equalsIgnoreCase(word) : "Current word mismatch";
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
    /**
     * @throws IllegalStateException if game not initialized
     * @pre game != null
     * @post resets game state to initial configuration
     */
    public void resetGame() {
        checkGameInitialized();
        game.reset();
        lastErrorMessage = null;
        notify(NotificationType.GAME_RESET);
    }
    /** @return last error message or null */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    /**
     * @param enabled toggle error display
     * @post updates config.showErrors
     */
    public void setErrorDisplayEnabled(boolean enabled) {
        config.setShowErrors(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }
    /**
     * @param enabled toggle path display
     * @post updates config.showPath
     */
    public void setPathDisplayEnabled(boolean enabled) {
        config.setShowPath(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }
    /** @return current word in game */
    public String getCurrentWord() {
        return (game != null) ? game.currentWord() : "";
    }
    /** @return target word */
    public String getTargetWord() {
        return (game != null) ? game.targetWord() : "";
    }
    /**
     * @param word to check
     * @return list of character statuses
     * @post returns size 4 list with feedback
     */
    public List<CharacterStatus> getCharacterFeedback(String word) {
        Objects.requireNonNull(word, "Word cannot be null");
        if (word.length() != 4) {
            throw new IllegalArgumentException("Word must be 4 letters");
        }
        checkGameInitialized();
        if (!validator.isValid(word)) {
            throw new IllegalArgumentException("Invalid word: " + word);
        }
        return game.calculateFeedback(word);
    }
    /**
     * @return unmodifiable path if enabled
     * @post returns Optional.empty() if path display disabled
     */
    public Optional<List<String>> getGamePath() {
        return game.getPath();
    }
    /**
     * @return random word pair from dictionary
     * @throws IllegalStateException if insufficient words
     * @pre validator contains ≥2 words
     */
    public String[] generateValidWordPair() {
        return validator.getValidWordPair();
    }
    /** @return number of attempts made */
    public int getAttemptCount() {
        return game != null ? game.attemptCount() : 0;
    }
    /** @param enabled toggle random word selection */
    public void setUseRandomWords(boolean enabled) {
        config.setUseRandomWords(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }
    /** @return random words config state */
    public boolean isRandomWordsEnabled() {
        return config.useRandomWords();
    }
    /** @return error display config state */
    public boolean isErrorDisplayEnabled() {
        return config.showErrors();
    }
    /** @return path display config state */
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
    /**
     * INVARIANTS:
     * 1. All boolean flags have valid states
     */
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
    /**
     * INVARIANTS:
     * 1. start and target are 4-letter lowercase
     * 2. path maintains valid word ladder transitions
     */
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
                String previous = current;
                String newWord = attempt.toLowerCase();
                path.add(newWord);
                current = newWord;
                assert isSingleLetterChange(previous, newWord) : "Invalid single-letter change";
                assert path.size() >= 2 : "Path must grow with attempts";
                assert validator.isValid(newWord) : "Invalid word in path";
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
    /**
     * INVARIANTS:
     * 1. dictionary ≠ null && contains only 4-letter words
     */
    private static class WordValidator {
        private final Set<String> dictionary;
        private final Random random = new Random();
        WordValidator(String path) throws IOException {
            this.dictionary = loadDictionary(path);
            if (dictionary.isEmpty()) {
                throw new IOException("Dictionary file contains no valid 4-letter words.");
            }
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
            int index1 = random.nextInt(words.size());
            int index2 = random.nextInt(words.size() - 1);
            if (index2 >= index1) index2++;
            return new String[]{words.get(index1), words.get(index2)};
        }
    }
}