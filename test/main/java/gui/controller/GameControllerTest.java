// GameControllerTest.java
package main.java.gui.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import gui.controller.GameController;
import gui.model.Model;
import gui.view.GameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GameControllerTest {

    @Mock private GameView mockView;
    @Mock private Model mockModel;
    @Captor private ArgumentCaptor<ActionListener> submitHandlerCaptor;
    @Captor private ArgumentCaptor<ActionListener> newGameHandlerCaptor;
    @Captor private ArgumentCaptor<ActionListener> resetHandlerCaptor;
    @Captor private ArgumentCaptor<String> stringCaptor;

    private GameController controller;
    private ActionEvent testEvent;

    @Before
    public void setUp() {
        configureModelBehavior();
        controller = new GameController(mockView, mockModel);
        testEvent = new ActionEvent(new JButton(), 0, "");

        // Capture event handlers
        verify(mockView).setSubmitHandler(submitHandlerCaptor.capture());
        verify(mockView).setNewGameHandler(newGameHandlerCaptor.capture());
        verify(mockView).setResetHandler(resetHandlerCaptor.capture());
    }

    // Configure model state linkages between methods
    private void configureModelBehavior() {
        // Link setUseRandomWords with isRandomWordsEnabled
        doAnswer((Answer<Void>) invocation -> {
            boolean enabled = invocation.getArgument(0);
            when(mockModel.isRandomWordsEnabled()).thenReturn(enabled);
            return null;
        }).when(mockModel).setUseRandomWords(anyBoolean());

        // Initialize default return value
        when(mockModel.isRandomWordsEnabled()).thenReturn(false);
    }

    @Test
    public void testConstructorInitialization() {
        verify(mockView).addConfigToggle(eq("Show Errors"), anyBoolean(), any());
        verify(mockView).addConfigToggle(eq("Show Solution Path"), anyBoolean(), any());
        verify(mockView).addConfigToggle(eq("Random Words"), anyBoolean(), any());
        verify(mockView).addConfigSpacer(20);
        verify(mockView).setupWindow();
    }

    @Test
    public void testValidGuessSubmission() {
        when(mockView.getUserInput()).thenReturn("test");
        when(mockModel.submitGuess("test")).thenReturn(true);

        submitHandlerCaptor.getValue().actionPerformed(testEvent);

        verify(mockView).clearInputField();
        verify(mockModel).submitGuess("test");
    }

    @Test
    public void testInvalidLengthInput() {
        when(mockView.getUserInput()).thenReturn("bad");

        submitHandlerCaptor.getValue().actionPerformed(testEvent);

        verify(mockView).showFeedbackDialog(
                eq("Invalid Input"),
                eq("Must be 4 characters"),
                eq(true)
        );
    }

    @Test
    public void testConfigToggleHandler() {
        // Get random words config toggle handler
        GameController.ConfigToggleHandler handler = getRandomWordsToggleHandler();

        // Activate configuration
        handler.toggle(true);

        // Verify state update and word generation
        verify(mockModel).setUseRandomWords(true);
        verify(mockModel).generateValidWordPair();
    }

    @Test
    public void testRefreshGameState() {
        // Trigger reset operation
        resetHandlerCaptor.getValue().actionPerformed(testEvent);

        verify(mockModel).resetGame();
        verify(mockView, atLeastOnce()).setStartWordDisplay(any());
        verify(mockView, atLeastOnce()).setTargetWordDisplay(any());
    }

    // Helper method: Get random words config toggle handler
    private GameController.ConfigToggleHandler getRandomWordsToggleHandler() {
        ArgumentCaptor<GameController.ConfigToggleHandler> toggleCaptor =
                ArgumentCaptor.forClass(GameController.ConfigToggleHandler.class);

        verify(mockView, times(3)).addConfigToggle(any(), anyBoolean(), toggleCaptor.capture());
        return toggleCaptor.getAllValues().get(2);
    }
}