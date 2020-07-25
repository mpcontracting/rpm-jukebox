package uk.co.mpcontracting.rpmjukebox.javafx;

import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public enum GuiState {

    INSTANCE;

    @Getter @Setter
    private static Scene scene;
    @Getter @Setter
    private static Stage stage;
    @Getter @Setter
    private static String title;
    @Getter @Setter
    private static HostServices hostServices;
    @Getter @Setter
    private static SystemTray systemTray;
}
