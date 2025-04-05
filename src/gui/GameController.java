// GameController.java
package gui;


import javax.swing.*;
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
        initializeUI();
        view.setTargetWord(game.getTargetWord());
        setupConfigControls();
    }

    private void initializeUI() {
        view.resetUI(game.getCurrentWord());
        view.getSubmitButton().addActionListener(this::onSubmit);
        view.getResetButton().addActionListener(this::onReset);
        view.getResetButton().setEnabled(false);
    }

    private void setupConfigControls() {
        // 错误显示开关
        JCheckBox errorToggle = new JCheckBox("显示错误", config.isShowErrorMessages());
        errorToggle.addActionListener(e ->
                config.setShowErrorMessages(errorToggle.isSelected()));

        // 路径显示开关
        JCheckBox pathToggle = new JCheckBox("显示路径", config.isDisplayPath());
        pathToggle.addActionListener(e -> {
            config.setDisplayPath(pathToggle.isSelected());
            updatePathDisplay();
        });

        view.addConfigControl(errorToggle);
        view.addConfigControl(pathToggle);
        view.addConfigControl((JComponent) Box.createHorizontalStrut(20));
    }

    private void onSubmit(ActionEvent e) {
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
        view.updateFeedback(game.getFeedback(input));
        view.getResetButton().setEnabled(true);
        if (game.isWin()) {
            showWinDialog();
        }
    }

    private void handleInvalidAttempt() {
        if (config.isShowErrorMessages()) {
            JOptionPane.showMessageDialog(
                    view,
                    "无效输入！请检查：\n1. 每次只能修改一个字母\n2. 必须是有效单词",
                    "错误详情",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    view,
                    "无效尝试，请重试",
                    "错误",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void showLengthError() {
        JOptionPane.showMessageDialog(
                view,
                "单词必须为4个字母",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void onReset(ActionEvent e) {
        game.resetGame();
        view.resetUI(game.getCurrentWord());
        view.getResetButton().setEnabled(false);
        updatePathDisplay();
    }

    private void updatePathDisplay() {
        if (config.isDisplayPath()) {
            List<String> path = game.getTransformationPath();
            view.setPathDisplay(String.join(" → ", path));
        } else {
            view.setPathDisplay("");
        }
    }

    private void showWinDialog() {
        int option = JOptionPane.showOptionDialog(
                view,
                "恭喜！您用" + game.getAttempts().size() + "步完成了挑战！\n再玩一次吗？",
                "游戏胜利",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"再来一局", "退出游戏"},
                "再来一局"
        );

        if (option == JOptionPane.YES_OPTION) {
            game.resetGame();
            view.resetUI(game.getCurrentWord());
            view.getResetButton().setEnabled(false);
            updatePathDisplay();
        }
    }
}