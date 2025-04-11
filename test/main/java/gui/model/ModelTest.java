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
        Files.write(Paths.get(testDictPath),
                Arrays.asList("star", "moon", "test", "bear", "tent", "boat"));
        model = new Model(testDictPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(testDictPath));
    }

    @Test
    void testGameInitialization_ValidWords() {
        model.initializeGame("star", "moon");

        assertEquals("star", model.getStartWord(), "initial word is not displayed correctly ");
        assertEquals("moon", model.getTargetWord(), "The target word is not displayed correctly ");
        assertEquals(0, model.getAttemptCount(), "The initial number of attempts should be 0");
    }

    @Test
    void testSubmitGuess_ValidAttempt() {
        model.initializeGame("test", "tent");

        assertTrue(model.submitGuess("tent"), "should accept a valid guess ");
        assertEquals(1, model.getAttemptCount(), "Attempts not updated ");
        assertTrue(model.getGamePath().get().contains("tent"), "path not complete ");
    }

    @Test
    void testSubmitGuess_InvalidAttempts() {
        model.initializeGame("test", "tent");

        assertFalse(model.submitGuess("test"), "same word should be rejected ");

        assertFalse(model.submitGuess("abcd"), "invalid dictionary words should be rejected ");

        assertFalse(model.submitGuess("bear"), "polygram changes should be rejected ");
    }



    @Test
    void testGameReset_StateRestoration() {
        model.initializeGame("star", "moon");
        model.submitGuess("stor");
        model.resetGame();

        assertEquals("star", model.getCurrentWord(), "Current word not restored after reset ");
        assertEquals(0, model.getAttemptCount(), "Attempts not reset ");
        assertEquals(1, model.getGamePath().get().size(), "Path not reset ");
    }


    @Test
    void testRandomWordGeneration_Validity() {
        String[] words = model.generateValidWordPair();

        assertTrue(isWordInDictionary(words[0]), "The generated word is not in the dictionary:" + words[0]);
        assertTrue(isWordInDictionary(words[1]), "Generated word not in dictionary:" + words[1]);
        assertEquals(4, words[0].length(), "generated word length error ");
        assertNotEquals(words[0], words[1], "Generate identical word pairs ");
    }

    private boolean isWordInDictionary(String word) {
        try {
            return Files.readAllLines(Paths.get(testDictPath))
                    .contains(word.toLowerCase());
        } catch (IOException e) {
            fail(" failed to read dictionary file ", e);
            return false;
        }
    }




}