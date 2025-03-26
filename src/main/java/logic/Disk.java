package logic;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Disk {
    private final int numSectors;
    private final int sectorSize;
    private final byte[][] data;
    private final boolean[] badSectors;

    private final boolean[] fullSectors;
    private final GridPane grid;
    private final VBox diskContainer;
    private final String diskLabel;
    private boolean isActive;
    private final Lock lock = new ReentrantLock();

    public Disk(int numSectors, int sectorSize, Region parent,String diskLabel) {
        this.numSectors = numSectors;
        this.sectorSize = sectorSize;
        this.data = new byte[numSectors][sectorSize];
        this.badSectors = new boolean[numSectors];
        this.fullSectors = new boolean[numSectors];
        this.diskLabel = diskLabel;
        this.isActive=true;


        this.diskContainer = new VBox(10);
        this.grid = new GridPane();


        // Disk header
        HBox diskHeader = new HBox(10);
        diskHeader.setAlignment(Pos.CENTER_LEFT);
        Text diskName = new Text(this.diskLabel);
        Rectangle diskActivityIndicator = new Rectangle(15,15,Color.rgb(8, 143, 143));
        HBox diskHeaderBtnContainer = new HBox(10);

        Button btnWrite = new Button("Write");
        Button btnRead = new Button("Read");
        Button btnToogleFailedState = new Button("Turn off");
        Button btnReset = new Button("Reset");
        diskHeaderBtnContainer.getChildren().addAll(btnWrite,btnRead,btnToogleFailedState,btnReset);

        // onclick
        btnWrite.setOnMouseClicked(event-> write());
        btnRead.setOnMouseClicked(event->read());
        btnToogleFailedState.setOnMouseClicked(event->toogleActiveMode(isActive,grid,btnToogleFailedState));
        btnReset.setOnMouseClicked(event->reset(grid));


        // Space beetwen diskName and diskHeaderBtnContainer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        diskHeader.getChildren().addAll(diskActivityIndicator,diskName,spacer,diskHeaderBtnContainer);


        // Sectors grid
        double columns = 16.0;

        for (int i = 0; i < numSectors; i++) {
            Rectangle sector = new Rectangle();
            sector.setFill(badSectors[i] ? Color.rgb(236,11,67) : Color.rgb(206, 212, 218));
            sector.setStrokeWidth(1.0);
            sector.setStroke(Color.rgb(225, 239, 230));

            int finalI = i;
            sector.setOnMouseClicked(event -> failSector(finalI, sector));

            sector.widthProperty().bind(parent.widthProperty().divide(columns*2).subtract(3));
            sector.heightProperty().bind(sector.widthProperty());

            grid.add(sector, i % 16, i / 16);
        }

        diskContainer.getChildren().addAll(diskHeader, grid);
    }

    public VBox render() {
        return diskContainer;
    }

    public void failSector(int sectorIndex, Rectangle sector) {
        if(isActive)
        {
            if (sectorIndex >= 0 && sectorIndex < numSectors) {
                badSectors[sectorIndex] = true;
                sector.setFill(Color.rgb(236,11,67));
            }
        }

    }

    private void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }
    public void toogleActiveMode(boolean isActive,GridPane grid,Button btnToogleFailedState)
    {
        boolean newState = !isActive;
        setActive(newState);
        btnToogleFailedState.setText(newState ? "Turn off" : "Turn on");

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle sector = (Rectangle) node;


                if (newState) {

                    int index = grid.getRowIndex(node) * 16 + grid.getColumnIndex(node);
                    if (badSectors[index]) {
                        sector.setFill(Color.RED);
                    } else if (fullSectors[index]) {
                        sector.setFill(Color.GREEN);
                    } else {
                        sector.setFill(Color.rgb(206, 212, 218));
                    }
                } else {

                    sector.setFill(Color.rgb(52, 58, 64));
                }
            }
        }

    }

    public void write(){
        if(!isActive)
        {
            System.out.println("Disk is inactive! Cannot write data.");
        }

        // modal window to write data
        Stage dialogWindow = new Stage();

        dialogWindow.initModality(Modality.APPLICATION_MODAL);
        dialogWindow.setTitle("Write Data");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);
        TextArea textArea = new TextArea();
        Button btnSave = new Button("Save");


        btnSave.setOnAction(event -> {
            String text = textArea.getText();
            byte[] textBytes = text.getBytes();

            lock.lock();
            try {
                int writtenBytes = 0;
                for (int i = 0; i < numSectors; i++) {
                    if (!fullSectors[i] && !badSectors[i] && writtenBytes < textBytes.length) {
                        Arrays.fill(data[i], (byte) 0);
                        int bytesToWrite = Math.min(sectorSize, textBytes.length - writtenBytes);
                        System.arraycopy(textBytes, writtenBytes, data[i], 0, bytesToWrite);
                        writtenBytes += bytesToWrite;
                        fullSectors[i] = true;

                        // Update sector color to green
                        Rectangle sector = (Rectangle) grid.getChildren().get(i);
                        sector.setFill(Color.GREEN);
                    }
                }

                if (writtenBytes < textBytes.length) {
                    System.out.println("Not enough space to write full data!");
                }
            } finally {
                lock.unlock();
            }

            dialogWindow.close();
        });



        dialogVBox.getChildren().addAll(textArea, btnSave);
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialogWindow.setScene(dialogScene);
        dialogWindow.show();


    }

    public void read(){
        if (!isActive) {
            System.out.println("Disk is inactive. Cannot read.");
            return;
        }

        StringBuilder output = new StringBuilder();
        lock.lock();
        try {
            for (int i = 0; i < numSectors; i++) {
                if (fullSectors[i] && !badSectors[i]) {
                    output.append(new String(data[i])).append(" ");
                }
            }
        } finally {
            lock.unlock();
        }

        System.out.println("Disk Data: " + output.toString().trim());
    }

    public void reset(GridPane grid) {
        setActive(true);

        for (int i = 0; i < numSectors; i++) {
            badSectors[i] = false;
            fullSectors[i] = false;
            Arrays.fill(data[i], (byte) 0); // Reset the data

            Rectangle sector = (Rectangle) grid.getChildren().get(i);
            sector.setFill(Color.rgb(206, 212, 218)); // Reset the sector color to default (inactive)
        }
    }
}




