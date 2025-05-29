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

import java.nio.charset.StandardCharsets;


public class RAID_0 {
    private final List<Disk> diskList;

    public RAID_0(List<Disk> disks) {
        if (disks.size() < 2) {
            throw new IllegalArgumentException("RAID 0 requires at least 2 disks.");
        }
        this.diskList = disks;
    }

    public void writeData() {
        Stage dialogWindow = new Stage();
        dialogWindow.initModality(Modality.APPLICATION_MODAL);
        dialogWindow.setTitle("Write Data");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(10));

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPromptText("Enter multi-line data here...");

        Button btnSave = new Button("Save");
        btnSave.setOnAction(event -> {
            String data = textArea.getText();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            int sectorSize = diskList.get(0).getSectorSize();

            List<Disk> activeDisks = diskList.stream()
                    .filter(Disk::isActive)
                    .toList();

            if (activeDisks.size() < 2) {
                System.out.println("Not enough active disks for RAID 0 write.");
                dialogWindow.close();
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

            System.out.println("RAID 0 write complete (with multi-line support).");
            dialogWindow.close();
        });

        dialogVBox.getChildren().addAll(textArea, btnSave);
        Scene scene = new Scene(dialogVBox, 400, 250);
        dialogWindow.setScene(scene);
        dialogWindow.show();
    }




    public void readData() {
        StringBuilder result = new StringBuilder();
        int sectorSize = diskList.get(0).getSectorSize();
        int maxLength = 0;

        // Wczytaj dane z każdego dysku
        byte[][] allData = new byte[diskList.size()][];
        for (int i = 0; i < diskList.size(); i++) {
            allData[i] = diskList.get(i).read();
            maxLength = Math.max(maxLength, allData[i].length);
        }

        // Składanie danych z RAID 0 - striping
        for (int i = 0; ; i++) {
            boolean anyData = false;
            for (byte[] diskData : allData) {
                int start = i * sectorSize;
                if (start + sectorSize <= diskData.length) {
                    result.append(new String(diskData, start, sectorSize, StandardCharsets.UTF_8));
                    anyData = true;
                }
            }
            if (!anyData) break;
        }

        // Wyświetlenie wyniku
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("RAID 0 – Read Data");

        TextArea textArea = new TextArea(result.toString());
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
