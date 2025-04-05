// cli/CLIGame.java
package cli;

import gui.GameConfig;
import gui.WordLadderGame;
import gui.WordValidator;
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

    private static GameConfig createConfig() {
        Scanner scanner = new Scanner(System.in);
        GameConfig config = new GameConfig();

        System.out.print("显示错误信息？(y/n): ");
        config.setShowErrorMessages(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.print("显示转换路径？(y/n): ");
        config.setDisplayPath(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.print("使用随机单词？(y/n): ");
        config.setUseRandomWords(scanner.nextLine().equalsIgnoreCase("y"));

        return config;
    }

    private static WordLadderGame initializeGame(WordValidator validator, GameConfig config) throws IOException {
        if (config.isUseRandomWords()) {
            List<String> words = validator.getRandomWordPair();
            return new WordLadderGame(words.get(0), words.get(1), validator, config);
        }
        return new WordLadderGame("star", "moon", validator, config);
    }

    private static void runGameLoop(WordLadderGame game, GameConfig config) {
        System.out.println("\n欢迎来到文字阶梯游戏！");
        System.out.println("将 '" + game.getCurrentWord().toUpperCase()
                + "' 转换为 '" + game.getTargetWord().toUpperCase() + "'\n");

        Scanner scanner = new Scanner(System.in);

        while (!game.isWin()) {
            displayCurrentState(game, config);

            System.out.print("输入下一个单词（4字母）: ");
            String input = scanner.nextLine().trim().toLowerCase();

            processInput(game, input, config);
        }

        displayVictory(game);
    }

    private static void displayCurrentState(WordLadderGame game, GameConfig config) {
        System.out.println("\n[当前单词] " + game.getCurrentWord().toUpperCase());

        if (config.isDisplayPath()) {
            System.out.println("当前路径: " +
                    String.join(" → ", game.getTransformationPath()));
        }
    }

    private static void processInput(WordLadderGame game, String input, GameConfig config) {
        if (game.submitAttempt(input)) {
            displayFeedback(game.getFeedback(input));
        } else {
            handleInvalidAttempt(config);
        }
    }

    private static void displayFeedback(List<WordLadderGame.CharacterFeedback> feedback) {
        feedback.forEach(f -> System.out.print(f.getAnsiColor() + "■ "));
        System.out.println("\u001B[0m");
    }

    private static void handleInvalidAttempt(GameConfig config) {
        if (config.isShowErrorMessages()) {
            System.out.println("无效单词！原因：");
            System.out.println("1. 必须与当前单词仅差一个字母");
            System.out.println("2. 必须是字典中的有效4字母单词\n");
        } else {
            System.out.println("输入无效，请重试\n");
        }
    }

    private static void displayVictory(WordLadderGame game) {
        System.out.println("\n\u001B[32m恭喜！您用了 "
                + game.getAttempts().size()
                + " 步获得胜利！\u001B[0m");
        if (game.getConfig().isDisplayPath()) {
            System.out.println("完整路径: " +
                    String.join(" → ", game.getTransformationPath()));
        }
    }
}