package gui;

import gui.controller.GameController;
import gui.model.Model;
import gui.view.GameView;
import javax.swing.*;

public class GUIMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Model model = new Model("dictionary.txt");
                GameView view = new GameView();

                model.addObserver(view);
                model.initializeGame("star", "moon");

                view.initializeWithModel(model);
                new GameController(view, model);
            } catch (Exception e) {
                showErrorDialog("Initialization Error: " + e.getMessage());
            }
        });
    }

    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null,
                message, "Initialization Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}