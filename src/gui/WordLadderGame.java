
package gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordLadderGame {
    public enum CharacterFeedback {
        GREEN(Color.GREEN, "\u001B[32m"),
        GREY(Color.GRAY, "\u001B[90m");

        private final Color guiColor;
        private final String ansiCode;

        CharacterFeedback(Color guiColor, String ansiCode) {
            this.guiColor = guiColor;
            this.ansiCode = ansiCode;
        }

        public Color getColor() {
            return this.guiColor;
        }

        public String getAnsiColor() {
            return this.ansiCode;
        }
    }

    private final String startWord;
    private final String targetWord;
    private String currentWord;
    private final WordValidator validator;
    private final List<String> attempts = new ArrayList<>();
    private final GameConfig config;
    private final List<String> transformationPath = new ArrayList<>();

    public WordLadderGame(String start, String target, WordValidator validator, GameConfig config) {
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

    public List<CharacterFeedback> getFeedback(String word) {
        List<CharacterFeedback> feedback = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            char c = word.charAt(i);
            if (c == targetWord.charAt(i)) {
                feedback.add(CharacterFeedback.GREEN);
            } else if (!targetWord.contains(String.valueOf(c))) {
                feedback.add(CharacterFeedback.GREY);
            } else {
                feedback.add(CharacterFeedback.GREY);
            }
        }
        return feedback;
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

    public GameConfig getConfig() {
        return config;
    }
}