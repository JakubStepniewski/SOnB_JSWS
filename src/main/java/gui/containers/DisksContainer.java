package gui.containers;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import logic.Disk;

public class DisksContainer {

    private final VBox vContainer;
    private final String containerLabel = "Disks";

    public DisksContainer(Region parent)
    {
        this.vContainer = new VBox(10);
        Text containerHeadline = new Text(containerLabel);
        containerHeadline.setFont(Font.font("Arial",20));
        HBox innerHContainer = new HBox(10);

        VBox innerVContainer_1 = new VBox(10);
        VBox innerVContainer_2 = new VBox(10);

        // Create disks
        Disk disk_1 = new Disk(128, 32, parent, "Dysk 1");
        Disk disk_2 = new Disk(128, 32, parent ,"Dysk 2");
        Disk disk_3 = new Disk(128, 32, parent, "Dysk 3");
        Disk disk_4 = new Disk(128, 32, parent, "Dysk 4");

        innerVContainer_1.getChildren().addAll(disk_1.render(),disk_2.render());
        innerVContainer_2.getChildren().addAll(disk_3.render(),disk_4.render());

        innerHContainer.getChildren().addAll(innerVContainer_1,innerVContainer_2);

        vContainer.getChildren().addAll(containerHeadline,innerHContainer);

    }

    public VBox render ()
    {
        return vContainer;
    }

}
