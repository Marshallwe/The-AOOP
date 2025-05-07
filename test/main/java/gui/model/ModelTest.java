package main.java.gui.model;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import gui.model.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ModelTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Model model;
    private Path testDictPath;
    private Observer mockObserver;

    @Before
    public void setUp() throws IOException {
        // Create temporary dictionary file (Java 8 compatible)
        testDictPath = tempFolder.newFile().toPath();
        Files.write(testDictPath, Arrays.asList("star", "stay", "stir", "sear", "moon"));

        // Initialize model
        model = new Model(testDictPath.toString());

        // Configure mock observer
        mockObserver = mock(Observer.class);
        model.addObserver(mockObserver);
    }

    /* Constructor Tests */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullPath() throws IOException {
        new Model(null);
    }

    @Test(expected = IOException.class)
    public void testConstructorWithInvalidDictionary() throws IOException {
        new Model("invalid/path.txt");
    }

    /* Gameplay Tests */
    @Test
    public void testValidGuessSubmission() {
        model.initializeGame("star", "stir");
        assertTrue(model.submitGuess("stir"));
        assertEquals(1, model.getAttemptCount());
        assertNotification(Model.NotificationType.STATE_UPDATE);
    }


    /* Configuration Tests */
    @Test
    public void testConfigurationChanges() {
        model.setShowPathEnabled(true);
        assertTrue(model.isShowPathEnabled());
        assertNotification(Model.NotificationType.CONFIG_CHANGED);

        model.setErrorDisplayEnabled(false);
        assertFalse(model.isErrorDisplayEnabled());
    }

    /* Random Word Generation Tests */
    @Test
    public void testRandomWordGeneration() {
        String[] words = model.generateValidWordPair();
        assertEquals(2, words.length);
        assertNotEquals(words[0], words[1]);
        assertTrue(isValid(words[0])); // Using local validation method
        assertTrue(isValid(words[1]));
    }

    /* Helper Methods */
    private void assertNotification(Model.NotificationType type) {
        verify(mockObserver).update(any(), eq(type));
    }

    // Local dictionary validation method
    private boolean isValid(String word) {
        try {
            return word != null
                    && word.length() == 4
                    && Files.readAllLines(testDictPath).contains(word);
        } catch (IOException e) {
            return false;
        }
    }
}