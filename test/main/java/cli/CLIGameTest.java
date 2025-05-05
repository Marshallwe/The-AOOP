package main.java.cli;

import cli.CLIGame;
import gui.model.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CLIGameTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Mock
    private Model mockModel;

    @BeforeEach
    void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));
        injectMockModel();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        resetSystemIn();
        clearMockModel();
    }

    private void injectMockModel() throws Exception {
        Field modelField = CLIGame.class.getDeclaredField("model");
        modelField.setAccessible(true);
        modelField.set(null, mockModel);
    }

    private void clearMockModel() {
        try {
            Field modelField = CLIGame.class.getDeclaredField("model");
            modelField.setAccessible(true);
            modelField.set(null, null);
        } catch (Exception ignored) {}
    }

    private void resetSystemIn() {
        System.setIn(System.in);
    }

    private void callPrivateMethod(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = CLIGame.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(null, args);
    }

    @Test
    void configureSettings_ShouldSetFlagsCorrectly() throws Exception {
        // Simulate user input: y for error display, n for path display, y for random words
        String input = "y\nn\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        callPrivateMethod("configureSettings", null);

        // Verify model flags are set correctly
        verify(mockModel).setErrorDisplayEnabled(true);
        verify(mockModel).setPathDisplayEnabled(false);
        verify(mockModel).setUseRandomWords(true);
    }

    @Test
    void initializeGame_WithRandomWordsEnabled() throws Exception {
        // Mock random word generation
        when(mockModel.isRandomWordsEnabled()).thenReturn(true);
        when(mockModel.generateValidWordPair()).thenReturn(new String[]{"test", "word"});

        callPrivateMethod("initializeGame", null);

        // Verify game initialization with generated words
        verify(mockModel).initializeGame("test", "word");
    }

    @Test
    void runGameLoop_ShouldCompleteSuccessfully() throws Exception {
        // Mock model behavior
        when(mockModel.getCurrentWord()).thenReturn("star", "stir", "ston", "moon");
        when(mockModel.getTargetWord()).thenReturn("moon");
        when(mockModel.submitGuess(anyString())).thenReturn(true);
        when(mockModel.getAttemptCount()).thenReturn(3);
        when(mockModel.isPathDisplayEnabled()).thenReturn(true);
        when(mockModel.getGamePath()).thenReturn(Optional.of(Arrays.asList("star", "stir", "ston", "moon")));

        // Simulate valid user inputs
        String input = "stir\nston\nmoon\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // Execute game loop
        callPrivateMethod("runGameLoop", null);

        // Verify output contains success messages
        String output = outContent.toString();
        assertTrue(output.contains("[ VICTORY ] Achieved in 3 steps"),
                "Victory message not displayed correctly");
        assertTrue(output.contains("Complete path: star → stir → ston → moon"),
                "Full path not displayed correctly");
    }

    @Test
    void processInput_WithInvalidAttemptAndErrorEnabled() throws Exception {
        // Configure error display and invalid submission
        when(mockModel.submitGuess("xxxx")).thenReturn(false);
        when(mockModel.isErrorDisplayEnabled()).thenReturn(true);

        // Process invalid input
        callPrivateMethod("processInput", new Class[]{String.class}, "xxxx");

        // Verify error messages contain all validation rules
        String output = outContent.toString();
        assertTrue(output.contains("1. Valid dictionary word"),
                "Dictionary word rule not displayed");
        assertTrue(output.contains("2. Exactly 1 letter changed"),
                "Single letter change rule not displayed");
        assertTrue(output.contains("3. Not a previous attempt"),
                "Previous attempt rule not displayed");
    }
}