package gui.view;

import gui.model.WordLadderGame;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class GameView extends JFrame {
    private final JLabel[] letterLabels = new JLabel[4];
    private final JTextField inputField = new JTextField(4);
    private final JButton submitButton = new JButton("Submit");
    private final JButton resetButton = new JButton("Reset");
    private final JPanel keyboardPanel = new JPanel();
    private final JLabel targetWordLabel = new JLabel("Target word:");
    private final JLabel pathLabel = new JLabel("Convert path:");
    private final JPanel configControlsPanel = new JPanel();
    private final ColorMapper colorMapper = new ColorMapper();

    public GameView() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.EAST);
        add(createLetterPanel(), BorderLayout.NORTH);
        add(createVirtualKeyboard(), BorderLayout.WEST);
        add(createInputPanel(), BorderLayout.SOUTH);

        configureInputFilter();
        setupWindow();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetWordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        targetPanel.add(targetWordLabel);

        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        pathPanel.add(pathLabel);

        configControlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        panel.add(targetPanel);
        panel.add(pathPanel);
        panel.add(configControlsPanel);
        return panel;
    }

    private JPanel createLetterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Letter status"));
        for (int i = 0; i < 4; i++) {
            letterLabels[i] = createLetterLabel();
            panel.add(letterLabels[i]);
        }
        return panel;
    }

    private JLabel createLetterLabel() {
        JLabel label = new JLabel("_", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 80));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        return label;
    }

    private JPanel createVirtualKeyboard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        String[][] keyLayout = {
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"z","x","c","v","b","n","m"},
                {"←", "Enter"}
        };

        for (String[] row : keyLayout) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            for (String key : row) {
                rowPanel.add(createKeyButton(key));
            }
            panel.add(rowPanel);
        }
        return panel;
    }

    private JButton createKeyButton(String key) {
        JButton button = new JButton(key.toUpperCase());
        button.setPreferredSize(new Dimension(key.equals("Enter") ? 100 : 50, 50));
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
                if (inputField.getText().length() < 4) {
                    inputField.setText(inputField.getText() + key);
                }
        }
    }

    private void deleteLastCharacter() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            inputField.setText(text.substring(0, text.length() - 1));
        }
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel inputGroup = new JPanel();
        inputField.setPreferredSize(new Dimension(220, 35));
        inputGroup.add(new JLabel("Enter the word:"));
        inputGroup.add(inputField);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonGroup.add(submitButton);
        buttonGroup.add(resetButton);

        panel.add(inputGroup, BorderLayout.CENTER);
        panel.add(buttonGroup, BorderLayout.SOUTH);
        return panel;
    }

    private void configureInputFilter() {
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new LengthFilter());
    }

    private class LengthFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
            processText(fb, offset, 0, str, attr, true);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attr) throws BadLocationException {
            processText(fb, offset, length, str, attr, false);
        }

        private void processText(FilterBypass fb, int offset, int length,
                                 String str, AttributeSet attr, boolean insert) throws BadLocationException {
            String filtered = str.replaceAll("[^a-z]", "");
            if (insert) {
                super.insertString(fb, offset, filtered, attr);
            } else {
                super.replace(fb, offset, length, filtered, attr);
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

    public void setTargetWord(String targetWord) {
        targetWordLabel.setText("Target word:" + targetWord.toUpperCase());
    }

    public void setPathDisplay(String path) {
        pathLabel.setText("Convert path:" + path);
    }

    public void updateCharacterDisplay(List<WordLadderGame.CharacterStatus> statuses, String input) {
        for (int i = 0; i < 4; i++) {
            letterLabels[i].setBackground(colorMapper.getColorForStatus(statuses.get(i)));
            letterLabels[i].setText(i < input.length() ?
                    String.valueOf(input.charAt(i)).toUpperCase() : "_");
        }
    }

    public void clearInput() {
        inputField.setText("");
    }

    public void resetUI(String currentWord) {
        String word = currentWord.toLowerCase();
        for (int i = 0; i < 4; i++) {
            letterLabels[i].setText(String.valueOf(word.charAt(i)));
            letterLabels[i].setBackground(Color.WHITE);
        }
    }

    private static class ColorMapper {
        private static final Color CORRECT = new Color(99, 190, 123);
        private static final Color PRESENT = new Color(255, 212, 100);
        private static final Color ABSENT = new Color(120, 124, 126);

        public Color getColorForStatus(WordLadderGame.CharacterStatus status) {
            switch (status) {
                case CORRECT_POSITION:
                    return CORRECT;
                case PRESENT_IN_WORD:
                    return PRESENT;
                case NOT_PRESENT:
                default:
                    return ABSENT;
            }
        }
    }

    public JTextField getInputField() { return inputField; }
    public JButton getSubmitButton() { return submitButton; }
    public JButton getResetButton() { return resetButton; }
    public void addConfigControl(Component comp) {
        configControlsPanel.add(comp);
        configControlsPanel.revalidate();
        configControlsPanel.repaint();
    }
    public void setupWindow() {
        setPreferredSize(new Dimension(1000, 800));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}