package main.java.gui.model;

import gui.model.WordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class WordValidatorTest {

    @TempDir
    static Path tempDir;

    private Path createDictFile(String content) throws IOException {
        Path dictFile = tempDir.resolve("test_dict.txt");
        Files.write(dictFile, content.getBytes(StandardCharsets.UTF_8));
        return dictFile;
    }

    @Test
    void shouldLoadValidDictionary() throws Exception {
        String dictContent = "test\nword\ndata\njava";
        Path dictPath = createDictFile(dictContent);
        WordValidator validator = new WordValidator(dictPath.toString());

        assertAll(
                () -> assertTrue(validator.isValidWord("TEST")),
                () -> assertTrue(validator.isValidWord("java")),
                () -> assertFalse(validator.isValidWord("abcd"))
        );
    }



    @Test
    void shouldHandleSpecialCharacters() throws Exception {
        String dictContent = "café\nnaïv";
        Path dictPath = createDictFile(dictContent);
        WordValidator validator = new WordValidator(dictPath.toString());
        assertAll(
                () -> assertTrue(validator.isValidWord("café")),
                () -> assertTrue(validator.isValidWord("NAÏV"))
        );
    }



    @Test
    void shouldGenerateUniquePairs() throws Exception {
        String dictContent = "test\nword\ndata\njava";
        Path dictPath = createDictFile(dictContent);
        WordValidator validator = new WordValidator(dictPath.toString());

        for (int i = 0; i < 20; i++) {
            List<String> pair = validator.getRandomWordPair();
            assertNotEquals(pair.get(0), pair.get(1));
        }
    }
}