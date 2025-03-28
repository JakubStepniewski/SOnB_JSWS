package gui.containers;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import logic.Disk;

import java.util.ArrayList;
import java.util.List;

public class DisksContainer {

    private final VBox vContainer;
    private final String containerLabel = "Disks";

    private final List<Disk> diskList;

    public DisksContainer(Region parent,List<Disk> disks)
    {
        this.vContainer = new VBox(10);
        this.diskList = disks;
        Text containerHeadline = new Text(containerLabel);
        containerHeadline.setFont(Font.font("Arial",20));
        HBox innerHContainer = new HBox(10);

        VBox innerVContainer_1 = new VBox(10);
        VBox innerVContainer_2 = new VBox(10);





        innerVContainer_1.getChildren().addAll(disks.get(0).render(),disks.get(1).render());
        innerVContainer_2.getChildren().addAll(disks.get(2).render(),disks.get(3).render());

        innerHContainer.getChildren().addAll(innerVContainer_1,innerVContainer_2);

        vContainer.getChildren().addAll(containerHeadline,innerHContainer);

    }

    public VBox render ()
    {
        return vContainer;
    }



    public List<Disk> getDisks()
    {
        return new ArrayList<>(diskList);
    }

}
