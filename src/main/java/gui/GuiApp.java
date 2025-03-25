package gui;

import gui.containers.DisksContainer;
import gui.containers.RaidContainer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.control.ScrollPane;


public class GuiApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        String appTitle = "RAID FaultSim";
        primaryStage.setTitle(appTitle);

        // Main app container
        VBox root = new VBox();


        // Scrollable container
        VBox scrollableContent = new VBox(20);
        scrollableContent.setPadding(new Insets(10));

        // Sticky header
        Header header = new Header(root);

        // RAID container
        RaidContainer raidContainer = new RaidContainer(scrollableContent);

        // Disks container
        DisksContainer diskContainer = new DisksContainer(scrollableContent);

        // Add content to root view
        scrollableContent.getChildren().addAll(raidContainer.render(),diskContainer.render());

        // ScrollPane scene
        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);


        // Set final root view
        root.getChildren().addAll(header.render(),scrollPane);

        // Final scene
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void startApp(String[] args) {
        launch(args);
    }
}