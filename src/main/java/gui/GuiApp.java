package gui;

import gui.containers.DisksContainer;
import gui.containers.RaidContainer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.control.ScrollPane;
import logic.Disk;

import java.util.ArrayList;
import java.util.List;


public class GuiApp extends Application {

    short raid; // 0 - RAID 0 | 1 - RAID 1 | 2 - RAID 2

    @Override
    public void start(Stage primaryStage) {
        String appTitle = "RAID FaultSim";
        primaryStage.setTitle(appTitle);


        List<Disk> disks = new ArrayList<>();

        // Main app container
        VBox root = new VBox();

        // Create disks
        Disk disk_1 = new Disk(128, 32, root, "Dysk 1");
        Disk disk_2 = new Disk(128, 32, root ,"Dysk 2");
        Disk disk_3 = new Disk(128, 32, root, "Dysk 3");
        Disk disk_4 = new Disk(128, 32, root, "Dysk 4");

        // Add disks to list
        disks.add(disk_1);
        disks.add(disk_2);
        disks.add(disk_3);
        disks.add(disk_4);


        // Scrollable container
        VBox scrollableContent = new VBox(20);
        scrollableContent.setPadding(new Insets(10));



        // RAID container
        RaidContainer raidContainer = new RaidContainer(scrollableContent,disks);

        // Disks container
        DisksContainer diskContainer = new DisksContainer(scrollableContent,disks);

        // Sticky header
        Header header = new Header(root,raidContainer,disks);


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

//    public static void selectRAID(short choice)
//    {
//        this.raid = choice;
//    }

    public static void startApp(String[] args) {
        launch(args);
    }
}