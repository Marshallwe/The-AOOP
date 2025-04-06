
package cli;

import gui.model.GameConfig;
import gui.model.WordLadderGame;
import gui.model.WordValidator;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class CLIGame {
    public static void start() {
        try {
            GameConfig config = createConfig();
            WordValidator validator = new WordValidator("dictionary.txt");
            WordLadderGame game = initializeGame(validator, config);

            runGameLoop(game, config);
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
    }

    public static GameConfig createConfig() {
        Scanner scanner = new Scanner(System.in);
        GameConfig config = new GameConfig();

        System.out.print("Display error messages?(y/n): ");
        config.setShowErrorMessages(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.print("Show the transition path?(y/n): ");
        config.setDisplayPath(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.print("Using random words?(y/n): ");
        config.setUseRandomWords(scanner.nextLine().equalsIgnoreCase("y"));

        return config;
    }

    public static WordLadderGame initializeGame(WordValidator validator, GameConfig config) throws IOException {
        if (config.isUseRandomWords()) {
            List<String> words = validator.getRandomWordPair();
            return new WordLadderGame(words.get(0), words.get(1), validator, config);
        }
        return new WordLadderGame("star", "moon", validator, config);
    }

    public static void runGameLoop(WordLadderGame game, GameConfig config) {
        System.out.println("\nWelcome to Word Ladder!");
        System.out.println("will '" + game.getCurrentWord().toUpperCase()
                + "' convert to '" + game.getTargetWord().toUpperCase() + "'\n");

        Scanner scanner = new Scanner(System.in);

        while (!game.isWin()) {
            displayCurrentState(game, config);

            System.out.print("Enter the next word (4 letters): ");
            String input = scanner.nextLine().trim().toLowerCase();

            processInput(game, input, config);
        }

        displayVictory(game, config);
    }

    public static void displayCurrentState(WordLadderGame game, GameConfig config) {
        System.out.println("\n[Current word] " + game.getCurrentWord().toUpperCase());

        if (config.isDisplayPath()) {
            System.out.println("Current path: " +
                    String.join(" → ", game.getTransformationPath()));
        }
    }

    public static void processInput(WordLadderGame game, String input, GameConfig config) {
        if (game.submitAttempt(input)) {
            displayFeedback(game.getCharacterStatus(input));
        } else {
            handleInvalidAttempt(config);
        }
    }

    private static void displayFeedback(List<WordLadderGame.CharacterStatus> statuses) {
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
            }
        });
        System.out.println("\u001B[0m");
    }

    private static void handleInvalidAttempt(GameConfig config) {
        if (config.isShowErrorMessages()) {
            System.out.println(" Invalid word! Reason: ");
            System.out.println("1. must be one letter away from the current word ");
            System.out.println("2. must be a valid 4-letter dictionary word \n");
        } else {
            System.out.println(" Invalid input, please try again \n");
        }
    }

    public static void displayVictory(WordLadderGame game, GameConfig config) {
        System.out.println("\n\u001B[32mCongratulations! You used the "
                + game.getAttempts().size()
                + " step to victory!\u001B[0m");
        if (config.isDisplayPath()) {
            System.out.println("Complete path: " +
                    String.join(" → ", game.getTransformationPath()));
        }
    }
}