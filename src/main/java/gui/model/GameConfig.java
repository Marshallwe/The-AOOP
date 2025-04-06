package gui.model;

public class GameConfig {
    private boolean showErrorMessages;
    private boolean displayPath;
    private boolean useRandomWords;

    public GameConfig() {
        this(true, false, false);
    }

    public GameConfig(boolean showErrors, boolean displayPath, boolean randomWords) {
        this.showErrorMessages = showErrors;
        this.displayPath = displayPath;
        this.useRandomWords = randomWords;
    }

    // Getters
    public boolean isShowErrorMessages() { return showErrorMessages; }
    public boolean isDisplayPath() { return displayPath; }
    public boolean isUseRandomWords() { return useRandomWords; }

    // Setters
    public void setShowErrorMessages(boolean showErrorMessages) {
        this.showErrorMessages = showErrorMessages;
    }
    public void setDisplayPath(boolean displayPath) {
        this.displayPath = displayPath;
    }
    public void setUseRandomWords(boolean useRandomWords) {
        this.useRandomWords = useRandomWords;
    }
}