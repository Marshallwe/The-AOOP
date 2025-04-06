package main.java.gui.model;

import gui.model.GameConfig;
import gui.model.WordLadderGame;
import gui.model.WordValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WordLadderGameTest {

    @Mock private WordValidator mockValidator;
    @Mock private GameConfig mockConfig;

    private WordLadderGame game;
    private final String START = "star";
    private final String TARGET = "moon";
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(mockValidator.isValidWord(anyString())).thenReturn(true);
        game = new WordLadderGame(START, TARGET, mockValidator, mockConfig);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldManageTransformationPath() {

        when(mockConfig.isDisplayPath()).thenReturn(true);

        game.submitAttempt("stat");
        game.submitAttempt("stot");
        game.submitAttempt("soot");
        game.submitAttempt("moot");
        game.submitAttempt("moon");

        assertAll("Path management verification",
                () -> assertEquals(6, game.getTransformationPath().size(), "The transition path length is incorrect"),
                () -> assertEquals(Arrays.asList(START, "stat", "stot", "soot", "moot", TARGET),
                        game.getTransformationPath(),
                        "Conversion path content does not match")
        );
    }

}