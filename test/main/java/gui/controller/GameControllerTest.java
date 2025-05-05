package main.java.gui.controller;

import gui.controller.GameController;
import gui.model.Model;
import gui.view.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GameControllerTest {
    private GameController controller;
    private Model mockModel;
    private GameView mockView;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize mocks and inject dependencies
        mockModel = Mockito.mock(Model.class);
        mockView = Mockito.mock(GameView.class);

        // Create controller with mocked dependencies
        controller = new GameController(mockView, mockModel);
    }

    @Test
    void testInitialization() {
        // Verify view configuration during initialization
        verify(mockView, times(3)).addConfigToggle(
                anyString(),  // Setting name
                anyBoolean(), // Default state
                any()         // Change listener
        );
        verify(mockView).addConfigSpacer(20);  // Verify UI spacing
        verify(mockView).setupWindow();        // Verify window setup
    }

    @Test
    void testValidGuessSubmission() throws Exception {
        // Configure mock behavior
        when(mockView.getUserInput()).thenReturn("test");
        when(mockModel.submitGuess("test")).thenReturn(true);

        // Simulate guess submission action
        invokePrivateMethod("handleGuessSubmission", new ActionEvent(this, 0, "submit"));

        // Verify expected interactions
        verify(mockView).clearInputField();       // Input field cleared
        verify(mockModel).submitGuess("test");    // Guess processed
    }

    @Test
    void testInvalidInputHandling() throws Exception {
        // Test short input handling
        when(mockView.getUserInput()).thenReturn("abc");

        // Trigger submission handling
        invokePrivateMethod("handleGuessSubmission", new ActionEvent(this, 0, "submit"));

        // Verify error feedback
        verify(mockView).showFeedbackDialog(
                eq("Invalid Input"),   // Dialog title
                anyString(),          // Error message
                eq(true)              // Warning type
        );
        // Ensure no submission occurred
        verify(mockModel, never()).submitGuess(anyString());
    }

    @Test
    void testStateUpdateProcessing() throws Exception {
        // Configure mock responses
        when(mockModel.getCurrentWord()).thenReturn("test");
        when(mockModel.getTargetWord()).thenReturn("test");

        // Trigger state update
        invokePrivateMethod("handleStateUpdate");

        // Verify UI updates
        verify(mockView).updateCharacterStatus(
                anyList(),   // Expected character status list
                eq("test")   // Current word display
        );
        verify(mockView).setResetButtonEnabled(true);  // Reset button state
    }

    @Test
    void testErrorFeedbackSuppression() throws Exception {
        // Configure error display suppression
        when(mockModel.isErrorDisplayEnabled()).thenReturn(false);

        // Attempt to show feedback
        invokePrivateMethod("showInvalidAttemptFeedback");

        // Verify no UI interaction occurred
        verify(mockView, never()).showFeedbackDialog(
                anyString(),   // Title
                anyString(),   // Message
                anyBoolean()   // Type
        );
    }

    /**
     * Helper method for invoking private controller methods
     * @param methodName Name of the private method to test
     * @param args Arguments to pass to the method
     */
    private void invokePrivateMethod(String methodName, Object... args) throws Exception {
        // Prepare parameter types
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        // Access and execute private method
        Method method = GameController.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(controller, args);
    }
}