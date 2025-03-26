package gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class Header {
    private final HBox hContainer;

    public Header(Region parent) {
        this.hContainer = new HBox(10);

        // Style
        hContainer.setStyle("-fx-background-color: rgb(8, 143, 143);");

        Button btnStart = new Button("Start Simulation");
        Button btnReset = new Button("Reset");

        hContainer.getChildren().addAll(btnStart,btnReset);



        hContainer.setPadding(new Insets(10));

        // Ensure width responsiveness
        hContainer.prefWidthProperty().bind(parent.widthProperty());
    }

    public HBox render() {
        return hContainer;
    }
}