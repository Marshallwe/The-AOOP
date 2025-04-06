package main.java.cli;
import cli.CLIGame;
import gui.model.GameConfig;
import gui.model.WordLadderGame;
import gui.model.WordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CLIGameTest {

    @Mock
    private WordValidator mockValidator;

    @Mock
    private GameConfig mockConfig;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testCreateConfigWithAllYes() {
        String input = "y\ny\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        GameConfig config = CLIGame.createConfig();

        assertAll("Configuration validation",
                () -> assertTrue(config.isShowErrorMessages()),
                () -> assertTrue(config.isDisplayPath()),
                () -> assertTrue(config.isUseRandomWords())
        );
    }

    @Test
    void testInitializeGameWithRandomWords() throws IOException {
        when(mockConfig.isUseRandomWords()).thenReturn(true);
        when(mockValidator.getRandomWordPair()).thenReturn(Arrays.asList("test", "tent"));

        WordLadderGame game = CLIGame.initializeGame(mockValidator, mockConfig);

        assertEquals("test", game.getCurrentWord());
        assertEquals("tent", game.getTargetWord());
    }

    @Test
    void testGameLoopWithValidAttempts() throws IOException {
        String input = "stat\nstot\nsoot\nmoot\nmoon\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        when(mockValidator.isValidWord(anyString())).thenReturn(true);
        when(mockConfig.isDisplayPath()).thenReturn(true);

        WordLadderGame game = new WordLadderGame("star", "moon", mockValidator, mockConfig);
        CLIGame.runGameLoop(game, mockConfig);

        String output = outContent.toString();
        assertTrue(output.contains("Congratulations!"));
        assertTrue(output.contains("Complete path: star → stat → stot → soot → moot → moon"));
    }

    @Test
    void testDisplayCurrentStateWithPath() {
        WordLadderGame game = mock(WordLadderGame.class);
        when(game.getCurrentWord()).thenReturn("test");
        when(game.getTransformationPath()).thenReturn(Arrays.asList("test", "text"));
        when(mockConfig.isDisplayPath()).thenReturn(true);

        CLIGame.displayCurrentState(game, mockConfig);

        assertTrue(outContent.toString().contains("test"));
        assertTrue(outContent.toString().contains("test → text"));
    }

    @Test
    void testProcessInputWithInvalidAttempt() {
        WordLadderGame game = mock(WordLadderGame.class);
        when(game.submitAttempt("test")).thenReturn(false);
        when(mockConfig.isShowErrorMessages()).thenReturn(true);

        CLIGame.processInput(game, "test", mockConfig);

        assertTrue(outContent.toString().contains("Invalid word! Reason:"));
    }

    @Test
    void testDisplayVictoryWithoutPath() {
        WordLadderGame game = mock(WordLadderGame.class);
        when(game.getAttempts()).thenReturn(Arrays.asList("step1", "step2"));
        when(mockConfig.isDisplayPath()).thenReturn(false);

        CLIGame.displayVictory(game, mockConfig);

        String output = outContent.toString();
        assertTrue(output.contains("2 step"));
        assertFalse(output.contains("Complete path:"));
    }
}