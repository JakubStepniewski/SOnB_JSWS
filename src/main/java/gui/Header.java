package gui;

import gui.containers.RaidContainer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import logic.Disk;
import logic.RAID.RAID_0;
import logic.RAID.RAID_1;
import logic.RAID.RAID_3;

import java.util.List;

public class Header {
    private final HBox hContainer;
    private final RaidContainer raidContainer;
    private List<Disk> diskList;

    private RAID_0 raid_0;
    private RAID_1 raid_1;
    private RAID_3 raid_3;

    public Header(Region parent, RaidContainer raidContainer, List<Disk> disks) {
        this.hContainer = new HBox(10);
        this.raidContainer = raidContainer;
        this.diskList = disks;

        this.raid_0 = new RAID_0(disks);
        this.raid_1 = new RAID_1(disks);
        this.raid_3 = new RAID_3(disks);

        // Style
        hContainer.setStyle("-fx-background-color: rgb(8, 143, 143);");


        // Buttons
        Button btnWrite = new Button("Write");
        Button btnRead = new Button("Read");
        Button btnReset = new Button("Reset");

        // Buttons onClick
        btnWrite.setOnMouseClicked(event->writeData());
        btnReset.setOnMouseClicked(event -> resetAllDisks());
        btnRead.setOnMouseClicked(event -> readData());

        hContainer.getChildren().addAll(btnWrite,btnRead,btnReset);



        hContainer.setPadding(new Insets(10));

        // Ensure width responsiveness
        hContainer.prefWidthProperty().bind(parent.widthProperty());
    }

    public void writeData()
    {
        int selectedRAID = this.raidContainer.getActiveRAID();

        switch (selectedRAID)
        {
            case 1: // RAID 0
                raid_0.writeData();
                break;
            case 2: // RAID 1
                raid_1.writeData();
                break;
            case 3: // RAID 3
                raid_3.writeData();
                break;
            default:
                System.out.println("RAID MODE NOT SELECTED!");
        }
    }



    public void readData() {
        int selectedRAID = this.raidContainer.getActiveRAID();

        switch (selectedRAID) {
            case 1: // RAID 0
                raid_0.readData();
                break;
            case 2: // RAID 1
                raid_1.readData();
                break;
            case 3: // RAID 3
                raid_3.readData();
                break;
            default:
                System.out.println("RAID MODE NOT SELECTED!");
        }
    }

    private void resetAllDisks() {
        for (Disk disk : diskList) {
            disk.reset();
        }
        System.out.println("All disks have been reset.");
    }


    public HBox render() {
        return hContainer;
    }
}

