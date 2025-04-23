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

public class GameView extends JFrame implements Observer {
    private final JLabel[] characterLabels = new JLabel[4];
    private final JTextField inputField = new JTextField(4);
    private final JButton submitButton = new JButton("Submit");
    private final JButton resetButton = new JButton("Reset");
    private final JPanel configPanel = new JPanel();
    private final JLabel startLabel = new JLabel("Start:");
    private final JLabel targetLabel = new JLabel("Target:");
    private final JLabel pathLabel = new JLabel("Path:");
    private final ColorMapper colorMapper = new ColorMapper();
    private boolean windowInitialized = false;
    private Model model;

    public GameView() {
        initializeUIComponents();
    }

    public void initializeWithModel(Model model) {
        this.model = model;
        resetUI(model.getCurrentWord());
        updatePersistentDisplays(model);
    }

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

    public void setSubmitHandler(ActionListener listener) {
        submitButton.addActionListener(listener);
    }

    public void setResetHandler(ActionListener listener) {
        resetButton.addActionListener(listener);
    }

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

    public void showFeedbackDialog(String title, String message, boolean isError) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void setStartWordDisplay(String word) {
        startLabel.setText("Start: " + word.toUpperCase());
    }

    public void setTargetWordDisplay(String word) {
        targetLabel.setText("Target: " + word.toUpperCase());
    }

    public void setTransformationPathDisplay(String path) {
        pathLabel.setText("Path: " + path);
    }

    public void updateCharacterStatus(List<Model.CharacterStatus> statuses, String word) {
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setBackground(colorMapper.getColor(statuses.get(i)));
            characterLabels[i].setText(i < word.length()
                    ? String.valueOf(word.charAt(i)).toUpperCase()
                    : "_");
        }
    }

    public void resetUI(String currentWord) {
        String word = currentWord.toUpperCase();
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setText(String.valueOf(word.charAt(i)));
            characterLabels[i].setBackground(Color.WHITE);
        }
        clearInputField();
    }

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

    private void initializeUIComponents() {
        configureWindowSettings();
        buildMainLayout();
        setupInputFiltering();
    }

    private void configureWindowSettings() {
        setTitle("Word Ladder Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(1200, 1000));
    }

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
        panel.add(createPathDisplay());
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

    private JPanel createPathDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(pathLabel);
        return panel;
    }

    private JPanel createCharacterDisplay() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Character Status"));
        for (int i = 0; i < 4; i++) {
            characterLabels[i] = createCharacterLabel();
            panel.add(characterLabels[i]);
        }
        return panel;
    }

    private JLabel createCharacterLabel() {
        JLabel label = new JLabel("_", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 80));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        return label;
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
        if (model != null && model.isPathDisplayEnabled()) {
            model.getGamePath().ifPresent(path ->
                    pathLabel.setText("Path: " + String.join(" → ", path))
            );
        } else {
            pathLabel.setText("Path: ");
        }
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

    public void setPathVisibility(boolean visible) {
        pathLabel.setVisible(visible);
    }

    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    private class InputFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attrs) throws BadLocationException {
            processInput(fb, offset, 0, str, attrs, true);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            processInput(fb, offset, length, str, attrs, false);
        }

        private void processInput(FilterBypass fb, int offset, int length, String str, AttributeSet attrs, boolean insert) throws BadLocationException {
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

    private static class ColorMapper {
        private static final Color CORRECT = new Color(99, 190, 123);
        private static final Color PRESENT = new Color(255, 212, 100);
        private static final Color ABSENT = new Color(120, 124, 126);

        public Color getColor(Model.CharacterStatus status) {
            switch (status) {
                case CORRECT_POSITION:
                    return CORRECT;
                case PRESENT_IN_WORD:
                    return PRESENT;
                default:
                    return ABSENT;
            }
        }
    }
}