package gui.controller;

import gui.model.*;
import gui.view.GameView;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class GameController {
    private final GameView view;
    private final WordLadderGame game;
    private final GameConfig config;

    public GameController(GameView view, WordLadderGame game, GameConfig config) {
        this.view = view;
        this.game = game;
        this.config = config;
        initialize();
    }

    private void initialize() {
        setupUI();
        setupConfigControls();
        view.setTargetWord(game.getTargetWord());
    }

    private void setupUI() {
        view.resetUI(game.getCurrentWord());
        view.getSubmitButton().addActionListener(this::handleSubmit);
        view.getResetButton().addActionListener(this::handleReset);
        view.getResetButton().setEnabled(false);
    }

    private void setupConfigControls() {
        addToggleCheckbox("Displaying errors", config.isShowErrorMessages(),
                selected -> config.setShowErrorMessages(selected));
        addToggleCheckbox("Display path", config.isDisplayPath(),
                selected -> {
                    config.setDisplayPath(selected);
                    updatePathDisplay();
                });
        view.addConfigControl(Box.createHorizontalStrut(20));
    }

    private void addToggleCheckbox(String label, boolean initialState, ToggleHandler handler) {
        JCheckBox checkBox = new JCheckBox(label, initialState);
        checkBox.addActionListener(e -> handler.handle(checkBox.isSelected()));
        view.addConfigControl(checkBox);
    }

    private void handleSubmit(ActionEvent e) {
        String input = view.getInputField().getText().trim().toLowerCase();
        if (input.length() != 4) {
            showLengthError();
            return;
        }

        if (game.submitAttempt(input)) {
            handleValidAttempt(input);
        } else {
            handleInvalidAttempt();
        }
        view.clearInput();
        updatePathDisplay();
    }

    private void handleValidAttempt(String input) {
        view.updateCharacterDisplay(game.getCharacterStatus(input), input);
        view.getResetButton().setEnabled(true);
        if (game.isWin()) {
            showWinDialog();
        }
    }

    private void handleInvalidAttempt() {
        String message = config.isShowErrorMessages() ?
                "Invalid input! Check: \n1. Only one letter can be modified at a time \n2. Must be a valid word ":" Invalid attempt, please try again ";
        JOptionPane.showMessageDialog(view, message, "Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showLengthError() {
        JOptionPane.showMessageDialog(view, "Word must be 4 letters "," Typos", JOptionPane.ERROR_MESSAGE);
    }

    private void handleReset(ActionEvent e) {
        game.resetGame();
        view.resetUI(game.getCurrentWord());
        view.getResetButton().setEnabled(false);
        updatePathDisplay();
    }

    public void updatePathDisplay() {
        view.setPathDisplay(config.isDisplayPath() ?
                String.join(" → ", game.getTransformationPath()) : "");
    }

    private void showWinDialog() {
        int option = JOptionPane.showOptionDialog(view,
                String.format("Congratulations! You completed the challenge with the %d step!\n" +
                        "Do you want to play again?", game.getAttempts().size()),
                "Winning the game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{" Play one more game ", "quit game "}," Play one more game");

        if (option == JOptionPane.YES_OPTION) {
            game.resetGame();
            view.resetUI(game.getCurrentWord());
            view.getResetButton().setEnabled(false);
            updatePathDisplay();
        }
    }

    @FunctionalInterface
    private interface ToggleHandler {
        void handle(boolean selected);
    }
}