package gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordLadderGame {
    public enum CharacterStatus {
        CORRECT_POSITION,
        PRESENT_IN_WORD,
        NOT_PRESENT
    }

    private final String startWord;
    private final String targetWord;
    private String currentWord;
    private final WordValidator validator;
    private final List<String> attempts = new ArrayList<>();
    private final GameConfig config;
    private final List<String> transformationPath = new ArrayList<>();

    public WordLadderGame(String start, String target, WordValidator validator, GameConfig config) {
        assert start != null : "Start word cannot be null";
        assert target != null : "Target word cannot be null";
        assert start.length() == 4 : "Start word must be 4 characters";
        assert target.length() == 4 : "Target word must be 4 characters";
        assert validator != null : "WordValidator cannot be null";
        assert config != null : "GameConfig cannot be null";

        if (start == null || target == null || start.length() != 4 || target.length() != 4) {
            throw new IllegalArgumentException("Start and target must be 4-letter words");
        }
        this.startWord = start.toLowerCase();
        this.targetWord = target.toLowerCase();
        this.currentWord = this.startWord;
        this.validator = validator;
        this.config = config;
        this.transformationPath.add(this.startWord);
    }

    public void resetGame() {
        this.currentWord = this.startWord;
        this.attempts.clear();
        this.transformationPath.clear();
        this.transformationPath.add(this.startWord);
    }

    public String getTargetWord() {
        return this.targetWord;
    }

    public boolean submitAttempt(String word) {
        assert word != null : "Submitted word cannot be null";
        assert word.length() == 4 : "Submitted word must be 4 characters";
        if (word == null || word.length() != 4
                || !validator.isValidWord(word)
                || !isOneLetterDifferent(currentWord, word)) {
            return false;
        }
        attempts.add(word);
        currentWord = word.toLowerCase();
        transformationPath.add(currentWord);
        return true;
    }

    private boolean isOneLetterDifferent(String word1, String word2) {
        assert word1 != null : "First word cannot be null";
        assert word2 != null : "Second word cannot be null";
        assert word1.length() == 4 : "First word must be 4 characters";
        assert word2.length() == 4 : "Second word must be 4 characters";
        if (word1.length() != 4 || word2.length() != 4) return false;
        int differences = 0;
        for (int i = 0; i < 4; i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                if (++differences > 1) return false;
            }
        }
        return differences == 1;
    }

    public boolean isWin() {
        return currentWord.equals(targetWord);
    }

    public List<CharacterStatus> getCharacterStatus(String word) {
        assert word != null : "Input word cannot be null";
        assert word.length() == 4 : "Input word must be 4 characters";
        List<CharacterStatus> statuses = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            char c = word.charAt(i);
            if (c == targetWord.charAt(i)) {
                statuses.add(CharacterStatus.CORRECT_POSITION);
            } else if (targetWord.contains(String.valueOf(c))) {
                statuses.add(CharacterStatus.PRESENT_IN_WORD);
            } else {
                statuses.add(CharacterStatus.NOT_PRESENT);
            }
        }
        return statuses;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public List<String> getAttempts() {
        return new ArrayList<>(attempts);
    }

    public List<String> getTransformationPath() {
        if (config.isDisplayPath()) {
            return Collections.unmodifiableList(transformationPath);
        }
        return Collections.emptyList();
    }
}