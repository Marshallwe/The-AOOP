// GameController.java
package gui.controller;

import gui.model.Model;
import gui.view.GameView;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

public class GameController implements Observer {
    public interface ConfigToggleHandler {
        void toggle(boolean enabled);
    }
    private boolean windowInitialized = false;
    private final GameView view;
    private final Model model;

    public GameController(GameView view, Model model) {
        this.view = view;
        this.model = model;
        model.addObserver(this);
        initializeComponents();
    }

    private void initializeComponents() {
        setupActionHandlers();
        initializeConfigControls();
        refreshGameState();
        if (!windowInitialized) {
            view.setupWindow();
            windowInitialized = true;
        }
    }

    private void initializeConfigControls() {
        view.addConfigToggle("Show Errors", model.isErrorDisplayEnabled(),
                enabled -> model.setErrorDisplayEnabled(enabled));

        view.addConfigToggle("Show Path", model.isPathDisplayEnabled(),
                enabled -> model.setPathDisplayEnabled(enabled));

        view.addConfigToggle("Random Words", model.isRandomWordsEnabled(),
                enabled -> {
                    model.setUseRandomWords(enabled);
                    handleConfigChange();
                });

        view.addConfigSpacer(20);
    }

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

    private void refreshGameState() {
        updateStartDisplay();
        updateTargetDisplay();
        updatePathDisplay();
        updatePathVisibility();
        view.setResetButtonEnabled(model.getAttemptCount() > 0);
    }

    private void updateStartDisplay() {
        view.setStartWordDisplay(model.getStartWord());
    }

    private void setupActionHandlers() {
        view.setSubmitHandler(this::handleGuessSubmission);
        view.setResetHandler(e -> model.resetGame());
        view.setNewGameHandler(e -> startNewGame());
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Model.NotificationType) {
            switch ((Model.NotificationType) arg) {
                case STATE_UPDATE:
                    handleStateUpdate();
                    break;
                case CONFIG_CHANGED:
                    updatePathVisibility();
                    break;
                case GAME_RESET:
                    handleGameReset();
                    break;
                case GAME_WON:
                    handleGameWon();
                    break;
            }
        }
    }

    private void handleGuessSubmission(ActionEvent event) {
        String input = view.getUserInput().trim().toLowerCase();
        if (!validateInputLength(input)) return;

        if (model.submitGuess(input)) {
            view.clearInputField();
        } else {
            showInvalidAttemptFeedback();
        }
    }

    private boolean validateInputLength(String input) {
        if (input.length() != 4) {
            view.showFeedbackDialog("Invalid Input", "Must be 4 characters", true);
            return false;
        }
        return true;
    }

    private void handleStateUpdate() {
        updateCharacterFeedback();
        updatePathDisplay();
        view.setResetButtonEnabled(true);

        if (model.getCurrentWord().equalsIgnoreCase(model.getTargetWord())) {
            handleGameWon();
        }
    }

    private void updateCharacterFeedback() {
        String current = model.getCurrentWord();
        view.updateCharacterStatus(
                model.getCharacterFeedback(current),
                current
        );
    }

    private void updatePathDisplay() {
        if (model.isPathDisplayEnabled()) {
            model.getGamePath().ifPresent(path ->
                    view.setTransformationPathDisplay(String.join(" → ", path))
            );
        } else {
            view.setTransformationPathDisplay("");
        }
    }

    private void showInvalidAttemptFeedback() {
        if (model.isErrorDisplayEnabled()) {
            String message = "Invalid attempt! Requirements:\n" +
                    "- Must change exactly 1 letter\n" +
                    "- Must be valid dictionary word\n" +
                    "- Cannot repeat previous word";
            view.showFeedbackDialog("Invalid Move", message, true);
        }
    }

    private void handleGameReset() {
        view.resetUI(model.getCurrentWord());
        view.setResetButtonEnabled(false);
        updatePathVisibility();
        updateTargetDisplay();
        updateStartDisplay();
        updatePathDisplay();
    }

    private void updateTargetDisplay() {
        view.setTargetWordDisplay(model.getTargetWord());
    }

    private void updatePathVisibility() {
        view.setPathVisibility(model.isPathDisplayEnabled());
    }

    private void handleGameWon() {
        int choice = view.showGameResultDialog(model.getAttemptCount());
        if (choice == 0) {
            if (model.isRandomWordsEnabled()) {
                startNewGame();
            } else {
                try {
                    model.initializeGame("star", "moon");
                } catch (IllegalArgumentException e) {
                    view.showFeedbackDialog("Game Error", e.getMessage(), true);
                }
            }
        }else{
            System.exit(0);
        }
    }

    private void startNewGame() {
        try {
            if (model.isRandomWordsEnabled()) {
                String[] words = model.generateValidWordPair();
                model.initializeGame(words[0], words[1]);
            } else {
                model.initializeGame("star", "moon");
            }
            updateStartDisplay();
            updateTargetDisplay();
            updatePathDisplay();
        } catch (Exception e) {
            view.showFeedbackDialog("Initialization Error",
                    "Failed to start new game: " + e.getMessage(), true);
        }
    }
}