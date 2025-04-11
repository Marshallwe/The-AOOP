// GameView.java
package gui.view;

import gui.model.Model;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class GameView extends JFrame {
    private final JLabel[] characterLabels = new JLabel[4];
    private final JTextField inputField = new JTextField(4);
    private final JButton submitButton = new JButton("Submit");
    private final JButton resetButton = new JButton("Reset");
    private final JPanel configPanel = new JPanel();
    private final JLabel startLabel = new JLabel("Start:");
    private final JLabel targetLabel = new JLabel("Target:");
    private final JLabel pathLabel = new JLabel("Path:");
    private final ColorMapper colorMapper = new ColorMapper();

    public GameView() {
        initializeUIComponents();
    }
    public void setStartWordDisplay(String word) {
        startLabel.setText("Start: " + word.toUpperCase());
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
        JPanel panel = new JPanel(new GridLayout(3, 1)); // 改为3行布局
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
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(
                new InputFilter()
        );
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
        String word = currentWord.toLowerCase();
        for (int i = 0; i < 4; i++) {
            characterLabels[i].setText(String.valueOf(word.charAt(i)));
            characterLabels[i].setBackground(Color.WHITE);
        }
    }

    public void setupWindow() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void addConfigControl(Component comp) {
        configPanel.add(comp);
        configPanel.revalidate();
        configPanel.repaint();
    }

    public void addConfigSpacer(int width) {
        configPanel.add(Box.createHorizontalStrut(width));
    }

    public void clearInputField() {
        inputField.setText("");
    }

    public void setPathVisibility(boolean visible) {
        pathLabel.setVisible(visible);
    }

    public JTextField getInputField() { return inputField; }
    public JButton getSubmitButton() { return submitButton; }
    public JButton getResetButton() { return resetButton; }

    private class InputFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attrs)
                throws BadLocationException {
            processInput(fb, offset, 0, str, attrs, true);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs)
                throws BadLocationException {
            processInput(fb, offset, length, str, attrs, false);
        }

        private void processInput(FilterBypass fb, int offset, int length,
                                  String str, AttributeSet attrs, boolean insert)
                throws BadLocationException {
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
                case CORRECT_POSITION: return CORRECT;
                case PRESENT_IN_WORD: return PRESENT;
                default: return ABSENT;
            }
        }
    }
}