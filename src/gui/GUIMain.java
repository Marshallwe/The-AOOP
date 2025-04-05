// GUIMain.java
package gui;


import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class GUIMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GameConfig config = createGUIConfig();
                WordValidator validator = new WordValidator("dictionary.txt");
                WordLadderGame game = createGame(validator, config);
                GameView view = new GameView();

                new GameController(view, game, config);

                view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                view.pack();
                view.setLocationRelativeTo(null);
                view.setVisible(true);
            } catch (IOException e) {
                handleGUILoadError(e);
            }
        });
    }

    private static GameConfig createGUIConfig() {
        GameConfig config = new GameConfig();
        int result = JOptionPane.showConfirmDialog(
                null,
                "是否使用随机起始单词？",
                "游戏设置",
                JOptionPane.YES_NO_OPTION
        );
        config.setUseRandomWords(result == JOptionPane.YES_OPTION);
        return config;
    }

    private static WordLadderGame createGame(WordValidator validator, GameConfig config)
            throws IOException {
        if (config.isUseRandomWords()) {
            List<String> words = validator.getRandomWordPair();
            return new WordLadderGame(
                    words.get(0),
                    words.get(1),
                    validator,
                    config
            );
        }
        return new WordLadderGame("star", "moon", validator, config);
    }

    private static void handleGUILoadError(IOException e) {
        JOptionPane.showMessageDialog(
                null,
                "游戏初始化失败:\n" + e.getMessage(),
                "致命错误",
                JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }
}