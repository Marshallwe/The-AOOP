package cli;

import gui.model.Model;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface implementation for the Word Ladder game.
 * Manages game flow, user interactions, and console-based display.
 * Provides colored feedback and optional solution hints.
 */
public class CLIGame {
    private static Model model;
    private static boolean showHints = false;

    // ANSI escape codes for colored text output
    private static final String GREEN = "\u001B[32m";   // Correct position
    private static final String YELLOW = "\u001B[33m";  // Present in word
    private static final String WHITE = "\u001B[37m";   // Default color
    private static final String CYAN = "\u001B[36m";    // Progress path
    private static final String RESET = "\u001B[0m";    // Reset terminal color

    /**
     * Main entry point for the CLI game.
     * Initializes game components and handles top-level exceptions.
     */
    public static void start() {
        try {
            model = new Model("dictionary.txt");
            assert model != null : "Model initialization failed";
            configureSettings();
            initializeGame();
            runGameLoop();
        } catch (IOException e) {
            System.err.println("Error initializing game: " + e.getMessage());
        }
    }

    /**
     * Configures game settings through user input.
     * Sets up error display, solution hints, and word selection mode.
     */
    private static void configureSettings() {
        Scanner scanner = new Scanner(System.in);

        // Error message display toggle
        System.out.print("Display error messages?(y/n): ");
        model.setErrorDisplayEnabled(scanner.nextLine().equalsIgnoreCase("y"));

        // Solution path hints toggle
        System.out.print("Enable solution path hints?(y/n): ");
        showHints = scanner.nextLine().equalsIgnoreCase("y");

        // Word selection mode
        System.out.print("Use random words?(y/n): ");
        model.setUseRandomWords(scanner.nextLine().equalsIgnoreCase("y"));
    }

    /**
     * Initializes game state based on configuration.
     * Uses either random word pair or default words.
     * @throws IOException If dictionary loading fails
     */
    private static void initializeGame() throws IOException {
        if (model.isRandomWordsEnabled()) {
            String[] words = model.generateValidWordPair();
            assert words != null && words.length == 2 :
                    "Invalid word pair generated: expected 2 elements";
            model.initializeGame(words[0], words[1]);
        } else {
            // Default word pair for demonstration
            model.initializeGame("star", "moon");
        }
        assert model.getCurrentWord() != null && !model.getCurrentWord().isEmpty() :
                "Current word not initialized";
        assert model.getTargetWord() != null && !model.getTargetWord().isEmpty() :
                "Target word not initialized";
    }

    /**
     * Main game loop controller.
     * Manages turn sequence and victory condition checking.
     */
    private static void runGameLoop() {
        System.out.println("\n" + GREEN + "Welcome to Word Ladder!" + RESET);
        printSolutionPath();

        Scanner scanner = new Scanner(System.in);
        while (!model.getCurrentWord().equalsIgnoreCase(model.getTargetWord())) {
            displayGameState();
            System.out.print("Enter next word (4 letters): ");
            processInput(scanner.nextLine().trim());
        }
        displayVictory();
    }

    /**
     * Displays the optimal conversion path at game start.
     */
    private static void printSolutionPath() {
        System.out.println("\nTarget Conversion Path:");
        System.out.println(GREEN + formatPath(model.getSolutionPath()) + RESET);
        System.out.println("\nYour Progress:");
    }

    /**
     * Shows current game state including:
     * - Current word
     * - Player's progress path
     * - Optional step hints
     */
    private static void displayGameState() {
        System.out.println("\n[Current] " + model.getCurrentWord().toUpperCase());
        System.out.println("Progress Path: " + formatProgressPath());
        if (showHints) showNextOptimalStep();
    }

    /**
     * Formats a word path as arrow-separated string.
     * @param path List of words in the path
     * @return Formatted path string in uppercase
     */
    private static String formatPath(List<String> path) {
        assert path != null : "Path list cannot be null";
        return String.join(" → ", path).toUpperCase();
    }

    /**
     * Formats player's progress path with cyan coloring.
     * @return Colored progress path string
     */
    private static String formatProgressPath() {
        return CYAN + String.join(" → ", model.getGamePath()).toUpperCase() + RESET;
    }

    /**
     * Displays the next recommended step in the solution path.
     * Highlights the character that needs to be changed.
     */
    private static void showNextOptimalStep() {
        List<String> solution = model.getSolutionPath();
        int currentStep = model.getAttemptCount();

        if (currentStep < solution.size() - 1) {
            String current = model.getCurrentWord();
            String nextStep = solution.get(currentStep + 1);
            System.out.println("\nNext Recommended Step: " + highlightDifference(current, nextStep));
        }
    }

    /**
     * Highlights the differing character between current and next step.
     * @param current Current word in the game
     * @param next Next recommended word in solution path
     * @return Formatted string with yellow highlight on changed character
     */
    private static String highlightDifference(String current, String next) {
        assert current != null && next != null : "Input words cannot be null";
        assert current.length() == 4 && next.length() == 4 :
                "Words must be 4 characters long";
        assert current.length() == next.length() :
                "Word length mismatch";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < current.length(); i++) {
            char c = current.charAt(i);
            char n = next.charAt(i);
            // Highlight changed character in yellow
            sb.append(c == n ? n : YELLOW + n + RESET);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Processes and validates user input.
     * @param input User's guessed word input
     */
    private static void processInput(String input) {
        // Validate input length first
        if (input.length() != 4) {
            if (model.isErrorDisplayEnabled()) {
                System.out.println("Error: Must be 4-letter word");
            }
            return;
        }

        // Submit valid attempt or show errors
        if (model.submitGuess(input)) {
            showColorFeedback(model.getCharacterFeedback(input));
        } else {
            showError();
        }
    }

    /**
     * Displays colored feedback for each character position.
     * @param feedback List of character status indicators
     */
    private static void showColorFeedback(List<Model.CharacterStatus> feedback) {
        assert feedback != null : "Feedback list cannot be null";
        feedback.forEach(status -> {
            switch (status) {
                case CORRECT_POSITION:
                    System.out.print(GREEN + "■ ");  // Green block
                    break;
                case PRESENT_IN_WORD:
                    System.out.print(YELLOW + "■ ");  // Yellow block
                    break;
                default:
                    System.out.print(WHITE + "■ ");  // White block
            }
        });
        System.out.println(RESET);  // Reset terminal color
    }

    /**
     * Displays validation rules for invalid attempts.
     */
    private static void showError() {
        if (model.isErrorDisplayEnabled()) {
            System.out.println("Invalid Attempt. Requirements:");
            System.out.println("1. Valid dictionary word");
            System.out.println("2. Change exactly 1 letter");
            System.out.println("3. No duplicate attempts");
        }
    }

    /**
     * Displays victory message with step count and path comparison.
     */
    private static void displayVictory() {
        assert model.getSolutionPath() != null : "Missing solution path";
        assert !model.getSolutionPath().isEmpty() : "Empty solution path";
        assert model.getGamePath() != null : "Missing game path";
        System.out.println("\n" + GREEN + "[VICTORY] Steps: " + model.getAttemptCount() + RESET);
        System.out.println("\nOptimal Path:");
        System.out.println(GREEN + formatPath(model.getSolutionPath()) + RESET);
        System.out.println("\nYour Path:");
        System.out.println(CYAN + formatPath(model.getGamePath()) + RESET);
    }
}