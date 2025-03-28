package gui;

import gui.containers.RaidContainer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import logic.Disk;
import logic.RAID.RAID_1;

import java.util.List;

public class Header {
    private final HBox hContainer;
    private final RaidContainer raidContainer;
    private List<Disk> diskList;

    private RAID_1 raid_1;

    public Header(Region parent, RaidContainer raidContainer, List<Disk> disks) {
        this.hContainer = new HBox(10);
        this.raidContainer = raidContainer;
        this.diskList = disks;

        this.raid_1 = new RAID_1(disks);

        // Style
        hContainer.setStyle("-fx-background-color: rgb(8, 143, 143);");


        // Buttons
        Button btnWrite = new Button("Write");
        Button btnRead = new Button("Read");
        Button btnReset = new Button("Reset");

        // Buttons onClick
        btnWrite.setOnMouseClicked(event->writeData());

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
                break;
            case 2: // RAID 1
                this.raid_1.writeData();
                break;
            case 3: // RAID 2
                break;
            default:
                System.out.println("RAID MODE NOT SELECTED!");
        }
    }

    public void readData() {
        int selectedRAID = this.raidContainer.getActiveRAID();

        switch (selectedRAID) {
            case 1: // RAID 0
                break;
            case 2: // RAID 1
                raid_1.readData();
                break;
            case 3: // RAID 2
                break;
            default:
                System.out.println("RAID MODE NOT SELECTED!");
        }
    }

    public HBox render() {
        return hContainer;
    }
}