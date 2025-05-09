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
        assert validator != null : "Validator initialization failed";
        assert validator.dictionary.size() > 0 : "Empty dictionary loaded";
    }

    /* Configuration Methods */

    /**
     * Checks if solution path display is enabled
     * @return true if optimal solution path should be displayed
     */
    public boolean isShowPathEnabled() {
        boolean result = config.showSolutionPath();
        assert result == config.showSolutionPath() : "Configuration state inconsistency";
        return config.showSolutionPath();
    }

    /**
     * Enables/disables display of the optimal solution path
     * @param enabled true to show solution path, false to hide
     */
    public void setShowPathEnabled(boolean enabled) {
        final boolean before = config.showSolutionPath();
        config.setShowSolutionPath(enabled);
        assert config.showSolutionPath() == enabled : "Configuration update failed";
        assert before != enabled : "Redundant configuration change";
        notify(NotificationType.CONFIG_CHANGED);
    }

    /* Game State Accessors */

    /**
     * Gets the optimal solution path for current game
     * @return Unmodifiable list of words in optimal solution path,
     *         or empty list if no game active
     */
    public List<String> getSolutionPath() {
        List<String> path = game != null ? game.getSolutionPath() : Collections.emptyList();
        if (game != null) {
            assert path.size() >= 2 : "Invalid solution path length: " + path.size();
            assert path.get(0).equals(game.start) : "Solution path start mismatch";
            assert path.get(path.size()-1).equals(game.target) : "Solution path end mismatch";
        }
        return path;
    }

    /**
     * Gets the starting word of current game
     * @return Starting word or empty string if no game active
     */
    public String getStartWord() {
        String result = game != null ? game.startWord() : "";
        if (game != null) {
            assert result.equals(game.start) : "Start word retrieval error";
        }
        return result;
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
            assert !validator.isValid(startWord) || !validator.isValid(targetWord) :
                    "Validation error on valid words";
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
            final int preAttemptCount = game.attemptCount();
            final String prevWord = game.current;
            if (game.submitAttempt(word)) {
                assert game.attemptCount() == preAttemptCount + 1 :
                        "Attempt count not incremented";
                assert game.current.equals(word.toLowerCase()) :
                        "Current word not updated";
                lastErrorMessage = null;
                notify(NotificationType.STATE_UPDATE);
                if (game.isWin()) {
                    assert game.current.equalsIgnoreCase(game.target) :
                            "Win state with incorrect current word";
                    notify(NotificationType.GAME_WON);
                }
                return true;
            }
            return false;
        } catch (IllegalStateException | IllegalArgumentException e) {
            assert game == null || !validator.isValid(word) :
                    "Validation error on valid input";
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
        assert game.path.size() == 1 : "Path not cleared after reset";
        assert game.attemptCount() == 0 : "Attempt count not reset";
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
        final boolean before = config.showErrors();
        config.setShowErrors(enabled);
        assert config.showErrors() == enabled : "Error display config update failed";
        assert before != enabled : "Redundant error config change";
        notify(NotificationType.CONFIG_CHANGED);
    }

    /* Game State Getters */

    /**
     * Gets current intermediate word state
     * @return Current word in solution path or empty string if no game
     */
    public String getCurrentWord() {
        String result = (game != null) ? game.currentWord() : "";
        if (game != null) {
            assert result.equals(game.current) : "Current word mismatch";
            assert game.path.contains(result) : "Current word not in path";
        }
        return result;
    }

    /**
     * Gets target word for current game
     * @return Target word or empty string if no game active
     */
    public String getTargetWord() {
        String result = (game != null) ? game.targetWord() : "";
        if (game != null) {
            assert result.equals(game.target) : "Target word mismatch";
            assert validator.isValid(result) : "Invalid target word in model";
        }
        return result;
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

        List<CharacterStatus> feedback = game.calculateFeedback(word);
        assert feedback.size() == 4 : "Invalid feedback size: " + feedback.size();
        assert feedback.stream().allMatch(Objects::nonNull) : "Null status in feedback";

        return feedback;
    }

    /**
     * Gets current game path (sequence of attempts)
     * @return Unmodifiable list of words in current path,
     *         or empty list if no game active
     */
    public List<String> getGamePath() {
        List<String> path = game != null ? game.getPath() : Collections.emptyList();
        if (game != null) {
            assert path.size() == game.attemptCount() + 1 : "Path size mismatch";
            assert path.get(0).equals(game.start) : "Path start mismatch";
            assert path.get(path.size()-1).equals(game.current) : "Path end mismatch";
        }
        return path;
    }

    /* Dictionary Operations */

    /**
     * Generates a valid random word pair from dictionary
     * @return Array containing [startWord, targetWord]
     * @throws IllegalStateException If dictionary contains insufficient words
     */
    public String[] generateValidWordPair() {
        String[] pair = validator.getValidWordPair();
        assert pair.length == 2 : "Invalid pair array length: " + pair.length;
        assert pair[0] != null && pair[1] != null : "Null values in word pair";
        assert !pair[0].equals(pair[1]) : "Duplicate words in pair";
        assert validator.isValid(pair[0]) : "Invalid start word: " + pair[0];
        assert validator.isValid(pair[1]) : "Invalid target word: " + pair[1];
        return pair;
    }

    /* Game Statistics */

    /**
     * Gets number of valid attempts made in current game
     * @return Attempt count or 0 if no game active
     */
    public int getAttemptCount() {
        int count = game != null ? game.attemptCount() : 0;
        if (game != null) {
            assert count == game.path.size() - 1 : "Attempt count mismatch";
        }
        return count;
    }

    /* Configuration Setters */

    /**
     * Enables/disables random word pair generation
     * @param enabled true to use random words for new games
     */
    public void setUseRandomWords(boolean enabled) {
        final boolean before = config.useRandomWords();
        config.setUseRandomWords(enabled);
        assert config.useRandomWords() == enabled : "Random words config update failed";
        assert before != enabled : "Redundant random words config change";
        notify(NotificationType.CONFIG_CHANGED);
    }

    /* Configuration Getters */

    /**
     * Checks if random word pair generation is enabled
     * @return true if random words are enabled
     */
    public boolean isRandomWordsEnabled() {
        boolean result = config.useRandomWords();
        assert result == config.useRandomWords() : "Configuration state inconsistency";
        return result;
    }

    /**
     * Checks if error message display is enabled
     * @return true if errors should be displayed
     */
    public boolean isErrorDisplayEnabled() {
        boolean result = config.showErrors();
        assert result == config.showErrors() : "Configuration state inconsistency";
        return result;
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
        assert Arrays.stream(words).allMatch(w -> w.length() == 4) : "Word length validation failed";
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
        assert Arrays.stream(words).allMatch(validator::isValid) : "Dictionary validation failed";
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
        assert game.start != null : "Missing start word";
        assert game.target != null : "Missing target word";
        assert game.path != null : "Missing game path";
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
        assert !hasChanged() : "Observer state not cleared";
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
            assert dictionary.contains(start) : "Start word not in dictionary";
            assert dictionary.contains(target) : "Target word not in dictionary";
            Queue<List<String>> queue = new LinkedList<>();
            queue.add(Arrays.asList(start.toLowerCase()));
            Set<String> visited = new HashSet<>();
            visited.add(start.toLowerCase());

            while (!queue.isEmpty()) {
                List<String> path = queue.poll();
                String current = path.get(path.size() - 1);
                assert path.size() <= 20 :
                        "Unreasonable solution path length: " + path.size();
                assert validator.isValid(current) :
                        "Invalid word in solution path: " + current;
                if (current.equalsIgnoreCase(target)) {
                    assert path.get(0).equalsIgnoreCase(start) :
                            "Solution path start mismatch";
                    assert path.get(path.size()-1).equalsIgnoreCase(target) :
                            "Solution path target mismatch";
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
                            assert newPath.size() == path.size() + 1 :
                                    "Path length increment error";
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
            final String originalCurrent = this.current;
            if (isValidAttempt(attempt)) {
                path.add(attempt.toLowerCase());
                current = attempt.toLowerCase();
                assert isSingleLetterChange(originalCurrent, current) :
                        "Invalid state transition: " + originalCurrent + " → " + current;
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
            final String currentLower = current.toLowerCase();
            final String targetLower = target.toLowerCase();
            int correctCount = 0;
            int presentCount = 0;

            for (CharacterStatus status : statuses) {
                if (status == CharacterStatus.CORRECT_POSITION) correctCount++;
                if (status == CharacterStatus.PRESENT_IN_WORD) presentCount++;
            }

            int actualCorrect = 0;
            for (int i = 0; i < 4; i++) {
                if (currentLower.charAt(i) == targetLower.charAt(i)) actualCorrect++;
            }
            assert correctCount == actualCorrect :
                    "Correct position count mismatch: " + correctCount + " vs " + actualCorrect;

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
                Set<String> dict = lines.map(String::trim)
                        .filter(word -> word.length() == 4)
                        .map(word -> word.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                assert !dict.isEmpty() : "Empty dictionary loaded";
                dict.forEach(word -> {
                    assert word.length() == 4 : "Invalid word length: " + word;
                    assert word.toLowerCase().equals(word) : "Non-lowercase word: " + word;
                });
                return dict;
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