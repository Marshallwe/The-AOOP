package main.java.gui.model;

import gui.model.GameConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameConfigTest {

    @Test
    void defaultConstructorShouldSetCorrectValues() {
        GameConfig config = new GameConfig();

        assertAll("The default constructor initializes state",
                () -> assertTrue(config.isShowErrorMessages(), "By default, an error message should be displayed"),
                () -> assertFalse(config.isDisplayPath(), "By default, the path should not be displayed"),
                () -> assertFalse(config.isUseRandomWords(), "Random words should not be used by default")
        );
    }

    @Test
    void parameterizedConstructorShouldSetAllValues() {
        GameConfig config = new GameConfig(
                false,
                true,
                true
        );

        assertAll("Full argument constructor initialization",
                () -> assertFalse(config.isShowErrorMessages(), "The error shows a setting error"),
                () -> assertTrue(config.isDisplayPath(), "The path display is set incorrectly"),
                () -> assertTrue(config.isUseRandomWords(), "Random word set wrong")
        );
    }

    @Test
    void settersShouldUpdateStateCorrectly() {
        GameConfig config = new GameConfig();

        config.setShowErrorMessages(false);
        assertFalse(config.isShowErrorMessages(), "The error message indicates that the status update failed");

        config.setDisplayPath(true);
        assertTrue(config.isDisplayPath(), "The path shows that the state update failed");

        config.setUseRandomWords(true);
        assertTrue(config.isUseRandomWords(), "The random word use status update failed");
    }

    @Test
    void shouldAllowMultipleStateChanges() {
        GameConfig config = new GameConfig();

        config.setShowErrorMessages(false);
        config.setDisplayPath(true);
        config.setUseRandomWords(true);
        assertAll("First state change verification",
                () -> assertFalse(config.isShowErrorMessages()),
                () -> assertTrue(config.isDisplayPath()),
                () -> assertTrue(config.isUseRandomWords())
        );
        config.setShowErrorMessages(true);
        config.setDisplayPath(false);
        config.setUseRandomWords(false);
        assertAll("Secondary state modification verification",
                () -> assertTrue(config.isShowErrorMessages()),
                () -> assertFalse(config.isDisplayPath()),
                () -> assertFalse(config.isUseRandomWords())
        );
    }

    @Test
    void shouldHandleBoundaryConditions() {
        GameConfig trueConfig = new GameConfig(true, true, true);
        assertAll("All true state",
                () -> assertTrue(trueConfig.isShowErrorMessages()),
                () -> assertTrue(trueConfig.isDisplayPath()),
                () -> assertTrue(trueConfig.isUseRandomWords())
        );

        GameConfig falseConfig = new GameConfig(false, false, false);
        assertAll("All false state",
                () -> assertFalse(falseConfig.isShowErrorMessages()),
                () -> assertFalse(falseConfig.isDisplayPath()),
                () -> assertFalse(falseConfig.isUseRandomWords())
        );
    }
}