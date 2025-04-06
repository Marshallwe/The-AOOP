package cli;

public class CLIMain {
    public static void main(String[] args) {
        try {
            CLIGame.start();
        } catch (Exception e) {
            System.err.println("The game failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}