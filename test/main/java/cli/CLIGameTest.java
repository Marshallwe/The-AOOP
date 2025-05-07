package main.java.cli;

import cli.CLIGame;
import gui.model.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CLIGameTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private InputStream originalIn;

    @Mock
    private Model mockModel;

    @BeforeEach
    void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));
        originalIn = System.in;
        injectMockModel();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        resetMockModel();
    }

    private void injectMockModel() throws Exception {
        Field modelField = CLIGame.class.getDeclaredField("model");
        modelField.setAccessible(true);
        modelField.set(null, mockModel);
    }

    private void resetMockModel() {
        try {
            Field modelField = CLIGame.class.getDeclaredField("model");
            modelField.setAccessible(true);
            modelField.set(null, null);
        } catch (Exception ignored) {}
    }

    private void provideInput(String input) {
        System.setIn(new ByteArrayInputStream((input + "\n").getBytes()));
    }

    @Test
    void processInput_ShouldHandleInvalidAttempts() throws Exception {
        // Configure test setup
        when(mockModel.isErrorDisplayEnabled()).thenReturn(true);
        when(mockModel.submitGuess("xxxx")).thenReturn(false);

        callPrivateMethod("processInput", new Class[]{String.class}, "xxxx");

        String output = outContent.toString();
        assertTrue(output.contains("1. Valid dictionary word"),
                "Should display validation rules when error display is enabled");
    }

    private void callPrivateMethod(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = CLIGame.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(null, args);
    }

    @Test
    void colorFeedback_ShouldGenerateCorrectOutput() throws Exception {
        // Create test feedback data
        List<Model.CharacterStatus> feedback = Arrays.asList(
                Model.CharacterStatus.CORRECT_POSITION,
                Model.CharacterStatus.PRESENT_IN_WORD,
                Model.CharacterStatus.NOT_PRESENT,
                Model.CharacterStatus.CORRECT_POSITION
        );

        // Invoke color feedback display
        callPrivateMethod("showColorFeedback", new Class[]{List.class}, feedback);

        // Verify ANSI color codes in output
        String output = outContent.toString();
        assertTrue(output.contains("\u001B[32m■"), "Missing green block");
        assertTrue(output.contains("\u001B[33m■"), "Missing yellow block");
        assertTrue(output.contains("\u001B[37m■"), "Missing white block");
    }
}