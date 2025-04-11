package main.java.cli;

import cli.CLIGame;
import gui.model.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    // 反射工具方法
    private void callPrivateMethod(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = CLIGame.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(null, args);
    }



    @Test
    void configureSettings_ShouldSetFlagsCorrectly() throws Exception {
        String input = "y\nn\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        callPrivateMethod("configureSettings", null);

        verify(mockModel).setErrorDisplayEnabled(true);
        verify(mockModel).setPathDisplayEnabled(false);
        verify(mockModel).setUseRandomWords(true);
    }

    @Test
    void initializeGame_WithRandomWordsEnabled() throws Exception {
        when(mockModel.isRandomWordsEnabled()).thenReturn(true);
        when(mockModel.generateValidWordPair()).thenReturn(new String[]{"test", "word"});

        callPrivateMethod("initializeGame", null);

        verify(mockModel).initializeGame("test", "word");
    }

    @Test
    void runGameLoop_ShouldCompleteSuccessfully() throws Exception {
        when(mockModel.getCurrentWord()).thenReturn("star", "stir", "ston", "moon");
        when(mockModel.getTargetWord()).thenReturn("moon");
        when(mockModel.submitGuess(anyString())).thenReturn(true);
        when(mockModel.getAttemptCount()).thenReturn(3);
        when(mockModel.isPathDisplayEnabled()).thenReturn(true);
        when(mockModel.getGamePath()).thenReturn(Optional.of(Arrays.asList("star", "stir", "ston", "moon")));

        String input = "stir\nston\nmoon\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        callPrivateMethod("runGameLoop", null);

        String output = outContent.toString();
        assertTrue(output.contains("Congratulations! Steps: 3"));
        assertTrue(output.contains("Full path: star → stir → ston → moon"));
    }

    @Test
    void processInput_WithInvalidAttemptAndErrorEnabled() throws Exception {
        when(mockModel.submitGuess("xxxx")).thenReturn(false);
        when(mockModel.isErrorDisplayEnabled()).thenReturn(true);

        callPrivateMethod("processInput", new Class[]{String.class}, "xxxx");

        String output = outContent.toString();
        assertTrue(output.contains("1. 4-letter dictionary word"));
        assertTrue(output.contains("2. Exactly 1 letter changed"));
    }
}