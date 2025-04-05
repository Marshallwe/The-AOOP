package gui;

import gui.controller.GameController;
import gui.model.WordLadderGame; // 明确导入
import gui.model.GameConfig;      // 明确导入
import gui.model.WordValidator;  // 明确导入
import gui.view.GameView;
import javax.swing.*;
import java.util.List;
public class GUIMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GameConfig config = createConfig();
                WordValidator validator = new WordValidator("dictionary.txt");
                gui.model.WordLadderGame game = createGame(validator, config);
                GameView view = new GameView();

                new GameController(view, game, config);
            } catch (Exception e) {
                handleError(e);
            }
        });
    }

    private static GameConfig createConfig() {
        GameConfig config = new GameConfig();
        int choice = JOptionPane.showConfirmDialog(null,
                "是否使用随机起始单词？", "游戏设置", JOptionPane.YES_NO_OPTION);
        config.setUseRandomWords(choice == JOptionPane.YES_OPTION);
        return config;
    }

    private static WordLadderGame createGame(WordValidator validator, GameConfig config) throws Exception {
        if (config.isUseRandomWords()) {
            List<String> words = validator.getRandomWordPair();
            return new WordLadderGame(words.get(0), words.get(1), validator, config);
        }
        return new WordLadderGame("star", "moon", validator, config);
    }

    private static void handleError(Exception e) {
        JOptionPane.showMessageDialog(null,
                "初始化失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}