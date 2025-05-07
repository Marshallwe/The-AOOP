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
 * The primary view component for the Word Ladder game interface.
 * Handles UI presentation, user input processing, and model state visualization.
 * Implements Observer pattern to receive updates from the game model.
 *
 */

public class GameView extends JFrame implements Observer {
    // Region: UI Component Declarations

    /** Display labels for current word characters (4-letter positions) */
    private final JLabel[] characterLabels = new JLabel[4];
    /** Display labels for target word characters (4-letter positions) */
    private final JLabel[] targetCharacterLabels = new JLabel[4];
    /** Panel containing transformation path visualization */
    private JPanel middleWordsPanel;
    /** Toggle flag for path visualization visibility */
    private boolean showPath = false;

    /** Text input field for word entry */
    private final JTextField inputField = new JTextField(4);
    /** Action buttons for game control */
    private final JButton submitButton = new JButton("Submit");
    private final JButton resetButton = new JButton("Reset");
    private final JButton newGameButton = new JButton("New Game");
    private final JPanel configPanel = new JPanel();
    private final JLabel startLabel = new JLabel("Start:");
    private final JLabel targetLabel = new JLabel("Target:");

    private final ColorMapper colorMapper = new ColorMapper();
    private boolean windowInitialized = false;
    private Model model;
    /**
     * Constructs game view and initializes UI components.
     */
    public GameView() {
        initializeUIComponents();
    }
    /**
     * Links view to game model and initializes display state.
     * @param model The game model to observe and visualize
     */

    public void initializeWithModel(Model model) {
        this.model = model;
        this.showPath = model.isPathDisplayEnabled();
        resetUI(model.getCurrentWord());
        updatePersistentDisplays(model);
        updatePathDisplay();
    }
    /**
     * Receives model state updates and triggers UI refresh.
     * @param o Observable object (game model)
     * @param arg Notification type or data payload
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Model) {
            Model model = (Model) o;
            // Ensure UI updates happen on EDT
            SwingUtilities.invokeLater(() -> {
                if (arg instanceof Model.NotificationType) {
                    handleModelNotification(model, (Model.NotificationType) arg);
                }
                updatePersistentDisplays(model);
            });
        }
    }
    /**
     * Sets submit button action handler.
     * @param listener ActionListener to handle submit events
     */
    public void setSubmitHandler(ActionListener listener) {
        submitButton.addActionListener(listener);
    }
    /**
     * Sets reset button action handler.
     * @param listener ActionListener to handle reset events
     */

    public void setResetHandler(ActionListener listener) {
        resetButton.addActionListener(listener);
    }
    /**
     * Processes model notifications and updates UI accordingly.
     * @param model Current game model instance
     * @param type Type of model notification
     */

    private void handleModelNotification(Model model, Model.NotificationType type) {
        switch (type) {
            case CONFIG_CHANGED:
                // Update path display visibility
                boolean newState = model.isPathDisplayEnabled();
                if (this.showPath != newState) {
                    this.showPath = newState;
                    updatePathDisplay();
                }
                break;
            case STATE_UPDATE:
                // Refresh character status and path
                updateCharacterStatus(
                        model.getCharacterFeedback(model.getCurrentWord()),
                        model.getCurrentWord()
                );
                updatePathDisplay();
                break;
            case GAME_RESET:
                // Reset UI to initial state
                resetUI(model.getCurrentWord());
                updatePathDisplay();
                break;
            case GAME_WON:
                // Show victory dialog
                showGameResultDialog(model.getAttemptCount());
                break;
            case ERROR:
                // Display error message
                showFeedbackDialog("Error", model.getLastErrorMessage(), true);
                break;
        }
    }

    /**
     * Displays game completion dialog with restart options.
     * @param attemptCount Number of attempts taken to win
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
     * Displays feedback/error message dialog.
     * @param title Dialog window title
     * @param message Information to display
     * @param isError True for error styling, false for info
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
     * @param word Current start word
     */
    public void setStartWordDisplay(String word) {
        startLabel.setText("Start: " + word.toUpperCase());
    }
    /**
     * Updates target word display with colored indicators.
     * @param word Current target word
     */
    public void setTargetWordDisplay(String word) {
        targetLabel.setText("Target: " + word.toUpperCase());
        String targetWord = word.toUpperCase();
        for (int i = 0; i < 4; i++) {
            targetCharacterLabels[i].setText(String.valueOf(targetWord.charAt(i)));
            // Target word always shows correct position styling
            targetCharacterLabels[i].setBackground(colorMapper.getColor(Model.CharacterStatus.CORRECT_POSITION));
        }
    }


    /**
     * Updates character status indicators with feedback colors.
     * @param statuses List of character evaluation results
     * @param word Current word being displayed
     */
    public void updateCharacterStatus(List<Model.CharacterStatus> statuses, String word) {
        for (int i = 0; i < 4; i++) {
            // Set background color based on evaluation status
            if (statuses != null && i < statuses.size()) {
                characterLabels[i].setBackground(colorMapper.getColor(statuses.get(i)));
            } else {
                characterLabels[i].setBackground(new Color(200, 200, 200));
            }
            // Display character or placeholder
            characterLabels[i].setText(i < word.length()
                    ? String.valueOf(word.charAt(i)).toUpperCase()
                    : "_");
        }
    }

    /**
     * Resets UI components to initial state.
     * @param currentWord Starting word for reset
     */
    public void resetUI(String currentWord) {
        String word = currentWord.toUpperCase();
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setText(String.valueOf(word.charAt(i)));
            characterLabels[i].setBackground(new Color(200, 200, 200));
        }
        if (model != null) {
            setTargetWordDisplay(model.getTargetWord());
        }
        clearInputField();
    }
    /**
     * Configures new game button handler.
     * @param listener ActionListener for new game events
     */
    public void setNewGameHandler(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }
    /**
     * Adds configurable toggle to settings panel.
     * @param label Toggle display text
     * @param initialState Default toggle state
     * @param handler Configuration change handler
     */

    public void addConfigToggle(String label, boolean initialState,
                                GameController.ConfigToggleHandler handler) {
        JCheckBox toggle = new JCheckBox(label, initialState);
        toggle.addActionListener(e -> handler.toggle(toggle.isSelected()));
        addConfigControl(toggle);
    }

    private void updatePersistentDisplays(Model model) {
        setStartWordDisplay(model.getStartWord());
        setTargetWordDisplay(model.getTargetWord());
        setResetButtonEnabled(model.getAttemptCount() > 0);
    }

    /**
     * Initializes and arranges all UI components.
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
     * Constructs primary application layout.
     */
    private void buildMainLayout() {
        add(createHeaderSection(), BorderLayout.NORTH);
        add(createCharacterDisplay(), BorderLayout.CENTER);
        add(createInputSection(), BorderLayout.SOUTH);
        add(createVirtualKeyboard(), BorderLayout.WEST);
        add(createConfigPanel(), BorderLayout.EAST);
    }

    private JPanel createConfigPanel() {
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        return configPanel;
    }


    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(createStartDisplay());
        panel.add(createTargetDisplay());
        return panel;
    }

    private JPanel createStartDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(startLabel);
        return panel;
    }

    private JPanel createTargetDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(targetLabel);
        return panel;
    }




    private JPanel createCharacterDisplay() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Character Status"),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        middleWordsPanel = new JPanel();
        middleWordsPanel.setLayout(new BoxLayout(middleWordsPanel, BoxLayout.Y_AXIS));
        middleWordsPanel.setVisible(showPath);

        JPanel currentRow = createWordRow(characterLabels);

        JPanel targetRow = createWordRow(targetCharacterLabels);
        initTargetLabels();

        mainPanel.add(middleWordsPanel);
        mainPanel.add(currentRow);
        mainPanel.add(targetRow);

        return mainPanel;
    }
    private JPanel createWordRow(JLabel[] labels) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        for (int i = 0; i < 4; i++) {
            labels[i] = createCharacterLabel();
            panel.add(labels[i]);
        }
        return panel;
    }
    private void initTargetLabels() {
        for (JLabel label : targetCharacterLabels) {
            label.setBackground(colorMapper.getColor(Model.CharacterStatus.CORRECT_POSITION));
        }
    }


    private JPanel createInputSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(createInputField(), BorderLayout.CENTER);
        panel.add(createControlButtons(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createInputField() {
        JPanel panel = new JPanel();
        inputField.setPreferredSize(new Dimension(220, 35));
        panel.add(new JLabel("Enter Word:"));
        panel.add(inputField);
        return panel;
    }


    private JPanel createControlButtons() {
        JPanel panel = new JPanel();
        panel.add(submitButton);
        panel.add(resetButton);
        panel.add(newGameButton);
        return panel;
    }


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

    private void updatePathDisplay() {
        middleWordsPanel.removeAll();

        if (showPath && model != null) {
            model.getGamePath().ifPresent(fullPath -> {
                List<String> displaySteps = fullPath.subList(0, fullPath.size() - 1);

                displaySteps.forEach(step -> {
                    JPanel historyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

                    List<Model.CharacterStatus> statuses = model.getCharacterFeedback(step);

                    for (int i = 0; i < 4; i++) {
                        JLabel label = createCharacterLabel();
                        label.setText(String.valueOf(step.charAt(i)).toUpperCase());

                        if (i < statuses.size()) {
                            label.setBackground(colorMapper.getColor(statuses.get(i)));
                        } else {
                            label.setBackground(Color.LIGHT_GRAY);
                        }

                        historyRow.add(label);
                    }
                    middleWordsPanel.add(historyRow);
                });
            });
        }
        middleWordsPanel.setVisible(showPath);
        middleWordsPanel.revalidate();
        middleWordsPanel.repaint();
    }
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


    private void deleteLastCharacter() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            inputField.setText(text.substring(0, text.length() - 1));
        }
    }


    private void updateInputField(String key) {
        if (inputField.getText().length() < 4) {
            inputField.setText(inputField.getText() + key);
        }
    }

    private void setupInputFiltering() {
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new InputFilter());
    }


    public void setupWindow() {
        if (!windowInitialized) {
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            windowInitialized = true;
        }
    }


    public void addConfigControl(Component comp) {
        configPanel.add(comp);
        configPanel.revalidate();
        configPanel.repaint();
    }


    public void addConfigSpacer(int width) {
        configPanel.add(Box.createHorizontalStrut(width));
    }


    public String getUserInput() {
        return inputField.getText();
    }


    public void clearInputField() {
        inputField.setText("");
    }





    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    /**
     * Custom document filter for input validation.
     * Restricts input to letters only and enforces 4-character maximum.
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
     * Maps character status values to display colors.
     */
    private static class ColorMapper {
        private static final Color CORRECT = new Color(99, 190, 123);
        private static final Color PRESENT = new Color(255, 212, 100);
        private static final Color ABSENT = new Color(200, 200, 200);
        /**
         * Retrieves color for character status.
         * @param status Character evaluation result
         * @return Corresponding display color
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