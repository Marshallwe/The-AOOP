package cli;

import gui.model.Model;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Command-line interface implementation for the Word Ladder game.
 * Manages game flow, user interactions, and display of game state through console.
 */
public class CLIGame {
    private static Model model;

    /**
     * Main entry point for the CLI game.
     * Initializes game components and handles top-level exception handling.
     */
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

    /**
     * Configures game settings through user input.
     * Collects preferences for error messages, path display, and word selection mode.
     */
    private static void configureSettings() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Display error messages?(y/n): ");
        model.setErrorDisplayEnabled(scanner.nextLine().equalsIgnoreCase("y"));


        System.out.print("Use random words?(y/n): ");
        model.setUseRandomWords(scanner.nextLine().equalsIgnoreCase("y"));
    }

    /**
     * Initializes game state based on configuration.
     * Uses random word pair if enabled, otherwise uses default words.
     * @throws IOException If dictionary loading fails
     */
    private static void initializeGame() throws IOException {
        if (model.isRandomWordsEnabled()) {
            String[] words = model.generateValidWordPair();
            model.initializeGame(words[0], words[1]);
        } else {
            model.initializeGame("star", "moon");
        }
    }

    /**
     * Main game loop controller.
     * Manages turn sequence and victory condition checking.
     */
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

    /**
     * Displays current game state including current word and transition path.
     */
    private static void displayCurrentState() {
        System.out.println("\n[Current] " + model.getCurrentWord().toUpperCase());


    }

    /**
     * Processes and validates user input.
     * @param input User's guessed word input
     */
    private static void processInput(String input) {
        // Validate input length
        if (input.length() != 4) {
            if (model.isErrorDisplayEnabled()) {
                System.out.println("Error: Input must be exactly 4 letters\n");
            }
            return;
        }

        // Submit valid attempt
        if (model.submitGuess(input)) {
            displayFeedback(model.getCharacterFeedback(input));
        } else {
            handleInvalidAttempt();
        }
    }

    /**
     * Displays colored feedback for each character position.
     * @param statuses List of character status indicators
     */
    private static void displayFeedback(List<Model.CharacterStatus> statuses) {
        statuses.forEach(status -> {
            switch (status) {
                case CORRECT_POSITION:
                    System.out.print("\u001B[32m■ ");  // Green
                    break;
                case PRESENT_IN_WORD:
                    System.out.print("\u001B[33m■ ");  // Yellow
                    break;
                default:
                    System.out.print("\u001B[37m■ ");  // White
                    break;
            }
        });
        System.out.println("\u001B[0m");  // Reset color
    }

    /**
     * Handles invalid attempts by displaying validation rules.
     */
    private static void handleInvalidAttempt() {
        if (model.isErrorDisplayEnabled()) {
            System.out.println("Invalid! Must be:");
            System.out.println("1. Valid dictionary word");
            System.out.println("2. Exactly 1 letter changed");
            System.out.println("3. Not a previous attempt\n");
        }
    }

    /**
     * Displays victory message with step count and complete path.
     */
    private static void displayVictory() {
        String victoryBanner = "\n\u001B[32m[ VICTORY ] Achieved in "
                + model.getAttemptCount() + " steps\u001B[0m";
        System.out.println(victoryBanner);


    }
}