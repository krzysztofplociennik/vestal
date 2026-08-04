package com.plociennik.vestal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class VestalApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(VestalApplication.class.getResource("/com/plociennik/vestal/vievs/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);
        stage.setTitle("Vestal");
        stage.setScene(scene);
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(VestalApplication.class.getResourceAsStream("/com/plociennik/vestal/icons/flammable-16.png"))),
                new Image(Objects.requireNonNull(VestalApplication.class.getResourceAsStream("/com/plociennik/vestal/icons/flammable-24.png"))),
                new Image(Objects.requireNonNull(VestalApplication.class.getResourceAsStream("/com/plociennik/vestal/icons/flammable-32.png")))
        );
        stage.show();
    }
}
