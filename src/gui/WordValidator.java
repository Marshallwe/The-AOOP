// model/WordValidator.java
package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    public List<String> getRandomWordPair() throws IOException {
        if (dictionary.size() < 2) {
            throw new IOException("Dictionary contains fewer than 2 words");
        }

        List<String> wordList = new ArrayList<>(dictionary);
        Collections.shuffle(wordList);

        String first = wordList.get(0);
        String second;
        do {
            second = wordList.get(1);
        } while (first.equals(second));

        return Arrays.asList(first, second);
    }
}