package gui.view;

import gui.controller.GameController;
import gui.model.Model;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Main game view component implementing the Observer pattern.
 * Handles all UI presentation and user interaction for the Word Ladder game.
 * Manages visual state, input handling, and model updates.
 */
public class GameView extends JFrame implements Observer {
    // UI Components
    private final JLabel[] characterLabels = new JLabel[4];  // Current word character displays
    private final JLabel[] targetCharacterLabels = new JLabel[4];  // Target word character displays
    private JPanel middleWordsPanel;  // Panel for displaying previous attempts
    private JPanel solutionPathPanel;  // Panel for showing optimal solution path
    private final JTextField inputField = new JTextField(4);  // Word input field
    private final JButton submitButton = new JButton("Submit");  // Submit guess button
    private final JButton resetButton = new JButton("Reset");  // Game reset button
    private final JButton newGameButton = new JButton("New Game");  // New game button
    private final JPanel configPanel = new JPanel();  // Configuration settings panel
    private final JLabel startLabel = new JLabel("Start:");  // Start word display
    private final JLabel targetLabel = new JLabel("Target:");  // Target word display

    private final ColorMapper colorMapper = new ColorMapper();  // Color mapping utility
    private boolean windowInitialized = false;  // Window initialization flag
    private Model model;  // Game model reference

    /**
     * Constructs the game view and initializes UI components.
     */
    public GameView() {
        initializeUIComponents();
    }

    /**
     * Handles game completion scenario.
     * Shows victory dialog and manages game restart/exit flow.
     * @param model Current game model instance
     */
    private void handleGameWon(Model model) {
        int choice = showGameResultDialog(model.getAttemptCount());
        if (choice == 0) {
            startNewGame(model);
        } else {
            System.exit(0);
        }
    }

    /**
     * Initializes a new game session with proper configuration.
     * @param model Current game model instance
     */
    private void startNewGame(Model model) {
        try {
            if (model.isRandomWordsEnabled()) {
                String[] words = model.generateValidWordPair();
                model.initializeGame(words[0], words[1]);
            } else {
                model.initializeGame("star", "moon");
            }
            fullRefreshUI(model);
        } catch (Exception e) {
            showFeedbackDialog("Error", "New game failed: " + e.getMessage(), true);
        }
    }

    /**
     * Performs complete UI refresh including component reset and redraw.
     * @param model Current game model instance
     */
    private void fullRefreshUI(Model model) {
        resetUI(model.getCurrentWord());
        updatePersistentDisplays(model);
        updateSolutionPathDisplay();
        updatePathDisplay();
        middleWordsPanel.removeAll();
        solutionPathPanel.removeAll();
        revalidate();
        repaint();
    }

    /**
     * Initializes view with game model reference.
     * @param model Game model to observe and display
     */
    public void initializeWithModel(Model model) {
        this.model = model;
        resetUI(model.getCurrentWord());
        updatePersistentDisplays(model);
        updatePathDisplay();
    }

    /**
     * Creates header section containing game state information.
     * @return Configured header panel
     */
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(createStartDisplay());
        panel.add(createTargetDisplay());
        panel.add(createSolutionPathDisplay());
        return panel;
    }

    /**
     * Creates solution path display area.
     * @return Initialized solution path panel
     */
    private JPanel createSolutionPathDisplay() {
        solutionPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        solutionPathPanel.add(new JLabel("Solution Path: "));
        return solutionPathPanel;
    }

    /**
     * Updates solution path display based on current model state.
     */
    private void updateSolutionPathDisplay() {
        solutionPathPanel.removeAll();
        solutionPathPanel.add(new JLabel("Solution Path: "));
        if (model != null && model.isShowPathEnabled()) {
            List<String> path = model.getSolutionPath();
            for (int i = 0; i < path.size(); i++) {
                JLabel label = new JLabel(path.get(i).toUpperCase());
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                solutionPathPanel.add(label);
                if (i < path.size() - 1) {
                    solutionPathPanel.add(new JLabel(" → "));
                }
            }
        }
        solutionPathPanel.revalidate();
        solutionPathPanel.repaint();
    }

    /**
     * Observer pattern implementation for model updates.
     * @param o Observable object
     * @param arg Notification type
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Model) {
            Model model = (Model) o;
            SwingUtilities.invokeLater(() -> {
                if (arg instanceof Model.NotificationType) {
                    handleModelNotification(model, (Model.NotificationType) arg);
                }
                updatePersistentDisplays(model);
            });
        }
    }

    /**
     * Routes model notifications to appropriate handlers.
     * @param model Game model instance
     * @param type Notification type
     */
    private void handleModelNotification(Model model, Model.NotificationType type) {
        switch (type) {
            case CONFIG_CHANGED:
                updateSolutionPathDisplay();
                updatePathDisplay();
                break;
            case STATE_UPDATE:
                updateCharacterStatus(
                        model.getCharacterFeedback(model.getCurrentWord()),
                        model.getCurrentWord()
                );
                updatePathDisplay();
                break;
            case GAME_RESET:
                resetUI(model.getCurrentWord());
                updateSolutionPathDisplay();
                updatePersistentDisplays(model);
                updatePathDisplay();
                break;
            case GAME_WON:
                handleGameWon(model);
                break;
        }
    }

    /**
     * Displays game completion dialog.
     * @param attemptCount Number of attempts made
     * @return User selection (0 = new game, 1 = exit)
     */
    public int showGameResultDialog(int attemptCount) {
        String message = String.format("Success! Achieved in %d steps\nStart new game?", attemptCount);
        return JOptionPane.showOptionDialog(
                this,
                message,
                "Victory",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"New Game", "Exit"},
                "New Game"
        );
    }

    /**
     * Displays feedback/error dialog.
     * @param title Dialog title
     * @param message Information message
     * @param isError true for error dialog, false for info
     */
    public void showFeedbackDialog(String title, String message, boolean isError) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Updates start word display.
     * @param word New start word to display
     */
    public void setStartWordDisplay(String word) {
        startLabel.setText("Start: " + word.toUpperCase());
    }

    /**
     * Updates target word display with color coding.
     * @param word New target word to display
     */
    public void setTargetWordDisplay(String word) {
        targetLabel.setText("Target: " + word.toUpperCase());
        String targetWord = word.toUpperCase();
        for (int i = 0; i < 4; i++) {
            targetCharacterLabels[i].setText(String.valueOf(targetWord.charAt(i)));
            targetCharacterLabels[i].setBackground(colorMapper.getColor(Model.CharacterStatus.CORRECT_POSITION));
        }
    }

    /**
     * Updates character status indicators with feedback colors.
     * @param statuses List of character statuses
     * @param word Current word being displayed
     */
    public void updateCharacterStatus(List<Model.CharacterStatus> statuses, String word) {
        for (int i = 0; i < 4; i++) {
            if (statuses != null && i < statuses.size()) {
                characterLabels[i].setBackground(colorMapper.getColor(statuses.get(i)));
            } else {
                characterLabels[i].setBackground(new Color(200, 200, 200));
            }
            characterLabels[i].setText(i < word.length()
                    ? String.valueOf(word.charAt(i)).toUpperCase()
                    : "_");
        }
    }

    /**
     * Resets UI components to initial state.
     * @param currentWord Word to display as current state
     */
    public void resetUI(String currentWord) {
        String word = currentWord.toUpperCase();
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setText(String.valueOf(word.charAt(i)));
            characterLabels[i].setBackground(new Color(200, 200, 200));
        }
        if (model != null) {
            setTargetWordDisplay(model.getTargetWord());
            setStartWordDisplay(model.getStartWord());
        }
        clearInputField();
        middleWordsPanel.removeAll();
        solutionPathPanel.removeAll();
        middleWordsPanel.revalidate();
        solutionPathPanel.revalidate();
    }

    /**
     * Sets handler for new game button.
     * @param listener Action listener to handle button clicks
     */
    public void setNewGameHandler(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }

    /**
     * Adds configuration toggle to settings panel.
     * @param label Toggle label text
     * @param initialState Initial toggle state
     * @param handler Configuration handler implementation
     */
    public void addConfigToggle(String label, boolean initialState,
                                GameController.ConfigToggleHandler handler) {
        JCheckBox toggle = new JCheckBox(label, initialState);
        toggle.addActionListener(e -> handler.toggle(toggle.isSelected()));
        addConfigControl(toggle);
    }

    /**
     * Updates persistent UI elements that shouldn't be cleared on reset.
     * @param model Current game model instance
     */
    private void updatePersistentDisplays(Model model) {
        setStartWordDisplay(model.getStartWord());
        setTargetWordDisplay(model.getTargetWord());
        setResetButtonEnabled(model.getAttemptCount() > 0);
    }

    /**
     * Initializes all UI components and layouts.
     */
    private void initializeUIComponents() {
        configureWindowSettings();
        buildMainLayout();
        setupInputFiltering();
    }

    /**
     * Configures main window properties.
     */
    private void configureWindowSettings() {
        setTitle("Word Ladder Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(1200, 1000));
    }

    /**
     * Constructs main application layout.
     */
    private void buildMainLayout() {
        add(createHeaderSection(), BorderLayout.NORTH);
        add(createCharacterDisplay(), BorderLayout.CENTER);
        add(createInputSection(), BorderLayout.SOUTH);
        add(createVirtualKeyboard(), BorderLayout.WEST);
        add(createConfigPanel(), BorderLayout.EAST);
    }

    /**
     * Creates configuration settings panel.
     * @return Configured settings panel
     */
    private JPanel createConfigPanel() {
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        return configPanel;
    }

    /**
     * Creates start word display panel.
     * @return Configured start display panel
     */
    private JPanel createStartDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(startLabel);
        return panel;
    }

    /**
     * Creates target word display panel.
     * @return Configured target display panel
     */
    private JPanel createTargetDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(targetLabel);
        return panel;
    }

    /**
     * Creates main character status display area.
     * @return Configured character display panel
     */
    private JPanel createCharacterDisplay() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Character Status"),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        middleWordsPanel = new JPanel();
        middleWordsPanel.setLayout(new BoxLayout(middleWordsPanel, BoxLayout.Y_AXIS));
        middleWordsPanel.setVisible(true);

        JPanel currentRow = createWordRow(characterLabels);
        JPanel targetRow = createWordRow(targetCharacterLabels);
        initTargetLabels();

        mainPanel.add(middleWordsPanel);
        mainPanel.add(currentRow);
        mainPanel.add(targetRow);

        return mainPanel;
    }

    /**
     * Creates a row of character display labels.
     * @param labels Array of labels to configure
     * @return Configured character row panel
     */
    private JPanel createWordRow(JLabel[] labels) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        for (int i = 0; i < 4; i++) {
            labels[i] = createCharacterLabel();
            panel.add(labels[i]);
        }
        return panel;
    }

    /**
     * Initializes target labels with correct position colors.
     */
    private void initTargetLabels() {
        for (JLabel label : targetCharacterLabels) {
            label.setBackground(colorMapper.getColor(Model.CharacterStatus.CORRECT_POSITION));
        }
    }

    /**
     * Creates input section with text field and buttons.
     * @return Configured input section panel
     */
    private JPanel createInputSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(createInputField(), BorderLayout.CENTER);
        panel.add(createControlButtons(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Creates input field component.
     * @return Configured input field panel
     */
    private JComponent createInputField() {
        JPanel panel = new JPanel();
        inputField.setPreferredSize(new Dimension(220, 35));
        panel.add(new JLabel("Enter Word:"));
        panel.add(inputField);
        return panel;
    }

    /**
     * Creates control button panel.
     * @return Configured button panel
     */
    private JPanel createControlButtons() {
        JPanel panel = new JPanel();
        panel.add(submitButton);
        panel.add(resetButton);
        panel.add(newGameButton);
        return panel;
    }

    /**
     * Creates virtual keyboard panel.
     * @return Configured keyboard panel
     */
    private JPanel createVirtualKeyboard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        String[][] keyboardLayout = {
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"z","x","c","v","b","n","m"},
                {"←", "Enter"}
        };

        for (String[] row : keyboardLayout) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            for (String key : row) {
                rowPanel.add(createKeyboardButton(key));
            }
            panel.add(rowPanel);
        }
        return panel;
    }

    /**
     * Creates individual keyboard button.
     * @param key Button character value
     * @return Configured keyboard button
     */
    private JButton createKeyboardButton(String key) {
        JButton button = new JButton(key.toUpperCase());
        button.setBackground(new Color(200, 200, 200));
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(
                key.equals("Enter") ? 100 : 50, 50
        ));
        button.addActionListener(e -> handleKeyPress(key));
        return button;
    }

    /**
     * Handles keyboard button press events.
     * @param key Pressed key value
     */
    private void handleKeyPress(String key) {
        switch (key) {
            case "←":
                deleteLastCharacter();
                break;
            case "Enter":
                submitButton.doClick();
                break;
            default:
                updateInputField(key);
        }
    }

    /**
     * Updates attempt history display.
     */
    private void updatePathDisplay() {
        middleWordsPanel.removeAll();

        if (model != null) {
            List<String> fullPath = model.getGamePath();
            int stepCount = fullPath.size();
            if (stepCount > 1) {
                List<String> displaySteps = fullPath.subList(0, stepCount - 1);
                for (String step : displaySteps) {
                    addStepToPathDisplay(step);
                }
            }
        }

        middleWordsPanel.revalidate();
        middleWordsPanel.repaint();
    }

    /**
     * Adds historical step to path display.
     * @param step Word attempt to display
     */
    private void addStepToPathDisplay(String step) {
        JPanel historyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        List<Model.CharacterStatus> statuses = model.getCharacterFeedback(step);
        for (int i = 0; i < 4; i++) {
            JLabel label = createCharacterLabel();
            label.setText(String.valueOf(step.charAt(i)).toUpperCase());
            label.setBackground(i < statuses.size() ?
                    colorMapper.getColor(statuses.get(i)) : Color.LIGHT_GRAY);
            historyRow.add(label);
        }
        middleWordsPanel.add(historyRow);
    }

    /**
     * Creates standardized character display label.
     * @return Configured character label
     */
    private JLabel createCharacterLabel() {
        JLabel label = new JLabel("_", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 80));
        label.setOpaque(true);
        label.setBackground(new Color(200, 200, 200));
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return label;
    }

    /**
     * Deletes last character from input field.
     */
    private void deleteLastCharacter() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            inputField.setText(text.substring(0, text.length() - 1));
        }
    }

    /**
     * Updates input field with new character.
     * @param key Character to add
     */
    private void updateInputField(String key) {
        if (inputField.getText().length() < 4) {
            inputField.setText(inputField.getText() + key);
        }
    }

    /**
     * Configures input field filtering for valid characters.
     */
    private void setupInputFiltering() {
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new InputFilter());
    }

    /**
     * Finalizes window setup and displays frame.
     */
    public void setupWindow() {
        if (!windowInitialized) {
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            windowInitialized = true;
        }
    }

    /**
     * Adds component to configuration panel.
     * @param comp Component to add
     */
    public void addConfigControl(Component comp) {
        configPanel.add(comp);
        configPanel.revalidate();
        configPanel.repaint();
    }

    /**
     * Adds spacing to configuration panel.
     * @param width Spacer width in pixels
     */
    public void addConfigSpacer(int width) {
        configPanel.add(Box.createHorizontalStrut(width));
    }

    /**
     * Retrieves current input field value.
     * @return User input text
     */
    public String getUserInput() {
        return inputField.getText();
    }

    /**
     * Clears input field content.
     */
    public void clearInputField() {
        inputField.setText("");
    }

    /**
     * Sets submit button action handler.
     * @param listener Action listener to handle submissions
     */
    public void setSubmitHandler(ActionListener listener) {
        submitButton.addActionListener(listener);
    }

    /**
     * Sets reset button action handler.
     * @param listener Action listener to handle resets
     */
    public void setResetHandler(ActionListener listener) {
        resetButton.addActionListener(listener);
    }

    /**
     * Enables/disables reset button.
     * @param enabled true to enable button, false to disable
     */
    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    /**
     * Input filter for validating text field entries.
     */
    private class InputFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attrs) throws BadLocationException {
            processInput(fb, offset, 0, str, attrs, true);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            processInput(fb, offset, length, str, attrs, false);
        }

        /**
         * Processes input with validation and filtering.
         */
        private void processInput(FilterBypass fb, int offset, int length, String str,
                                  AttributeSet attrs, boolean insert) throws BadLocationException {
            String filtered = str.replaceAll("[^a-zA-Z]", "");
            if (insert) {
                super.insertString(fb, offset, filtered, attrs);
            } else {
                super.replace(fb, offset, length, filtered, attrs);
            }
            enforceMaxLength();
        }

        /**
         * Enforces maximum input length of 4 characters.
         */
        private void enforceMaxLength() {
            SwingUtilities.invokeLater(() -> {
                String text = inputField.getText();
                if (text.length() > 4) {
                    inputField.setText(text.substring(0, 4));
                }
            });
        }
    }

    /**
     * Maps character statuses to display colors.
     */
    private static class ColorMapper {
        private static final Color CORRECT = new Color(99, 190, 123);
        private static final Color PRESENT = new Color(255, 212, 100);
        private static final Color ABSENT = new Color(200, 200, 200);

        /**
         * Gets color corresponding to character status.
         * @param status Character status enum
         * @return Associated display color
         */
        public Color getColor(Model.CharacterStatus status) {
            switch (status) {
                case CORRECT_POSITION: return CORRECT;
                case PRESENT_IN_WORD: return PRESENT;
                default: return ABSENT;
            }
        }
    }
}