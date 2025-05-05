package main.java.gui.model;

import gui.model.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    private Model model;
    private final String testDictPath = "test_dict.txt";

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary dictionary file with test words
        Files.write(Paths.get(testDictPath),
                Arrays.asList("star", "moon", "test", "bear", "tent", "boat"));
        // Initialize model with test dictionary
        model = new Model(testDictPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temporary file after each test
        Files.deleteIfExists(Paths.get(testDictPath));
    }

    @Test
    void testGameInitialization_ValidWords() {
        // Initialize game with known words
        model.initializeGame("star", "moon");

        // Verify initial game state
        assertEquals("star", model.getStartWord(),
                "Initial word should match provided start word");
        assertEquals("moon", model.getTargetWord(),
                "Target word should match provided end word");
        assertEquals(0, model.getAttemptCount(),
                "Attempt count should initialize to zero");
    }

    @Test
    void testSubmitGuess_ValidAttempt() {
        // Set up test game scenario
        model.initializeGame("test", "tent");

        // Submit valid guess
        boolean result = model.submitGuess("tent");

        // Verify successful submission
        assertTrue(result, "Valid guess should be accepted");
        assertEquals(1, model.getAttemptCount(),
                "Attempt count should increment on valid guess");
        assertTrue(model.getGamePath().get().contains("tent"),
                "Game path should include accepted guess");
    }

    @Test
    void testSubmitGuess_InvalidAttempts() {
        // Initialize test game
        model.initializeGame("test", "tent");

        // Test same-word submission
        assertFalse(model.submitGuess("test"),
                "Should reject duplicate start word");

        // Test invalid dictionary word
        assertFalse(model.submitGuess("abcd"),
                "Should reject non-dictionary words");

        // Test invalid multi-letter change
        assertFalse(model.submitGuess("bear"),
                "Should reject multi-letter changes");
    }

    @Test
    void testGameReset_StateRestoration() {
        // Set up and modify game state
        model.initializeGame("star", "moon");
        model.submitGuess("stor");

        // Reset game state
        model.resetGame();

        // Verify state restoration
        assertEquals("star", model.getCurrentWord(),
                "Current word should reset to initial value");
        assertEquals(0, model.getAttemptCount(),
                "Attempt count should reset to zero");
        assertEquals(1, model.getGamePath().get().size(),
                "Game path should reset to initial state");
    }

    @Test
    void testRandomWordGeneration_Validity() {
        // Generate random word pair
        String[] words = model.generateValidWordPair();

        // Validate word properties
        assertTrue(isWordInDictionary(words[0]),
                "Start word must exist in dictionary: " + words[0]);
        assertTrue(isWordInDictionary(words[1]),
                "Target word must exist in dictionary: " + words[1]);
        assertEquals(4, words[0].length(),
                "Generated words must be 4 characters long");
        assertNotEquals(words[0], words[1],
                "Start and target words must be different");
    }

    /**
     * Helper method to verify word existence in test dictionary
     * @param word Word to check
     * @return true if word exists in test dictionary
     */
    private boolean isWordInDictionary(String word) {
        try {
            return Files.readAllLines(Paths.get(testDictPath))
                    .contains(word.toLowerCase());
        } catch (IOException e) {
            fail("Dictionary file access failed", e);
            return false;
        }
    }
}