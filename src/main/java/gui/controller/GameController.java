// GameController.java
package gui.controller;

import gui.model.Model;
import gui.view.GameView;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

/**
 * MVC Controller component for managing game logic and coordinating between
 * Model and View. Handles user input, configuration changes, and game state updates.
 */
public class GameController{

    /** Functional interface for handling configuration toggle events */
    public interface ConfigToggleHandler {
        /**
         * Handles configuration toggle changes
         * @param enabled New state of the configuration option
         */
        void toggle(boolean enabled);
    }

    private boolean windowInitialized = false;
    private final GameView view;
    private final Model model;

    /**
     * Constructs a GameController with specified view and model
     * @param view GameView component to manage
     * @param model Model component containing game logic
     */
    public GameController(GameView view, Model model) {
        assert view != null : "GameView cannot be null";
        assert model != null : "Model cannot be null";
        this.view = view;
        this.model = model;
        initializeComponents();
        assert windowInitialized : "Controller initialization failed";
    }

    /** Initializes UI components and game state */
    private void initializeComponents() {
        setupActionHandlers();
        initializeConfigControls();
        refreshGameState();
        if (!windowInitialized) {
            view.setupWindow();
            windowInitialized = true;
        }
    }

    /** Sets up configuration controls in the view */
    private void initializeConfigControls() {
        // Error display toggle
        view.addConfigToggle("Show Errors", model.isErrorDisplayEnabled(),
                enabled -> {
                    assert !model.isRandomWordsEnabled() || enabled == model.isErrorDisplayEnabled()
                            : "Invalid error display state";
                    model.setErrorDisplayEnabled(enabled);
                });
        view.addConfigToggle("Show Solution Path", model.isShowPathEnabled(),
                enabled -> model.setShowPathEnabled(enabled));
        // Random words mode toggle
        view.addConfigToggle("Random Words", model.isRandomWordsEnabled(),
                enabled -> {
                    model.setUseRandomWords(enabled);
                    handleConfigChange();
                });

        view.addConfigSpacer(20);  // Visual separator
    }

    /** Handles configuration changes requiring game reset */
    private void handleConfigChange() {
        if (model.isRandomWordsEnabled()) {
            startNewGame();
        } else {
            try {
                model.initializeGame("star", "moon");
            } catch (IllegalArgumentException e) {
                view.showFeedbackDialog("Configuration Error", e.getMessage(), true);
            }
        }
    }

    /** Refreshes all game state displays */
    private void refreshGameState() {
        updateStartDisplay();
        updateTargetDisplay();
        view.setResetButtonEnabled(model.getAttemptCount() > 0);
    }

    /** Updates starting word display */
    private void updateStartDisplay() {
        view.setStartWordDisplay(model.getStartWord());
    }

    /** Configures event handlers for user actions */
    private void setupActionHandlers() {
        view.setSubmitHandler(this::handleGuessSubmission);
        view.setResetHandler(e -> model.resetGame());
        view.setNewGameHandler(e -> startNewGame());
    }



    /** Handles word submission from the view */
    private void handleGuessSubmission(ActionEvent event) {
        String input = view.getUserInput().trim().toLowerCase();
        assert input != null : "Null input in submission";
        if (!validateInputLength(input)) return;
        assert input.length() == 4 : "Invalid input length after validation";
        if (model.submitGuess(input)) {
            view.clearInputField();
        } else {
            showInvalidAttemptFeedback();
        }
    }

    /**
     * Validates user input length
     * @param input User-provided word
     * @return true if valid length, false otherwise
     */
    private boolean validateInputLength(String input) {
        if (input.length() != 4) {
            view.showFeedbackDialog("Invalid Input", "Must be 4 characters", true);
            return false;
        }
        return true;
    }

    /** Updates UI components after model state change */
    private void handleStateUpdate() {
        updateCharacterFeedback();
        view.setResetButtonEnabled(true);

        if (model.getCurrentWord().equalsIgnoreCase(model.getTargetWord())) {
            handleGameWon();
        }
    }

    /** Updates character position feedback visualization */
    private void updateCharacterFeedback() {
        String current = model.getCurrentWord();
        view.updateCharacterStatus(
                model.getCharacterFeedback(current),
                current
        );
    }



    /** Shows invalid attempt feedback dialog when enabled */
    private void showInvalidAttemptFeedback() {
        if (model.isErrorDisplayEnabled()) {
            String message = "Invalid attempt! Requirements:\n" +
                    "- Must change exactly 1 letter\n" +
                    "- Must be valid dictionary word\n" +
                    "- Cannot repeat previous word";
            view.showFeedbackDialog("Invalid Move", message, true);
        }
    }

    /** Handles game reset event */
    private void handleGameReset() {
        view.resetUI(model.getCurrentWord());
        view.setResetButtonEnabled(false);
        updateTargetDisplay();
        updateStartDisplay();
    }

    /** Updates target word display */
    private void updateTargetDisplay() {
        view.setTargetWordDisplay(model.getTargetWord());
    }



    /** Handles game completion scenario */
    private void handleGameWon() {
        int choice = view.showGameResultDialog(model.getAttemptCount());
        if (choice == 0) {  // User chose to restart
            if (model.isRandomWordsEnabled()) {
                startNewGame();
            } else {
                try {
                    model.initializeGame("star", "moon");
                } catch (IllegalArgumentException e) {
                    view.showFeedbackDialog("Game Error", e.getMessage(), true);
                }
            }
        } else {  // User chose to exit
            System.exit(0);
        }
    }

    /** Starts new game with current configuration */
    private void startNewGame() {
        try {
            if (model.isRandomWordsEnabled()) {
                String[] words = model.generateValidWordPair();
                assert words.length == 2 : "Invalid word pair array";
                assert words[0].length() == 4 && words[1].length() == 4
                        : "Generated words have invalid length";
                model.initializeGame(words[0], words[1]);
            } else {
                model.initializeGame("star", "moon");
            }
            assert model.getStartWord() != null && model.getTargetWord() != null
                    : "Game initialization failed";
            updateStartDisplay();
            updateTargetDisplay();
        } catch (Exception e) {
            view.showFeedbackDialog("Initialization Error",
                    "Failed to start new game: " + e.getMessage(), true);
        }
    }
}