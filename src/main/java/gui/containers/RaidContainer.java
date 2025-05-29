package gui.containers;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import logic.Disk;

import java.util.List;

public class RaidContainer {

    private final VBox vContainer;
    private final String containerLabel = "RAID";
    private final List<Disk> diskList;
    private final HBox hContainer;
    private int activeRAID;

    public RaidContainer(Region parent, List<Disk> disks) {
        this.vContainer = new VBox(10);
        this.diskList = disks;
        this.activeRAID = 0; // 0 - not selected
        Text containerHeadline = new Text(containerLabel);
        containerHeadline.setFont(Font.font("Arial", 20));
        this.hContainer = new HBox(10);

        for (int i = 0; i < 3; i++) {
            StackPane raidPane = createRAIDPane(i + 1, parent);
            hContainer.getChildren().add(raidPane);
        }

        vContainer.getChildren().addAll(containerHeadline, hContainer);
        updateRAIDColors();
    }

    private StackPane createRAIDPane(int raidType, Region parent) {
        StackPane stackPane = new StackPane();

        Rectangle raidContainer = new Rectangle(30, 150, Color.rgb(206, 212, 218));
        raidContainer.widthProperty().bind(parent.widthProperty().divide(3).subtract(17));
        raidContainer.setOnMouseClicked(event -> setActiveRAID(raidType));
        raidContainer.setStroke(Color.WHITE);
        raidContainer.setStrokeWidth(2);

        if(raidType != 3) {
            Text raidText = new Text("RAID " + (raidType-1));
            raidText.setFont(Font.font(16));
            stackPane.getChildren().addAll(raidContainer, raidText);
        }else {
            Text raidText = new Text("RAID " + (raidType));
            raidText.setFont(Font.font(16));
            stackPane.getChildren().addAll(raidContainer, raidText);
        }
        return stackPane;
    }

    public void setActiveRAID(int raid) {
        this.activeRAID = raid;
        updateRAIDColors();
    }

    public int getActiveRAID(){
        return  this.activeRAID;
    }

    private void updateRAIDColors() {
        for (int i = 0; i < hContainer.getChildren().size(); i++) {
            StackPane raidPane = (StackPane) hContainer.getChildren().get(i);
            Rectangle raidContainer = (Rectangle) raidPane.getChildren().get(0);

            if (i + 1 == activeRAID) {
                raidContainer.setFill(Color.WHITE);
                raidContainer.setStroke(Color.BLACK);
                raidContainer.setStrokeWidth(2);
            } else {
                raidContainer.setFill(Color.rgb(206, 212, 218));
                raidContainer.setStroke(Color.WHITE);
                raidContainer.setStrokeWidth(2);
            }
        }
    }

    public VBox render() {
        return vContainer;
    }
}