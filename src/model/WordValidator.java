package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WordValidator {
    private final Set<String> dictionary;

    public WordValidator(String dictPath) throws IOException {
        this.dictionary = loadDictionary(dictPath);
    }

    private Set<String> loadDictionary(String dictPath) throws IOException {
        Path path = Paths.get(dictPath);
        if (!Files.exists(path)) {
            throw new IOException("Dictionary file not found: " + dictPath);
        }

        return Files.lines(path)
                .map(String::trim)
                .filter(word -> word.length() == 4)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public boolean isValidWord(String word) {
        return word != null
                && word.length() == 4
                && dictionary.contains(word.toLowerCase());
    }
}