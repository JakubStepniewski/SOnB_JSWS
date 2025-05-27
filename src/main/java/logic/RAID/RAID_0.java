package logic.RAID;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.Disk;

import java.util.List;
import java.util.Optional;

public class RAID_0 {
    private final List<Disk> diskList;

    public RAID_0(List<Disk> disks) {
        if (disks.size() < 2) {
            throw new IllegalArgumentException("RAID 0 requires at least 2 disks.");
        }
        this.diskList = disks;
    }

    public void writeData() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Write Data");
        dialog.setHeaderText("Enter the data to write:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(data -> {
            byte[] dataBytes = data.getBytes();
            int sectorSize = diskList.get(0).getSectorSize();

            // Lista tylko aktywnych dysków
            List<Disk> activeDisks = diskList.stream()
                    .filter(Disk::isActive)
                    .toList();

            if (activeDisks.size() < 2) {
                System.out.println("Not enough active disks for RAID 0 write.");
                return;
            }

            int i = 0;
            while (i < dataBytes.length) {
                for (Disk disk : activeDisks) {
                    if (i >= dataBytes.length) break;

                    int remaining = dataBytes.length - i;
                    int blockSize = Math.min(sectorSize, remaining);
                    byte[] chunk = new byte[blockSize];
                    System.arraycopy(dataBytes, i, chunk, 0, blockSize);

                    disk.write(chunk);
                    i += blockSize;
                }
            }

            System.out.println("RAID 0 write complete (partial striping on active disks).");
        });
    }



    public void readData() {
        StringBuilder result = new StringBuilder();
        int sectorSize = diskList.get(0).getSectorSize();

        // Odczytaj dane z dysków
        byte[][] allData = new byte[diskList.size()][];
        for (int i = 0; i < diskList.size(); i++) {
            allData[i] = diskList.get(i).read();
        }

        int i = 0;
        boolean dataLeft = true;

        while (dataLeft) {
            dataLeft = false;
            for (int d = 0; d < diskList.size(); d++) {
                byte[] diskData = allData[d];
                if (i + sectorSize <= diskData.length) {
                    dataLeft = true;
                    result.append(new String(diskData, i, sectorSize));
                }
            }
            i += sectorSize;
        }

        // Wyświetl dane w oknie dialogowym
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("RAID 0 – Read Data");

        TextArea textArea = new TextArea(result.toString().trim());
        textArea.setWrapText(true);
        textArea.setEditable(false);

        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> dialog.close());

        VBox layout = new VBox(10, textArea, btnClose);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 300);
        dialog.setScene(scene);
        dialog.show();
    }





}
