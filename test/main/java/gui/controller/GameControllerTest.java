package main.java.gui.controller;

import gui.controller.GameController;
import gui.model.Model;
import gui.view.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GameControllerTest {
    private GameController controller;
    private Model mockModel;
    private GameView mockView;

    @BeforeEach
    void setUp() throws Exception {
        mockModel = Mockito.mock(Model.class);
        mockView = Mockito.mock(GameView.class);

        controller = new GameController(mockView, mockModel);
    }

    @Test
    void testInitialization() {
        verify(mockView, times(3)).addConfigToggle(anyString(), anyBoolean(), any());
        verify(mockView).addConfigSpacer(20);
        verify(mockView).setupWindow();
    }

    @Test
    void testValidGuessSubmission() throws Exception {
        // 设置模拟行为
        when(mockView.getUserInput()).thenReturn("test");
        when(mockModel.submitGuess("test")).thenReturn(true);

        // 通过反射调用私有方法
        invokePrivateMethod("handleGuessSubmission", new ActionEvent(this, 0, "submit"));

        verify(mockView).clearInputField();
        verify(mockModel).submitGuess("test");
    }

    @Test
    void testInvalidInputHandling() throws Exception {
        // 测试短输入
        when(mockView.getUserInput()).thenReturn("abc");
        invokePrivateMethod("handleGuessSubmission", new ActionEvent(this, 0, "submit"));

        verify(mockView).showFeedbackDialog(eq("Invalid Input"), anyString(), eq(true));
        verify(mockModel, never()).submitGuess(anyString());
    }




    @Test
    void testStateUpdateProcessing() throws Exception {
        when(mockModel.getCurrentWord()).thenReturn("test");
        when(mockModel.getTargetWord()).thenReturn("test");

        invokePrivateMethod("handleStateUpdate");

        verify(mockView).updateCharacterStatus(anyList(), eq("test"));
        verify(mockView).setResetButtonEnabled(true);
    }

    @Test
    void testErrorFeedbackSuppression() throws Exception {
        when(mockModel.isErrorDisplayEnabled()).thenReturn(false);
        invokePrivateMethod("showInvalidAttemptFeedback");

        verify(mockView, never()).showFeedbackDialog(anyString(), anyString(), anyBoolean());
    }

    // 反射工具方法
    private void invokePrivateMethod(String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        Method method = GameController.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(controller, args);
    }
}