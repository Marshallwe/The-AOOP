package cli;

import model.WordLadderGame;
import model.WordValidator;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class CLIGame {
    public static void start() {
        try {
            WordValidator validator = new WordValidator("dictionary.txt");
            WordLadderGame game = new WordLadderGame("star", "moon", validator);
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to Weaver CLI Game!");
            System.out.println("Transform '" + game.getCurrentWord().toUpperCase()
                    + "' to '" + game.getTargetWord().toUpperCase() + "'\n");

            while (!game.isWin()) {
                System.out.println("[Current Word] " + game.getCurrentWord().toUpperCase());
                System.out.print("Enter your next word (4 letters): ");
                String input = scanner.nextLine().trim();

                if (game.submitAttempt(input)) {
                    displayFeedback(game.getFeedback(input));
                } else {
                    System.out.println("Invalid word! Reasons:");
                    System.out.println("- Must change exactly 1 letter from current word");
                    System.out.println("- Must be a valid 4-letter word in dictionary\n");
                }
            }

            System.out.println("\n\u001B[32mCongratulations! You won in "
                    + game.getAttempts().size() + " steps.\u001B[0m");
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
    }

    private static void displayFeedback(List<WordLadderGame.CharacterFeedback> feedback) {
        feedback.forEach(f -> System.out.print(f.getAnsiColor() + "■ "));
        System.out.println("\u001B[0m");
    }
}