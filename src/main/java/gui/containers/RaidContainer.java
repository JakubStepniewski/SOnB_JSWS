package gui.containers;


import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.swing.*;

public class RaidContainer {

    private final VBox vContainer;
    private final String containerLabel = "RAID";


    public RaidContainer(Region parent)
    {
        this.vContainer = new VBox(10);
        Text containerHeadline = new Text(containerLabel);
        containerHeadline.setFont(Font.font("Arial",20));
        HBox hContainer = new HBox(10);

        System.out.println(parent.widthProperty());

        for(int i=0;i<3;i++)
        {
            Rectangle raidContainer = new Rectangle(30,150, Color.rgb(206,212,218));
            raidContainer.widthProperty().bind(parent.widthProperty().divide(3).subtract(17)); // TODO this is wrong
            hContainer.getChildren().add(raidContainer);
        }

        vContainer.getChildren().addAll(containerHeadline,hContainer);
    }

    public VBox render()
    {
        return vContainer;
    }

}
