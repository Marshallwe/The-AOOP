package gui.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Core Model class for a Word Ladder game implementing the Observer pattern.
 * Manages game state, configuration, and dictionary validation.
 * Notifies observers of game state changes through NotificationType events.
 */
public class Model extends Observable {
    /**
     * Represents the status of individual characters in a guessed word
     */
    public enum CharacterStatus {
        /** Character is in the correct position */
        CORRECT_POSITION,
        /** Character exists in the word but in wrong position */
        PRESENT_IN_WORD,
        /** Character does not exist in the target word */
        NOT_PRESENT
    }

    /**
     * Notification types for observer updates
     */
    public enum NotificationType {
        /** Game state has changed (e.g. new guess submitted) */
        STATE_UPDATE,
        /** Configuration setting has been modified */
        CONFIG_CHANGED,
        /** Game has been reset to initial state */
        GAME_RESET,
        /** Player has successfully reached the target word */
        GAME_WON,
        /** An error condition occurred */
        ERROR
    }

    // Game configuration settings
    private final GameConfig config = new GameConfig();
    // Current game instance
    private WordLadderGame game;
    // Word validation component
    private final WordValidator validator;
    // Last error message to display
    private String lastErrorMessage;

    /**
     * Constructs a new Model instance with dictionary validation
     * @param dictionaryPath Path to dictionary file containing 4-letter words
     * @throws IOException If dictionary file cannot be read or is invalid
     * @throws IllegalArgumentException If dictionaryPath is null
     */
    public Model(String dictionaryPath) throws IOException {
        if (dictionaryPath == null) {
            throw new IllegalArgumentException("Dictionary path cannot be null");
        }
        this.validator = new WordValidator(dictionaryPath);
    }

    /* Configuration Methods */

    /**
     * Checks if solution path display is enabled
     * @return true if optimal solution path should be displayed
     */
    public boolean isShowPathEnabled() {
        return config.showSolutionPath();
    }

    /**
     * Enables/disables display of the optimal solution path
     * @param enabled true to show solution path, false to hide
     */
    public void setShowPathEnabled(boolean enabled) {
        config.setShowSolutionPath(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }

    /* Game State Accessors */

    /**
     * Gets the optimal solution path for current game
     * @return Unmodifiable list of words in optimal solution path,
     *         or empty list if no game active
     */
    public List<String> getSolutionPath() {
        return game != null ? game.getSolutionPath() : Collections.emptyList();
    }

    /**
     * Gets the starting word of current game
     * @return Starting word or empty string if no game active
     */
    public String getStartWord() {
        return game != null ? game.startWord() : "";
    }

    /* Core Game Management */

    /**
     * Initializes a new game session with specified words
     * @param startWord 4-letter starting word (must exist in dictionary)
     * @param targetWord 4-letter target word (must exist in dictionary)
     * @throws IllegalArgumentException If words are invalid or not in dictionary
     */
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
            lastErrorMessage = null;
            notify(NotificationType.GAME_RESET);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            notify(NotificationType.ERROR);
        }
    }

    /**
     * Submits a word guess attempt
     * @param word 4-letter guess to validate and process
     * @return true if valid attempt, false otherwise
     * @throws IllegalStateException If game not initialized
     * @throws IllegalArgumentException If word is invalid format
     */
    public boolean submitGuess(String word) {
        try {
            Objects.requireNonNull(word, "Input word cannot be null");
            checkGameInitialized();
            if (game.submitAttempt(word)) {
                lastErrorMessage = null;
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
     * Resets current game to initial state
     * @throws IllegalStateException If game not initialized
     */
    public void resetGame() {
        checkGameInitialized();
        game.reset();
        lastErrorMessage = null;
        notify(NotificationType.GAME_RESET);
    }

    /* Error Handling */

    /**
     * Gets last error message encountered
     * @return Error message string or null if no recent error
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Enables/disables error message display
     * @param enabled true to show error messages, false to suppress
     */
    public void setErrorDisplayEnabled(boolean enabled) {
        config.setShowErrors(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }

    /* Game State Getters */

    /**
     * Gets current intermediate word state
     * @return Current word in solution path or empty string if no game
     */
    public String getCurrentWord() {
        return (game != null) ? game.currentWord() : "";
    }

    /**
     * Gets target word for current game
     * @return Target word or empty string if no game active
     */
    public String getTargetWord() {
        return (game != null) ? game.targetWord() : "";
    }

    /**
     * Calculates character-level feedback for a guessed word
     * @param word 4-letter word to analyze
     * @return List of CharacterStatus for each character position
     * @throws IllegalArgumentException If word is invalid format or not in dictionary
     * @throws IllegalStateException If game not initialized
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
     * Gets current game path (sequence of attempts)
     * @return Unmodifiable list of words in current path,
     *         or empty list if no game active
     */
    public List<String> getGamePath() {
        return game != null ? game.getPath() : Collections.emptyList();
    }

    /* Dictionary Operations */

    /**
     * Generates a valid random word pair from dictionary
     * @return Array containing [startWord, targetWord]
     * @throws IllegalStateException If dictionary contains insufficient words
     */
    public String[] generateValidWordPair() {
        return validator.getValidWordPair();
    }

    /* Game Statistics */

    /**
     * Gets number of valid attempts made in current game
     * @return Attempt count or 0 if no game active
     */
    public int getAttemptCount() {
        return game != null ? game.attemptCount() : 0;
    }

    /* Configuration Setters */

    /**
     * Enables/disables random word pair generation
     * @param enabled true to use random words for new games
     */
    public void setUseRandomWords(boolean enabled) {
        config.setUseRandomWords(enabled);
        notify(NotificationType.CONFIG_CHANGED);
    }

    /* Configuration Getters */

    /**
     * Checks if random word pair generation is enabled
     * @return true if random words are enabled
     */
    public boolean isRandomWordsEnabled() {
        return config.useRandomWords();
    }

    /**
     * Checks if error message display is enabled
     * @return true if errors should be displayed
     */
    public boolean isErrorDisplayEnabled() {
        return config.showErrors();
    }

    /* Validation Helpers */

    /**
     * Validates word format (4-letter)
     * @param words Words to validate
     * @throws IllegalArgumentException If any word is invalid
     */
    private void validateWords(String... words) {
        Arrays.stream(words).forEach(word -> {
            if (word == null || word.length() != 4) {
                throw new IllegalArgumentException("Invalid 4-letter word: " + word);
            }
        });
    }

    /**
     * Verifies words exist in dictionary
     * @param words Words to check
     * @throws IllegalArgumentException If any word not in dictionary
     */
    private void checkWordsInDictionary(String... words) {
        Arrays.stream(words)
                .filter(word -> !validator.isValid(word))
                .findFirst()
                .ifPresent(invalid -> {
                    throw new IllegalArgumentException("Invalid word: " + invalid);
                });
    }

    /* State Validation */

    /**
     * Verifies game is initialized
     * @throws IllegalStateException If no active game
     */
    private void checkGameInitialized() {
        if (game == null) {
            throw new IllegalStateException("Game not initialized");
        }
    }

    /* Observer Notification */

    /**
     * Notifies observers with specified notification type
     * @param type Type of state change to notify
     */
    private void notify(NotificationType type) {
        setChanged();
        super.notifyObservers(type);
        clearChanged();
    }

    /**
     * Internal configuration storage class
     */
    private static class GameConfig {
        private boolean showErrors = true;
        private boolean useRandom = false;
        private boolean showSolutionPath = false;

        /**
         * @return true if solution path display is enabled
         */
        public boolean showSolutionPath() {
            return showSolutionPath;
        }

        /**
         * Toggles solution path visibility
         * @param enable true to show solution path
         */
        public void setShowSolutionPath(boolean enable) {
            this.showSolutionPath = enable;
        }

        /**
         * @return true if error messages should be displayed
         */
        boolean showErrors() {
            return showErrors;
        }

        /**
         * Toggles error message visibility
         * @param show true to enable error display
         */
        void setShowErrors(boolean show) {
            this.showErrors = show;
        }

        /**
         * @return true if random word pairs are enabled
         */
        boolean useRandomWords() {
            return useRandom;
        }

        /**
         * Toggles random word pair usage
         * @param enable true to use random words
         */
        void setUseRandomWords(boolean enable) {
            this.useRandom = enable;
        }
    }

    /**
     * Word Ladder game instance handling core gameplay logic
     */
    private class WordLadderGame {
        private final String start;
        private final String target;
        private String current;
        private final List<String> path;
        private final List<String> solutionPath;
        private final WordValidator validator;

        /**
         * Constructs new Word Ladder game instance
         * @param start Valid 4-letter starting word
         * @param target Valid 4-letter target word
         * @param validator Word validation component
         * @param config Game configuration
         */
        WordLadderGame(String start, String target, WordValidator validator, GameConfig config) {
            this.start = start;
            this.target = target;
            this.current = start;
            this.validator = validator;
            this.path = new ArrayList<>();
            this.path.add(start);
            this.solutionPath = findSolutionPath(start, target);
        }

        /**
         * Finds optimal solution path using BFS algorithm
         * @param start Starting word
         * @param target Target word
         * @return Shortest path as list of words, or empty list if no solution
         */
        private List<String> findSolutionPath(String start, String target) {
            Set<String> dictionary = validator.getDictionary();
            Queue<List<String>> queue = new LinkedList<>();
            queue.add(Arrays.asList(start.toLowerCase()));
            Set<String> visited = new HashSet<>();
            visited.add(start.toLowerCase());

            while (!queue.isEmpty()) {
                List<String> path = queue.poll();
                String current = path.get(path.size() - 1);
                if (current.equalsIgnoreCase(target)) {
                    return path;
                }
                // Generate all possible 1-letter variations
                for (int i = 0; i < current.length(); i++) {
                    char[] chars = current.toCharArray();
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == chars[i]) continue;
                        chars[i] = c;
                        String next = new String(chars).toLowerCase();
                        if (dictionary.contains(next) && !visited.contains(next)) {
                            visited.add(next);
                            List<String> newPath = new ArrayList<>(path);
                            newPath.add(next);
                            queue.add(newPath);
                        }
                    }
                }
            }
            return Collections.emptyList();
        }

        /**
         * @return Optimal solution path
         */
        public List<String> getSolutionPath() {
            return solutionPath;
        }

        /**
         * @return Original starting word
         */
        String startWord() {
            return start;
        }

        /**
         * @return Number of valid attempts made
         */
        int attemptCount() {
            return path.size() - 1;
        }

        /**
         * Submits and validates a guess attempt
         * @param attempt 4-letter word guess
         * @return true if valid attempt, false otherwise
         */
        boolean submitAttempt(String attempt) {
            if (isValidAttempt(attempt)) {
                path.add(attempt.toLowerCase());
                current = attempt.toLowerCase();
                return true;
            }
            return false;
        }

        /**
         * Validates attempt format and game rules
         * @param attempt Word to validate
         * @return true if valid 1-letter change from current state
         */
        private boolean isValidAttempt(String attempt) {
            return attempt != null
                    && attempt.length() == 4
                    && validator.isValid(attempt)
                    && isSingleLetterChange(current, attempt)
                    && !attempt.equalsIgnoreCase(current);
        }

        /**
         * Verifies single-letter difference between words
         * @param current Base word
         * @param attempt New word to compare
         * @return true if exactly one character differs
         */
        private boolean isSingleLetterChange(String current, String attempt) {
            int diff = 0;
            for (int i = 0; i < 4; i++) {
                if (Character.toLowerCase(current.charAt(i)) != Character.toLowerCase(attempt.charAt(i))) {
                    if (++diff > 1) return false;
                }
            }
            return diff == 1;
        }

        /**
         * Calculates per-character feedback for guessed word
         * @param word Guessed word to analyze
         * @return List of CharacterStatus for each position
         */
        List<CharacterStatus> calculateFeedback(String word) {
            char[] targetCopy = target.toCharArray().clone();
            CharacterStatus[] statuses = new CharacterStatus[4];
            Arrays.fill(statuses, CharacterStatus.NOT_PRESENT);

            // First pass: Check correct positions
            for (int i = 0; i < 4; i++) {
                if (Character.toLowerCase(word.charAt(i)) == targetCopy[i]) {
                    statuses[i] = CharacterStatus.CORRECT_POSITION;
                    targetCopy[i] = 0; // Mark as matched
                }
            }

            // Second pass: Check present in word
            for (int i = 0; i < 4; i++) {
                if (statuses[i] == CharacterStatus.CORRECT_POSITION) continue;

                char c = Character.toLowerCase(word.charAt(i));
                for (int j = 0; j < 4; j++) {
                    if (targetCopy[j] == c) {
                        statuses[i] = CharacterStatus.PRESENT_IN_WORD;
                        targetCopy[j] = 0; // Mark as matched
                        break;
                    }
                }
            }

            return Arrays.asList(statuses);
        }

        /**
         * Resets game to initial state
         */
        void reset() {
            current = start;
            path.clear();
            path.add(start);
            Model.this.notify(NotificationType.STATE_UPDATE);
        }

        /**
         * @return true if current word matches target
         */
        boolean isWin() {
            return current.equalsIgnoreCase(target);
        }

        /**
         * @return Unmodifiable list of current path
         */
        List<String> getPath() {
            return Collections.unmodifiableList(path);
        }

        /**
         * @return Current intermediate word
         */
        String currentWord() {
            return current;
        }

        /**
         * @return Target word for victory condition
         */
        String targetWord() {
            return target;
        }
    }

    /**
     * Dictionary validator and word pair generator
     */
    private static class WordValidator {
        private final Set<String> dictionary;
        private final Random random = new Random();

        /**
         * @return Read-only view of dictionary words
         */
        public Set<String> getDictionary() {
            return Collections.unmodifiableSet(dictionary);
        }

        /**
         * Constructs validator with dictionary from file
         * @param path Path to dictionary file
         * @throws IOException If file cannot be read or contains no valid words
         */
        WordValidator(String path) throws IOException {
            this.dictionary = loadDictionary(path);
            if (dictionary.isEmpty()) {
                throw new IOException("Dictionary file contains no valid 4-letter words.");
            }
        }

        /**
         * Loads and filters 4-letter words from file
         * @param path File path to read
         * @return Set of valid lowercase 4-letter words
         * @throws IOException If file cannot be read
         */
        private static Set<String> loadDictionary(String path) throws IOException {
            try (Stream<String> lines = Files.lines(Paths.get(path))) {
                return lines.map(String::trim)
                        .filter(word -> word.length() == 4)
                        .map(word -> word.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        /**
         * Validates word format and existence in dictionary
         * @param word Word to check
         * @return true if valid 4-letter dictionary word
         */
        boolean isValid(String word) {
            return word != null
                    && word.length() == 4
                    && dictionary.contains(word.toLowerCase());
        }

        /**
         * Generates random valid word pair from dictionary
         * @return Array containing two distinct words [start, target]
         * @throws IllegalStateException If insufficient dictionary words
         */
        String[] getValidWordPair() {
            List<String> words = new ArrayList<>(dictionary);
            if (words.size() < 2) {
                throw new IllegalStateException("Insufficient dictionary words");
            }

            // Select two distinct random indices
            int index1 = random.nextInt(words.size());
            int index2 = random.nextInt(words.size() - 1);
            if (index2 >= index1) index2++;

            return new String[]{words.get(index1), words.get(index2)};
        }
    }
}