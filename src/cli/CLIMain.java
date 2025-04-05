// CLIMain.java
package cli;

public class CLIMain {
    public static void main(String[] args) {
        try {
            CLIGame.start();
        } catch (Exception e) {
            System.err.println("游戏启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}