package logic;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;

import java.util.concurrent.locks.Lock;

public class Disk {
    private final int numSectors;
    private final int sectorSize;
    private final byte[][] data;
    private final boolean[] badSectors;
    private final GridPane grid;
    private final VBox diskContainer;

    private final String diskLabel;
    private final boolean failed;
    //private final Lock lock;

    public Disk(int numSectors, int sectorSize, Region parent,String diskLabel) {
        this.numSectors = numSectors;
        this.sectorSize = sectorSize;
        this.data = new byte[numSectors][sectorSize];
        this.badSectors = new boolean[numSectors];
        this.diskLabel = diskLabel;
        this.failed=false;


        this.diskContainer = new VBox(10);
        Text diskName = new Text(this.diskLabel);
        this.grid = new GridPane();

        double columns = 16.0; // Sektory będą w układzie 16x8

        for (int i = 0; i < numSectors; i++) {
            Rectangle sector = new Rectangle();
            sector.setFill(badSectors[i] ? Color.RED : Color.rgb(206, 212, 218));
            sector.setStrokeWidth(1.0);
            sector.setStroke(Color.rgb(225, 239, 230));

            int finalI = i;
            sector.setOnMouseClicked(event -> failSector(finalI, sector));

            // Bindowanie szerokości sektora do szerokości kontenera
            sector.widthProperty().bind(parent.widthProperty().divide(columns*2).subtract(3));
            sector.heightProperty().bind(sector.widthProperty());

            grid.add(sector, i % 16, i / 16);
        }

        diskContainer.getChildren().addAll(diskName, grid);
    }

    public VBox render() {
        return diskContainer;
    }

    public void failSector(int sectorIndex, Rectangle sector) {
        if (sectorIndex >= 0 && sectorIndex < numSectors) {
            badSectors[sectorIndex] = true;
            sector.setFill(Color.RED); // Aktualizacja koloru sektora
        }
    }
    public void failDisk()
    {

    }
}