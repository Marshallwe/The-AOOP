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
        addToggleCheckbox("显示错误", config.isShowErrorMessages(),
                selected -> config.setShowErrorMessages(selected));
        addToggleCheckbox("显示路径", config.isDisplayPath(),
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
                "无效输入！请检查：\n1. 每次只能修改一个字母\n2. 必须是有效单词" : "无效尝试，请重试";
        JOptionPane.showMessageDialog(view, message, "错误", JOptionPane.WARNING_MESSAGE);
    }

    private void showLengthError() {
        JOptionPane.showMessageDialog(view, "单词必须为4个字母", "输入错误", JOptionPane.ERROR_MESSAGE);
    }

    private void handleReset(ActionEvent e) {
        game.resetGame();
        view.resetUI(game.getCurrentWord());
        view.getResetButton().setEnabled(false);
        updatePathDisplay();
    }

    private void updatePathDisplay() {
        view.setPathDisplay(config.isDisplayPath() ?
                String.join(" → ", game.getTransformationPath()) : "");
    }

    private void showWinDialog() {
        int option = JOptionPane.showOptionDialog(view,
                String.format("恭喜！您用%d步完成了挑战！\n再玩一次吗？", game.getAttempts().size()),
                "游戏胜利", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{"再来一局", "退出游戏"}, "再来一局");

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