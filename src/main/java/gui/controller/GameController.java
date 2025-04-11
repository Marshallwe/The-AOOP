// GameController.java
package gui.controller;

import gui.model.Model;
import gui.view.GameView;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

public class GameController implements Observer {
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
        view.setupWindow();
    }
    private void initializeConfigControls() {
        addConfigToggle("Show Errors", model.isErrorDisplayEnabled(),
                model::setErrorDisplayEnabled);

        addConfigToggle("Show Path", model.isPathDisplayEnabled(),
                model::setPathDisplayEnabled);

        // 新增随机单词切换
        addConfigToggle("Random Words", model.isRandomWordsEnabled(),
                enabled -> {
                    model.setUseRandomWords(enabled);
                    handleConfigChange(); // 处理配置变更
                });

        view.addConfigSpacer(20);
    }

    // 新增配置变更处理方法
    private void handleConfigChange() {
        if (model.isRandomWordsEnabled()) {
            startNewGame(); // 启用随机时生成新词对
        } else {
            // 关闭随机时使用默认词对
            try {
                model.initializeGame("star", "moon");
            } catch (IllegalArgumentException e) {
                showFeedbackDialog("Configuration Error", e.getMessage(), true);
            }
        }
    }

    // GameController.java
    private void refreshGameState() {
        updateStartDisplay();
        updateTargetDisplay();
        updatePathDisplay();  // 新增
        updatePathVisibility();
        view.getResetButton().setEnabled(model.getAttemptCount() > 0);
    }
    private void updateStartDisplay() {
        view.setStartWordDisplay(model.getStartWord());
    }
    private void setupActionHandlers() {
        view.getSubmitButton().addActionListener(this::handleGuessSubmission);
        view.getResetButton().addActionListener(e -> model.resetGame());
        view.getResetButton().setEnabled(false);
    }



    private void addConfigToggle(String label, boolean initialState,
                                 ConfigToggleHandler handler) {
        JCheckBox toggle = new JCheckBox(label, initialState);
        toggle.addActionListener(e -> handler.toggle(toggle.isSelected()));
        view.addConfigControl(toggle);
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
        String input = view.getInputField().getText().trim().toLowerCase();
        if (!validateInputLength(input)) return;

        if (model.submitGuess(input)) {
            view.clearInputField();
        } else {
            showInvalidAttemptFeedback();
        }
    }

    private boolean validateInputLength(String input) {
        if (input.length() != 4) {
            showFeedbackDialog("Invalid Input", "Must be 4 characters", true);
            return false;
        }
        return true;
    }

    private void handleStateUpdate() {
        SwingUtilities.invokeLater(() -> {
            updateCharacterFeedback();
            updatePathDisplay();
            view.getResetButton().setEnabled(true);

            if (model.getCurrentWord().equalsIgnoreCase(model.getTargetWord())) {
                handleGameWon();
            }
        });
    }

    private void updateCharacterFeedback() {
        String current = model.getCurrentWord();
        view.updateCharacterStatus(
                model.getCharacterFeedback(current),
                current
        );
    }

    // GameController.java
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
            showFeedbackDialog("Invalid Move", message, true);
        }
    }

    private void handleGameReset() {
        view.resetUI(model.getCurrentWord());
        view.getResetButton().setEnabled(false);
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
        int choice = showGameResultDialog();
        if (choice == JOptionPane.YES_OPTION) {
            if (model.isRandomWordsEnabled()) {
                startNewGame();
            } else {
                try {
                    model.initializeGame("star", "moon");
                } catch (IllegalArgumentException e) {
                    showFeedbackDialog("Game Error", e.getMessage(), true);
                }
            }
        }
    }

    private int showGameResultDialog() {
        String message = String.format(
                "Success! Achieved in %d steps\nStart new game?",
                model.getAttemptCount()
        );

        return JOptionPane.showOptionDialog(
                view,
                message,
                "Victory",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"New Game", "Exit"},
                "New Game"
        );
    }

    // GameController.java
    private void startNewGame() {
        try {
            String[] words = model.generateValidWordPair();
            model.initializeGame(words[0], words[1]);
            updateStartDisplay();
            updateTargetDisplay();
            updatePathDisplay();
        } catch (Exception e) {
            showFeedbackDialog("Initialization Error",
                    "Failed to start new game: " + e.getMessage(), true);
        }
    }

    private void showFeedbackDialog(String title, String message, boolean isError) {
        JOptionPane.showMessageDialog(view, message, title,
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    @FunctionalInterface
    private interface ConfigToggleHandler {
        void toggle(boolean enabled);
    }
}