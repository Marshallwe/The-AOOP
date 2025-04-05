// GameView.java
package gui;


import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class GameView extends JFrame {
    // ==== 字段声明 ====
    private final JLabel[] letterLabels = new JLabel[4];
    private final JTextField inputField = new JTextField(4);
    private final JButton submitButton = new JButton("Submit");
    private final JButton resetButton = new JButton("Reset");
    private final JPanel keyboardPanel = new JPanel();
    private final JLabel currentWordLabel = new JLabel("当前单词：");
    private final JLabel targetWordLabel = new JLabel("目标单词：");
    private final JLabel pathLabel = new JLabel("转换路径：");
    private final JPanel configControlsPanel = new JPanel();

    // ==== 构造函数 ====
    public GameView() {
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 顶部信息面板
        JPanel headerPanel = createHeaderPanel();

        // 字母显示面板
        JPanel lettersPanel = createLetterPanel();

        // 虚拟键盘面板
        createVirtualKeyboard();

        // 输入控制面板
        JPanel inputPanel = createInputPanel();

        // 主布局
        add(headerPanel, BorderLayout.EAST);
        add(lettersPanel, BorderLayout.NORTH);
        add(keyboardPanel, BorderLayout.WEST);
        add(inputPanel, BorderLayout.SOUTH);

        // 输入过滤配置
        configureInputFilter();

        setPreferredSize(new Dimension(1000, 800));
        pack();
        setLocationRelativeTo(null);
    }

    // ==== 头部面板 ====
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));


        // 目标单词
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetWordLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        targetWordLabel.setForeground(new Color(139, 0, 0));
        targetPanel.add(targetWordLabel);

        // 路径显示
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        pathPanel.add(pathLabel);

        // 配置控件容器
        configControlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        panel.add(targetPanel);
        panel.add(pathPanel);
        panel.add(configControlsPanel);

        return panel;
    }

    // ==== 配置控件方法 ====
    public void addConfigControl(JComponent component) {
        configControlsPanel.add(component);
        configControlsPanel.revalidate();
        configControlsPanel.repaint();
    }

    // ==== 字母显示面板 ====
    private JPanel createLetterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createTitledBorder("字母状态")
        ));
        for (int i = 0; i < 4; i++) {
            letterLabels[i] = new JLabel("_", SwingConstants.CENTER);
            letterLabels[i].setPreferredSize(new Dimension(80, 80));
            letterLabels[i].setOpaque(true);
            letterLabels[i].setBackground(Color.WHITE);
            letterLabels[i].setFont(new Font("Arial", Font.BOLD, 24));
            panel.add(letterLabels[i]);
        }
        return panel;
    }

    // ==== 虚拟键盘 ====
    private void createVirtualKeyboard() {
        keyboardPanel.setBackground(new Color(240, 240, 240));
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        String[][] keyLayout = {
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"z","x","c","v","b","n","m"},
                {"←", "Enter"}
        };

        for (String[] row : keyLayout) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            for (String key : row) {
                JButton btn = createKeyButton(key);
                rowPanel.add(btn);
            }
            keyboardPanel.add(rowPanel);
        }
    }

    private JButton createKeyButton(String key) {
        JButton button = new JButton(key.toUpperCase());
        button.setPreferredSize(new Dimension(
                key.equals("Enter") ? 100 : 50,
                50
        ));

        // 按钮样式
        button.setBackground(new Color(245, 245, 245));
        button.setForeground(Color.DARK_GRAY);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        button.setOpaque(true);
        button.setFocusPainted(false);

        button.addActionListener(e -> handleKeyPress(key));
        return button;
    }

    // ==== 按键处理 ====
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

    // ==== 输入面板 ====
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel inputGroup = new JPanel();
        inputGroup.add(new JLabel("输入单词:"));
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        inputField.setPreferredSize(new Dimension(220, 35));
        inputGroup.add(inputField);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        submitButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        resetButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        resetButton.setEnabled(false);
        buttonGroup.add(submitButton);
        buttonGroup.add(resetButton);

        panel.add(inputGroup, BorderLayout.CENTER);
        panel.add(buttonGroup, BorderLayout.SOUTH);
        return panel;
    }

    // ==== 输入过滤 ====
    private void configureInputFilter() {
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr)
                    throws BadLocationException {
                processInput(fb, offset, 0, str, attr, true);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attr)
                    throws BadLocationException {
                processInput(fb, offset, length, str, attr, false);
            }

            private void processInput(FilterBypass fb, int offset, int length,
                                      String str, AttributeSet attr, boolean insert)
                    throws BadLocationException {
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
        });
    }



    public void setTargetWord(String targetWord) {
        targetWordLabel.setText("目标单词：" + targetWord.toUpperCase());
    }

    public void setPathDisplay(String path) {
        pathLabel.setText("转换路径：" + path);
    }

    public void updateFeedback(List<WordLadderGame.CharacterFeedback> feedback) {
        String input = inputField.getText();
        for (int i = 0; i < 4; i++) {
            Color color = feedback.get(i).getColor();
            letterLabels[i].setBackground(color);
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

    // ==== 组件访问器 ====
    public JTextField getInputField() {
        return inputField;
    }

    public JButton getSubmitButton() {
        return submitButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }
}