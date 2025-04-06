package gui;

import gui.controller.GameController;
import gui.model.WordLadderGame;
import gui.model.GameConfig;
import gui.model.WordValidator;
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
                "Do you use a random starting word?", "Game Settings", JOptionPane.YES_NO_OPTION);
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
                "Failed initialization:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}