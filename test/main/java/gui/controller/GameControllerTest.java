package main.java.gui.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import gui.controller.GameController;
import gui.model.*;
import gui.view.GameView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class GameControllerTest {

    @Mock private GameView mockView;
    @Mock private WordLadderGame mockGame;
    @Mock private GameConfig mockConfig;
    @Mock private JButton mockSubmitButton;
    @Mock private JButton mockResetButton;
    @Mock private JTextField mockInputField;

    @Captor private ArgumentCaptor<ActionListener> submitCaptor;
    @Captor private ArgumentCaptor<ActionListener> resetCaptor;

    private GameController controller;

    @Before
    public void setUp() {
        // 基础桩配置
        when(mockView.getSubmitButton()).thenReturn(mockSubmitButton);
        when(mockView.getResetButton()).thenReturn(mockResetButton);
        when(mockView.getInputField()).thenReturn(mockInputField);
        when(mockGame.getCurrentWord()).thenReturn("test");
        when(mockGame.getTargetWord()).thenReturn("goal");

        controller = new GameController(mockView, mockGame, mockConfig);

        verify(mockSubmitButton).addActionListener(submitCaptor.capture());
        verify(mockResetButton).addActionListener(resetCaptor.capture());
    }

    @Test
    public void testValidSubmission() {
        when(mockInputField.getText()).thenReturn("text");
        when(mockGame.submitAttempt("text")).thenReturn(true);
        when(mockGame.getCharacterStatus("text")).thenReturn(Arrays.asList(
                WordLadderGame.CharacterStatus.CORRECT_POSITION,
                WordLadderGame.CharacterStatus.PRESENT_IN_WORD,
                WordLadderGame.CharacterStatus.NOT_PRESENT,
                WordLadderGame.CharacterStatus.CORRECT_POSITION
        ));

        submitCaptor.getValue().actionPerformed(
                new ActionEvent(mockSubmitButton, ActionEvent.ACTION_PERFORMED, "")
        );

        verify(mockView).updateCharacterDisplay(anyList(), eq("text"));
        verify(mockView).clearInput();
        verify(mockResetButton).setEnabled(true);
    }

    @Test
    public void testPathDisplay() {
        when(mockConfig.isDisplayPath()).thenReturn(true);
        when(mockGame.getTransformationPath())
                .thenReturn(Arrays.asList("test", "rest", "goal"));

        controller.updatePathDisplay();

        verify(mockView).setPathDisplay("test → rest → goal");
    }

}