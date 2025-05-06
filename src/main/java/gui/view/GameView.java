// GameView.java
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
 * The main view component for the Word Ladder game GUI implementing the Observer pattern.
 * Manages user interface rendering, input handling, and model state visualization.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Displaying game state including start/target words and transformation path</li>
 *   <li>Providing interactive components for word input and game control</li>
 *   <li>Managing configuration toggles and visual feedback</li>
 *   <li>Coordinating with Model through Observer pattern updates</li>
 * </ul>
 */
public class GameView extends JFrame implements Observer {
    // UI Components
    private final JLabel[] characterLabels = new JLabel[4];
    private final JTextField inputField = new JTextField(4);
    private final JButton submitButton = new JButton("Submit");
    private final JButton resetButton = new JButton("Reset");
    private final JButton newGameButton = new JButton("New Game");
    private final JPanel configPanel = new JPanel();
    private final JLabel startLabel = new JLabel("Start:");
    private final JLabel targetLabel = new JLabel("Target:");
    private final JLabel pathLabel = new JLabel("Path:");
    private final ColorMapper colorMapper = new ColorMapper();
    private boolean windowInitialized = false;
    private Model model;

    /**
     * Constructs the game view and initializes UI components.
     * Sets up the main window layout and component hierarchy.
     */
    public GameView() {
        initializeUIComponents();
    }

    /**
     * Links the view to a game model and initializes display state.
     * @param model The game model to observe and visualize
     */
    public void initializeWithModel(Model model) {
        this.model = model;
        resetUI(model.getCurrentWord());
        updatePersistentDisplays(model);
    }

    /**
     * Observer pattern implementation for model updates.
     * @param o The observable object (Model instance)
     * @param arg Notification payload (NotificationType enum)
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
     * Sets the handler for word submission events.
     * @param listener ActionListener to handle submit button clicks
     */
    public void setSubmitHandler(ActionListener listener) {
        submitButton.addActionListener(listener);
    }

    /**
     * Sets the handler for game reset events.
     * @param listener ActionListener to handle reset button clicks
     */
    public void setResetHandler(ActionListener listener) {
        resetButton.addActionListener(listener);
    }

    /**
     * Handles different types of model notifications by updating specific UI components.
     * @param model The current game model
     * @param type The type of model notification received
     */
    private void handleModelNotification(Model model, Model.NotificationType type) {
        switch (type) {
            case STATE_UPDATE:
                updateCharacterStatus(
                        model.getCharacterFeedback(model.getCurrentWord()),
                        model.getCurrentWord()
                );
                updatePathDisplay();
                break;
            case CONFIG_CHANGED:
                setPathVisibility(model.isPathDisplayEnabled());
                updatePathDisplay();
                break;
            case GAME_RESET:
                resetUI(model.getCurrentWord());
                updatePathDisplay();
                break;
            case GAME_WON:
                showGameResultDialog(model.getAttemptCount());
                break;
            case ERROR:
                showFeedbackDialog("Error", model.getLastErrorMessage(), true);
                break;
        }
    }

    /**
     * Displays victory dialog with game completion options.
     * @param attemptCount Number of steps taken to win
     * @return User's choice (0 = new game, 1 = exit)
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
     * Displays a modal feedback/error dialog.
     * @param title Dialog title
     * @param message Content message
     * @param isError Whether to use error styling (true) or info styling (false)
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
     * Updates the display of the starting word.
     * @param word Current starting word in uppercase
     */
    public void setStartWordDisplay(String word) {
        startLabel.setText("Start: " + word.toUpperCase());
    }

    /**
     * Updates the display of the target word.
     * @param word Current target word in uppercase
     */
    public void setTargetWordDisplay(String word) {
        targetLabel.setText("Target: " + word.toUpperCase());
    }

    /**
     * Updates the transformation path display.
     * @param path Formatted string of transformation steps
     */
    public void setTransformationPathDisplay(String path) {
        pathLabel.setText("Path: " + path);
    }

    /**
     * Updates character status indicators with color coding.
     * @param statuses List of character evaluation states
     * @param word Current word being displayed
     */
    public void updateCharacterStatus(List<Model.CharacterStatus> statuses, String word) {
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setBackground(colorMapper.getColor(statuses.get(i)));
            characterLabels[i].setText(i < word.length()
                    ? String.valueOf(word.charAt(i)).toUpperCase()
                    : "_");
        }
    }

    /**
     * Resets UI elements to initial state.
     * @param currentWord Word to display in character slots
     */
    public void resetUI(String currentWord) {
        String word = currentWord.toUpperCase();
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setText(String.valueOf(word.charAt(i)));
            characterLabels[i].setBackground(Color.WHITE);
        }
        clearInputField();
    }

    /**
     * Sets handler for new game requests.
     * @param listener ActionListener for new game button
     */
    public void setNewGameHandler(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }

    /**
     * Adds a configuration toggle to the settings panel.
     * @param label Toggle display text
     * @param initialState Initial toggle state
     * @param handler Configuration change handler
     */
    public void addConfigToggle(String label, boolean initialState,
                                GameController.ConfigToggleHandler handler) {
        JCheckBox toggle = new JCheckBox(label, initialState);
        toggle.addActionListener(e -> handler.toggle(toggle.isSelected()));
        addConfigControl(toggle);
    }

    /**
     * Updates persistent game state displays.
     * @param model Current game model
     */
    private void updatePersistentDisplays(Model model) {
        setStartWordDisplay(model.getStartWord());
        setTargetWordDisplay(model.getTargetWord());
        setResetButtonEnabled(model.getAttemptCount() > 0);
    }

    /* UI Construction Methods */

    /**
     * Initializes core UI components and layout.
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
     * Constructs primary UI layout sections.
     */
    private void buildMainLayout() {
        add(createHeaderSection(), BorderLayout.NORTH);
        add(createCharacterDisplay(), BorderLayout.CENTER);
        add(createInputSection(), BorderLayout.SOUTH);
        add(createVirtualKeyboard(), BorderLayout.WEST);
        add(createConfigPanel(), BorderLayout.EAST);
    }

    /**
     * Creates settings configuration panel.
     * @return Configured settings panel
     */
    private JPanel createConfigPanel() {
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        return configPanel;
    }

    /**
     * Creates header section with game state displays.
     * @return Header panel containing start/target/path labels
     */
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(createStartDisplay());
        panel.add(createTargetDisplay());
        panel.add(createPathDisplay());
        return panel;
    }

    /**
     * Creates start word display component.
     * @return Configured start word panel
     */
    private JPanel createStartDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(startLabel);
        return panel;
    }

    /**
     * Creates target word display component.
     * @return Configured target word panel
     */
    private JPanel createTargetDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(targetLabel);
        return panel;
    }

    /**
     * Creates transformation path display component.
     * @return Configured path display panel
     */
    private JPanel createPathDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(pathLabel);
        return panel;
    }

    /**
     * Creates main character status display area.
     * @return Panel with 4 character slots
     */
    private JPanel createCharacterDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Character Status"));
        for (int i = 0; i < 4; i++) {
            characterLabels[i] = createCharacterLabel();
            panel.add(characterLabels[i]);
        }
        return panel;
    }

    /**
     * Creates standardized character display label.
     * @return Configured character label
     */
    private JLabel createCharacterLabel() {
        JLabel label = new JLabel("_", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 80));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        return label;
    }

    /**
     * Creates input section with text field and control buttons.
     * @return Configured input section panel
     */
    private JPanel createInputSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(createInputField(), BorderLayout.CENTER);
        panel.add(createControlButtons(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Creates text input field component.
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
     * Creates control button row.
     * @return Panel containing action buttons
     */
    private JPanel createControlButtons() {
        JPanel panel = new JPanel();
        panel.add(submitButton);
        panel.add(resetButton);
        panel.add(newGameButton);
        return panel;
    }

    /**
     * Creates virtual keyboard layout.
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
     * Creates standardized keyboard button.
     * @param key Button label/text
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
     * Handles virtual keyboard input events.
     * @param key Pressed key identifier
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
     * Updates transformation path display based on model state.
     */
    private void updatePathDisplay() {
        if (model != null) {
            if (model.isPathDisplayEnabled()) {
                model.getGamePath().ifPresent(path -> {
                    String pathText = String.join(" → ", path);
                    pathLabel.setText("Path: " + pathText);
                });
            } else {
                pathLabel.setText("Path: ");
            }
        }
        pathLabel.revalidate();
        pathLabel.repaint();
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
     * Appends character to input field if within limit.
     * @param key Character to append
     */
    private void updateInputField(String key) {
        if (inputField.getText().length() < 4) {
            inputField.setText(inputField.getText() + key);
        }
    }

    /**
     * Applies input filtering to text field.
     */
    private void setupInputFiltering() {
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new InputFilter());
    }

    /**
     * Finalizes window initialization and displays UI.
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
     * Retrieves current input field text.
     * @return User input string
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
     * Controls visibility of transformation path display.
     * @param visible Whether to show path
     */
    public void setPathVisibility(boolean visible) {
        pathLabel.setVisible(visible);
    }

    /**
     * Enables/disables reset button.
     * @param enabled Whether button should be active
     */
    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    /**
     * Document filter enforcing letter-only input and 4-character limit.
     */
    private class InputFilter extends DocumentFilter {
        /**
         * Filters inserted text to letters only.
         */
        @Override
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attrs) throws BadLocationException {
            processInput(fb, offset, 0, str, attrs, true);
        }

        /**
         * Filters replaced text to letters only.
         */
        @Override
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            processInput(fb, offset, length, str, attrs, false);
        }

        /**
         * Common processing for text input operations.
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
         * Enforces 4-character maximum input length.
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
        private static final Color ABSENT = new Color(120, 124, 126);

        /**
         * Gets color for character evaluation status.
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