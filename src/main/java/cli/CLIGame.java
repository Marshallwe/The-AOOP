package cli;

import gui.model.Model;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class CLIGame {
    private static Model model;

    public static void start() {
        try {
            model = new Model("dictionary.txt");
            configureSettings();
            initializeGame();
            runGameLoop();
        } catch (IOException e) {
            System.err.println("Error initializing game: " + e.getMessage());
        }
    }

    private static void configureSettings() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Display error messages?(y/n): ");
        model.setErrorDisplayEnabled(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.print("Show the transition path?(y/n): ");
        model.setPathDisplayEnabled(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.print("Use random words?(y/n): ");
        model.setUseRandomWords(scanner.nextLine().equalsIgnoreCase("y"));
    }

    private static void initializeGame() throws IOException {
        if (model.isRandomWordsEnabled()) {
            String[] words = model.generateValidWordPair();
            model.initializeGame(words[0], words[1]);
        } else {
            model.initializeGame("star", "moon");
        }
    }

    private static void runGameLoop() {
        System.out.println("\nWelcome to Word Ladder!");
        System.out.println("Convert '" + model.getCurrentWord().toUpperCase()
                + "' to '" + model.getTargetWord().toUpperCase() + "'\n");

        Scanner scanner = new Scanner(System.in);

        while (!model.getCurrentWord().equalsIgnoreCase(model.getTargetWord())) {
            displayCurrentState();
            System.out.print("Enter next word (4 letters): ");
            String input = scanner.nextLine().trim();
            processInput(input);
        }
        displayVictory();
    }

    private static void displayCurrentState() {
        System.out.println("\n[Current] " + model.getCurrentWord().toUpperCase());

        if (model.isPathDisplayEnabled()) {
            Optional<List<String>> pathOpt = model.getGamePath();
            pathOpt.ifPresent(path ->
                    System.out.println("Path: " + String.join(" → ", path))
            );
        }
    }

    private static void processInput(String input) {
        if (input.length() != 4) {
            if (model.isErrorDisplayEnabled()) {
                System.out.println("Error: Input must be exactly 4 letters\n");
            }
            return;
        }

        if (model.submitGuess(input)) {
            displayFeedback(model.getCharacterFeedback(input));
        } else {
            handleInvalidAttempt();
        }
    }

    private static void displayFeedback(List<Model.CharacterStatus> statuses) {
        statuses.forEach(status -> {
            switch (status) {
                case CORRECT_POSITION:
                    System.out.print("\u001B[32m■ ");
                    break;
                case PRESENT_IN_WORD:
                    System.out.print("\u001B[33m■ ");
                    break;
                default:
                    System.out.print("\u001B[37m■ ");
                    break;
            }
        });
        System.out.println("\u001B[0m");
    }

    private static void handleInvalidAttempt() {
        if (model.isErrorDisplayEnabled()) {
            System.out.println("Invalid! Must be:");
            System.out.println("1. Valid dictionary word");
            System.out.println("2. Exactly 1 letter changed");
            System.out.println("3. Not a previous attempt\n");
        }
    }
    private static void displayVictory() {
        String victoryBanner = "\n\u001B[32m[ VICTORY ] Achieved in "
                + model.getAttemptCount() + " steps\u001B[0m";
        System.out.println(victoryBanner);

        if (model.isPathDisplayEnabled()) {
            model.getGamePath().ifPresent(path ->
                    System.out.println("Complete path: " + String.join(" → ", path))
            );
        }
    }
}